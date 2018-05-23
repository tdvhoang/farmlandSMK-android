package hdv.ble.tdx.ui.control;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main_old.MainOldActivity;
import hdv.ble.tdx.util.DialogFactory;

/**
 * Created by Ann on 1/29/16.
 */
public class ControlActivity extends BaseActivity implements ControlMvpView {

    private static final int ACTIVITY_REQUEST_TURNONBLE = 1;
    private static final int ACTIVITY_REQUEST_ADDDEVICE = 2;


    @Bind(R.id.ivStatusConnect)
    ImageView ivStatusConnect;

    @Inject
    ControlPresenter controlPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
//        setContentView(R.layout.activity_control);
        ButterKnife.bind(this);
        controlPresenter.attachView(this);

        if (!controlPresenter.isEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ACTIVITY_REQUEST_TURNONBLE);
        }else{
            controlPresenter.initialize();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == Activity.RESULT_OK) {
        if (requestCode == ACTIVITY_REQUEST_TURNONBLE ) {
            if (!controlPresenter.isAddedDevice()) {
                startActivityAddDevice();
            } else {
                controlPresenter.initialize();
            }
        }else if(requestCode == ACTIVITY_REQUEST_ADDDEVICE){
            if(data.getExtras()!= null && data.getExtras().getParcelable("IKYDEVICE") != null){
                IkyDevice ikyDevice = data.getExtras().getParcelable("IKYDEVICE");
                controlPresenter.saveIkyDevice(ikyDevice);
                controlPresenter.connect();
            }else{
                controlPresenter.connect();
            }
        }
//        }
    }

    private void log(String ss){
        Log.d("nna",ss);
    }
    @Override
    protected void onResume() {
        super.onResume();
        log("resume");

    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controlPresenter.detachView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_adddevice:
                startActivityAddDevice();
                break;
            case R.id.menu_setting:
                showDialogSettings();
                break;

            case R.id.menu_reconnect:
//                controlPresenter.connect();
//                controlPresenter.setNotify(true);
                break;
            case R.id.menu_disconnect:
//                controlPresenter.setNotify(false);
                break;

        }
        return true;
    }



    @Override
    public void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                rlProcessbar.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void hideLoading() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                rlProcessbar.setVisibility(View.GONE);
//            }
//        });

    }

    @Override
    public void showError(String error) {
        DialogFactory.createGenericErrorDialog(this,error).show();
    }

    @Override
    public void setNameDevice(final String device) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tvNameDevice.setText(device);
//            }
//        });

    }

    @Override
    public void setSendData(final String sendData) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tvTx.setText("T:" + sendData);
//            }
//        });

    }

    @Override
    public void setReceiveData(final String rssi) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                tvRssiDevice.setText("R:" + rssi);
//            }
//        });
    }

    @Override
    public void toast(final String ss) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ControlActivity.this, ss, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void updateImageForStatus(byte lock, byte vibrate) {



    }


    @Override
    public void showDialogAddDevice() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogFactory.createGenericErrorDialog(ControlActivity.this,
                        "You don't have any IKY Device. \nPlease add the device",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityAddDevice();
                            }
                        })
                        .show();

            }
        });

    }

    @Override
    public void showDialogSettings() {


    }

    @Override
    public void showDialogRename() {



    }

    @Override
    public void showDialogChangePin() {


    }


    private void startActivityAddDevice(){
        controlPresenter.close();
        Intent ii = new Intent(getApplicationContext(), MainOldActivity.class);
        ii.addFlags(0);
        startActivityForResult(ii, ACTIVITY_REQUEST_ADDDEVICE);
    }
}
