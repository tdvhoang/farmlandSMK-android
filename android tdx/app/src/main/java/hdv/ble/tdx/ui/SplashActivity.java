package hdv.ble.tdx.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import hdv.ble.tdx.R;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;
import hdv.ble.tdx.ui.main_old.MainOldActivity;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Ann on 2/27/16.
 */
public class SplashActivity extends BaseActivity {
    private static final int ACTIVITY_REQUEST_TURNONBLE = 2 ;
    private static int SPLASH_TIME_OUT = 1000;

    @Inject
    DataManager dataManager;
    private IkyDevice mIkyDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_splash);

        if(!isEnable()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ACTIVITY_REQUEST_TURNONBLE);
        }else{
            checkIkyDevice();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_REQUEST_TURNONBLE ) {
            checkIkyDevice();
        }
    }

    public boolean isEnable(){
        BluetoothManager mBluetoothManager;
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

    private void checkIkyDevice(){
        new Handler().postDelayed(new Runnable() {
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                dataManager.findIkyDevices()
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Subscriber<IkyDevice>() {

                            @Override
                            public void onCompleted() {
                                if(mIkyDevice != null){
                                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                    startActivity(i);
                                    finish();
                                }else{
                                    Intent i = new Intent(SplashActivity.this, MainOldActivity.class);
                                    startActivity(i);
                                    finish();

                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                Intent i = new Intent(SplashActivity.this, MainOldActivity.class);
                                startActivity(i);
                                finish();

                            }

                            @Override
                            public void onNext(IkyDevice ikyDevice) {
                                mIkyDevice = ikyDevice;

                            }
                        });


            }
        }, SPLASH_TIME_OUT);
    }


}
