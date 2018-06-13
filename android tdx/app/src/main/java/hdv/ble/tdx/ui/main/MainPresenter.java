package hdv.ble.tdx.ui.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import hdv.ble.tdx.BLEApplication;
import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.data.Protocol;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.injection.ApplicationContext;
import hdv.ble.tdx.service.BluetoothLeService;
import hdv.ble.tdx.ui.base.BasePresenter;
import hdv.ble.tdx.util.AndroidComponentUtil;
import hdv.ble.tdx.util.CommonUtils;
import hdv.ble.tdx.util.EventPosterHelper;
import hdv.ble.tdx.util.NotificationHelper;
import hdv.ble.tdx.util.PreferencesHelper;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Ann on 2/26/16.
 */
public class MainPresenter extends BasePresenter<MainMvpView> {
    private static final String TAG = "MainPresenter";


    @Inject
    PreferencesHelper preferencesHelper;
    @Inject
    Bus mEventBus;
    @Inject
    NotificationHelper notificationHelper;

    @Inject
    EventPosterHelper eventPosterHelper;

    private final DataManager dataManager;
    private Context context;
    private BluetoothLeService mBluetoothLeService;
    private IkyDevice mIkyDevice;

    @Inject
    public MainPresenter(@ApplicationContext Context context, DataManager dataManager){
        BLEApplication.get(context).getComponent().inject(this);
        this.dataManager = dataManager;
        this.context = context;

    }


    @Override
    public void attachView(MainMvpView mvpView) {
        mEventBus.register(this);
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        mEventBus.unregister(this);
        if(AndroidComponentUtil.isServiceRunning(context,BluetoothLeService.class)) {
            context.unbindService(mServiceConnection);
        }
        super.detachView();
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                if(isViewAttached()){
                    getMvpView().showError("Unable to initialize Bluetooth");
                }
            }
            getIkyDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public void bindService() {
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }


