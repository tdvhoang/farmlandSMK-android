package hdv.ble.tdx.ui.main_old;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.ui.base.BasePresenter;
import hdv.ble.tdx.util.PreferencesHelper;

/**
 * Created by Ann on 1/29/16.
 */
public class MainOldPresenter extends BasePresenter<MainOldMvpView> {

    public static final long SCAN_PERIOD = 20000;

    private final BluetoothAdapter mBluetoothAdapter;
    private Context context;
    private Handler mHandler ;
    private boolean mScanning;

    private PreferencesHelper preferencesHelper;

    private IkyDevice ikyDevice;

    public MainOldPresenter(Context context) {

        this.context = context;

        preferencesHelper = new PreferencesHelper(context);
        final BluetoothManager bluetoothManager =
                (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
    }

    @Override
    public void attachView(MainOldMvpView mvpView) {
        super.attachView(mvpView);
    }

    @Override
    public void detachView() {
        super.detachView();
    }


    public IkyDevice getIkyDevice(){
        return ikyDevice;
    }
    public void setIkyDevicePin(String pin){
        ikyDevice.setPin(pin);
    }
    public void saveDevice(BluetoothDevice bluetoothDevice){
        ikyDevice = new IkyDevice();
        ikyDevice.setName(bluetoothDevice.getName());
        ikyDevice.setAddress(bluetoothDevice.getAddress());
        ikyDevice.setUuid("UUId");

    }

    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if(isViewAttached()){
                        getMvpView().hideLoading();
                        getMvpView().updateOptionsMenu();
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            if(isViewAttached()){
                getMvpView().showLoading();
                getMvpView().showDevicesScaned();
                getMvpView().updateOptionsMenu();
            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if(isViewAttached()){
                getMvpView().hideLoading();
                getMvpView().updateOptionsMenu();
            }
        }
    }

    public boolean isScanning(){
        return mScanning;
    }

    private static final String TAG = "MainOldPresenter";
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    String msg = "payload = ";
                    for (byte b : scanRecord)
                        msg += String.format("%02x ", b);
                    Log.d(TAG, device.getAddress() + "onLeScan " + msg);
//                    Log.d(TAG, "onLeScan " + msg);

                    if(isViewAttached()) {
                        getMvpView().updateDeviceScaned(new ExtendedBluetoothDevice(device,rssi));
                        getMvpView().hideLoading();
                    }
                }
            };
}
