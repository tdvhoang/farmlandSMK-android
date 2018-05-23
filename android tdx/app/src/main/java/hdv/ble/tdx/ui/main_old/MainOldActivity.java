package hdv.ble.tdx.ui.main_old;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class MainOldActivity extends BaseActivity implements MainOldMvpView {
    private static final String TAG = "MainOldActivity";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;


    @Bind(R.id.rlProcessbarmain)
    RelativeLayout rlProcessbar;
    @Bind(R.id.lvDevice)
    ListView lvDevice;
    @Bind(R.id.tvScan)
    TextView tvScan;
    @Bind(R.id.pBSearch)
    ProgressBar pBSearch;


    MainOldPresenter mainOldPresenter;
    LeDeviceListAdapter leDeviceListAdapter;
    IkyDevice ikyDevice;

    @Inject
    DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        //init presenter
        mainOldPresenter = new MainOldPresenter(this);
        mainOldPresenter.attachView(this);

        //init listview
        leDeviceListAdapter = new LeDeviceListAdapter(this);
        lvDevice.setAdapter(leDeviceListAdapter);
        lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mainOldPresenter.saveDevice(leDeviceListAdapter.getItem(position).device);
                showDialogPin();
            }


        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mainOldPresenter.scanLeDevice(true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mainOldPresenter.scanLeDevice(false);
        leDeviceListAdapter.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainOldPresenter.detachView();
    }

    @Override
    public void showLoading() {
        rlProcessbar.setVisibility(View.VISIBLE);

    }
    @OnClick(R.id.tvScan)
    void OnClickTvScan(){
        if(!mainOldPresenter.isScanning()){
            leDeviceListAdapter.clear();
            mainOldPresenter.scanLeDevice(true);
        }else {
            mainOldPresenter.scanLeDevice(false);

        }
    }


    @Override
    public void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rlProcessbar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.d(TAG, "onBackPressed ");
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void showDevicesScaned() {
    }

    @Override
    public void updateDeviceScaned(ExtendedBluetoothDevice device) {

        boolean deviceFound = false;
        for (ExtendedBluetoothDevice extendedBluetoothDevice : leDeviceListAdapter.getItems()) {
            if(extendedBluetoothDevice.device.getAddress().equals(device.device.getAddress())){
                deviceFound = true;
                break;
            }
        }
        if(!deviceFound) {
            leDeviceListAdapter.addDevice(device);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    leDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    @Override
    public void updateOptionsMenu() {
        if (!mainOldPresenter.isScanning()) {
            tvScan.setText(R.string.textsearch);
            pBSearch.setVisibility(View.GONE);
        } else {
            pBSearch.setVisibility(View.VISIBLE);
            tvScan.setText(R.string.stop);
        }
    }

    @Override
    public void showDialogPin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_pin, null);
        final EditText etPin = (EditText)view.findViewById(R.id.etPin);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                .setTitle("Mật khẩu")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String pin = etPin.getText().toString();
                        mainOldPresenter.setIkyDevicePin(pin);
                        saveIkyDevice(mainOldPresenter.getIkyDevice());

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create().show();
    }


    public void saveIkyDevice(IkyDevice ikydevice){
        dataManager.setIkyDevice(ikydevice)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Intent returnIntent = new Intent(MainOldActivity.this, MainActivity.class);
                        returnIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(returnIntent);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }


}
