package hdv.ble.tdx.ui.control;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import hdv.ble.tdx.BLEApplication;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.data.Protocol;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.injection.ApplicationContext;
import hdv.ble.tdx.service.BluetoothLeService;
import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.ui.base.BasePresenter;
import hdv.ble.tdx.util.AndroidComponentUtil;
import hdv.ble.tdx.util.CommonUtils;
import hdv.ble.tdx.util.NotificationHelper;
import hdv.ble.tdx.util.PreferencesHelper;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Ann on 1/29/16.
 */
public class ControlPresenter extends BasePresenter<ControlMvpView> {

    private static final String TAG = "ControlPresenter";
    @Inject PreferencesHelper preferencesHelper;
    @Inject Bus mEventBus;
    @Inject NotificationHelper notificationHelper;
    @Inject DataManager dataManager;
    Context context;
    private BluetoothLeService mBluetoothLeService;
    private byte valueOfLock, valueOfVibrate;

    IkyDevice mIkyDevice;

    @Inject
    public ControlPresenter(@ApplicationContext Context context, DataManager dataManager) {

        BLEApplication.get(context).getComponent().inject(this);
        this.context = context;
        this.dataManager = dataManager;

    }

    public String getNameDevie(){
        if(mIkyDevice != null && mIkyDevice.getName() != null){
            return mIkyDevice.getName();
        }else{
            return "";
        }
    }

    public void saveIkyDevice(IkyDevice ikydevice){
        mIkyDevice = ikydevice;
        dataManager.setIkyDevice(ikydevice)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

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
                            getMvpView().showDialogAddDevice();
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


    @Override
    public void attachView(ControlMvpView mvpView) {
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

    public boolean isEnable(){
        BluetoothManager mBluetoothManager;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            return false;
        }

        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }

        if(mBluetoothAdapter != null){
           return mBluetoothAdapter.isEnabled();
        }
        return false;
    }


    public boolean isAddedDevice(){
        if(mIkyDevice == null || mIkyDevice.getAddress() == null){
            return false;
        }else{
            return true;
        }
    }

    public void clearDevice(){
        preferencesHelper.clearValue(PreferencesHelper.PREF_DEVICE_ADDRESS);
    }


    public void initialize() {
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
        context.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
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


    public void setNotify(boolean value){
        mBluetoothLeService.enableNotify(value);
    }

    public void sendCommandReadStatus(){
        mBluetoothLeService.writeRXCharacteristic(Protocol.readStatus(mIkyDevice.getPin()));
    }


    public void close(){
        clearDevice();
        if(mBluetoothLeService != null) {
            mBluetoothLeService.close();
        }
//        if(AndroidComponentUtil.isServiceRunning(context,BluetoothLeService.class)){
//            context.unbindService(mServiceConnection);
//        }

    }

    public void disconnect(){
        mBluetoothLeService.disconnect();
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

    public void findBike(){
//        mBluetoothLeService.writeRXCharacteristic(Protocol.findbyke(mIkyDevice.getPin()));
    }


    public void rename(String newName){
//        mBluetoothLeService.writeRXCharacteristic(Protocol.rename(mIkyDevice.getPin(), newName));
    }

    public void changePin(String newPin){
//        mBluetoothLeService.writeRXCharacteristic(Protocol.changePin(mIkyDevice.getPin(), newPin));
    }

    public void clickLockButton(){
//        if(valueOfLock == (byte)0){
//            mBluetoothLeService.writeRXCharacteristic(Protocol.setLockOn(mIkyDevice.getPin()));
//        }else{
//            mBluetoothLeService.writeRXCharacteristic(Protocol.setLockOff(mIkyDevice.getPin()));
//        }
    }

    public void clickVibrateButton(){
//        if(valueOfVibrate == (byte)0){
//            mBluetoothLeService.writeRXCharacteristic(Protocol.setVibrateOn(mIkyDevice.getPin()));
//
//        }else{
//            mBluetoothLeService.writeRXCharacteristic(Protocol.setVibrateOff(mIkyDevice.getPin()));
//        }
    }

    private void updateImageStatus(){
        if(isViewAttached()){
            getMvpView().updateImageForStatus(valueOfLock, valueOfVibrate);
        }
    }

    private void showAlarm(){
        notificationHelper.show("Someone touches your bike");

    }

    //Subscribe event bus, what is sent from Ble BluetoohLeService.class
    @Subscribe
    public void eventReceived(BusEvent.ReceiveData event){

        byte[] bytes = event.values;
//        if(bytes[1] == Protocol.APP_OPCODE_READSTATUS){
//            valueOfLock = bytes[7];
//            valueOfVibrate = bytes[8];
//            updateImageStatus();
//        }else if(bytes[1] == Protocol.APP_OPCODE_SETLOCK){
//            valueOfLock = bytes[7];
//            sendCommandReadStatus();
//        }else if(bytes[1] == Protocol.APP_OPCODE_SETVIBRATE){
//            valueOfVibrate = bytes[7];
//            sendCommandReadStatus();
//        }else if(bytes[1] == Protocol.APP_OPCODE_ALARM){
//            showAlarm();
//        }

        if(isViewAttached()){
            getMvpView().setReceiveData(CommonUtils.convertByteToString(bytes));
        }
    }

    @Subscribe
    public void eventConnect(BusEvent.Connect event){
        if(isViewAttached()) {
            getMvpView().hideLoading();
            if (event.state == BluetoothLeService.STATE_CONNECTED) {
                getMvpView().setNameDevice("Connected");
                notificationHelper.show("Connected");
            } else if (event.state == BluetoothLeService.STATE_CONNECTING) {
                getMvpView().setNameDevice("Connecting");
            } else if (event.state == BluetoothLeService.STATE_DISCONNECTED) {
                getMvpView().setNameDevice("Disconnect");
                notificationHelper.show("Disconnected");
            } else if (event.state == BluetoothLeService.STATE_DISCONVERED) {
                sendCommandReadStatus();
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