    private void deleteIkyDevice(){
        dataManager.deleteIkyDevice()
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        if(isViewAttached()){
                            getMvpView().errorPin();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }

                });


    }
    private void getIkyDevice(){
        dataManager.findIkyDevices()
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<IkyDevice>() {
                    @Override
                    public void onCompleted() {
                        if(mIkyDevice == null && isViewAttached()){
                            getMvpView().toast("Error");
                        }else {
                            connect();
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        getMvpView().toast("IkyDevice Error");

                    }

                    @Override
                    public void onNext(IkyDevice ikyDevice) {

                        mIkyDevice = ikyDevice;

                    }
                });


    }

    public void connect(){
        if(mIkyDevice != null && mIkyDevice.getAddress() != null) {
            mBluetoothLeService.connect(mIkyDevice.getAddress());
        }else{
            if(isViewAttached()){
                getMvpView().showError("Cannot connect Iky device");
            }
        }
    }

    public void logon(){

        mBluetoothLeService.writeRXCharacteristic(Protocol.logOn(mIkyDevice.getPin()));

    }
    public void readVersion(){

        mBluetoothLeService.writeRXCharacteristic(Protocol.version(mIkyDevice.getPin()));
    }

    public void sendCMD1(byte value){
        mBluetoothLeService.writeRXCharacteristic(Protocol.sendCMD1(mIkyDevice.getPin(),value));
    }

    public void sendCMD2(byte value){
        mBluetoothLeService.writeRXCharacteristic(Protocol.sendCMD2(mIkyDevice.getPin(),value));
    }

    public void sendCMD3(byte value){
        mBluetoothLeService.writeRXCharacteristic(Protocol.sendCMD3(mIkyDevice.getPin(),value));
    }

    public void sendCMD4(byte value){
        mBluetoothLeService.writeRXCharacteristic(Protocol.sendCMD4(mIkyDevice.getPin(),value));
    }

    public void rename(String newName){
        mBluetoothLeService.writeRXCharacteristic(Protocol.rename(mIkyDevice.getPin(), newName));
    }

    public void changePINSMK(String newPIN, String newTime){
        mBluetoothLeService.writeRXCharacteristic(Protocol.changePINSMK(mIkyDevice.getPin(), newPIN, newTime));
    }

    public void getPINSMK(){
        mBluetoothLeService.writeRXCharacteristic(Protocol.readPINSMK(mIkyDevice.getPin()));
    }

    public void changePin(String newPin){
        mBluetoothLeService.writeRXCharacteristic(Protocol.changePin(mIkyDevice.getPin(), newPin));
    }


    public void sendCommandReadStatus(){
        if(mIkyDevice != null) {
            mBluetoothLeService.writeRXCharacteristic(Protocol.readStatus(mIkyDevice.getPin()));
        }
    }


    //Subscribe event bus, what is sent from Ble BluetoohLeService.class
    @Subscribe
    public void eventReceived(BusEvent.ReceiveData event){

        byte[] bytes = event.values;
        Log.d(TAG, "eventReceived " + CommonUtils.convertByteToString(bytes));

        if(bytes[Protocol.OPCODE_OFFSET] == Protocol.OPCODE_STATUS){
            //CA 9 8 E6 68 F8 59 0 0 0 0 9F
            if(bytes.length > 11) {
                try {
                    BusEvent.UpdateStatus eventUpdateStatus = new BusEvent.UpdateStatus();

                    if (bytes[Protocol.DATA_OFFSET] == 0) {
                        eventUpdateStatus.bCMD1 = false;
                    } else {
                        eventUpdateStatus.bCMD1 = true;
                    }

                    if (bytes[Protocol.DATA_OFFSET + 1] == 0) {
                        eventUpdateStatus.bCMD2 = false;
                    } else {
                        eventUpdateStatus.bCMD2 = true;
                    }

                    if (bytes[Protocol.DATA_OFFSET + 2] == 0) {
                        eventUpdateStatus.bCMD3 = false;
                    } else {
                        eventUpdateStatus.bCMD3 = true;
                    }

                    if (bytes[Protocol.DATA_OFFSET + 3] == 0) {
                        eventUpdateStatus.bCMD4 = false;
                    } else {
                        eventUpdateStatus.bCMD4 = true;
                    }

                    eventPosterHelper.postEventSafely(eventUpdateStatus);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }else if(bytes[Protocol.OPCODE_OFFSET] == Protocol.OPCODE_LOGON){
            try {
                byte statusCode = bytes[3];
                if (statusCode != Protocol.STATUS_CODE_SUCCESS) {
                    deleteIkyDevice();
                }else{
                    sendCommandReadStatus();
                }
            }catch (Exception e){
                e.printStackTrace();
                deleteIkyDevice();
            }

        }else if(bytes[1] == Protocol.OPCODE_NAME){
            BusEvent.UpdateName updateName = new BusEvent.UpdateName();
            try {
                byte statusCode = bytes[3];
                if (statusCode == Protocol.STATUS_CODE_SUCCESS) {
                    updateName.isSuccess = true;
                } else {
                    updateName.isSuccess = false;
                }
            }catch (Exception e){
                e.printStackTrace();
                updateName.isSuccess = false;
            }
            eventPosterHelper.postEventSafely(updateName);

        }else if(bytes[1] == Protocol.OPCODE_PIN){
            BusEvent.UpdatePin updatePin = new BusEvent.UpdatePin();
            try {
                byte statusCode = bytes[3];
                if (statusCode == Protocol.STATUS_CODE_SUCCESS) {
                    updatePin.isSuccess = true;
                } else {
                    updatePin.isSuccess = false;
                }
            }catch (Exception e){
                e.printStackTrace();
                updatePin.isSuccess = false;
            }
            eventPosterHelper.postEventSafely(updatePin);
        }else if(bytes[1] == Protocol.OPCODE_FWVER){

            //0xCA 0x8F 0x05 0x31 0x2E 0x30 0x2E 0x38 0xF5
            if(bytes.length > 5 ){
                byte[] version = new byte[bytes.length - 4];
                for (int i = 3; i < bytes.length - 1; i++) {
                    version[i-3] = bytes[i];

                }
                String sVersion = new String(version);
                eventPosterHelper.postEventSafely(new BusEvent.EventVersion(sVersion));

            }
        }else if(bytes[1] == Protocol.OPCODE_READ_SMARTKEY){

            //0xCA 0x8F 0x05 0x31 0x2E 0x30 0x2E 0x38 0xF5
            if(bytes.length > 17 ){
                byte[] bPINSMK = new byte[bytes.length - 6];
                byte[] bTIME = new byte[2];
                for (int i = 7; i < bytes.length - 3; i++) {
                    bPINSMK[i-7] = bytes[i];

                }
                bTIME[0] = bytes[bytes.length - 3];
                bTIME[1] = bytes[bytes.length - 2];

                String sPINSMK = new String(bPINSMK);
                String sTIME = new String(bTIME);
                eventPosterHelper.postEventSafely(new BusEvent.GetPinSmartkey(sPINSMK, sTIME));

            }
        }else if(bytes[1] == Protocol.OPCODE_WRITE_SMARTKEY){

            //0xCA 0x8F 0x05 0x31 0x2E 0x30 0x2E 0x38 0xF5
            BusEvent.UpdatePinSmartkey updatePin = new BusEvent.UpdatePinSmartkey(false);
            try {
                byte statusCode = bytes[3];
                if (statusCode == Protocol.STATUS_CODE_SUCCESS) {
                    updatePin.isSuccess = true;
                } else {
                    updatePin.isSuccess = false;
                }
            }catch (Exception e){
                e.printStackTrace();
                updatePin.isSuccess = false;
            }
            eventPosterHelper.postEventSafely(updatePin);
        }



        if(isViewAttached()){
            getMvpView().setReceiveData(CommonUtils.convertByteToString(bytes));
        }
    }

    @Subscribe
    public void eventConnect(BusEvent.Connect event){
        if(isViewAttached()){
            getMvpView().hideLoading();
            if(event.state == BluetoothLeService.STATE_CONNECTED){
                getMvpView().setNameDevice("Connected");
            }else if(event.state == BluetoothLeService.STATE_CONNECTING){
                getMvpView().setNameDevice("Connecting");
            }else if(event.state == BluetoothLeService.STATE_DISCONNECTED){
                getMvpView().setNameDevice("Disconnect");
                notificationHelper.show("Disconnected");
            }else if(event.state == BluetoothLeService.STATE_DISCONVERED){
//
                notificationHelper.show("Connected");
                logon();
            }
        }
    }


    @Subscribe
    public void eventSendData(BusEvent.SendData event){
        if(isViewAttached()){
            getMvpView().hideLoading();
            getMvpView().setSendData(CommonUtils.convertByteToString(event.msg));
        }
    }

    @Subscribe
    public void eventError(BusEvent.ShowError event){
        if(isViewAttached()){
            getMvpView().hideLoading();
            getMvpView().showError(event.message);
        }
    }






}
