package hdv.ble.tdx.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Ann on 2/28/16.
 */
public class ChangePINSMKFragment extends Fragment {


    @Bind(R.id.etNewPINSMKFragment)
    EditText etNewPINSMKFragment;

    @Bind(R.id.etTimePINSMKFragment)
    EditText etTimePINSMKFragment;


    @Inject
    DataManager dataManager;
    @Inject
    Bus bus;

    private IkyDevice mIkyDevice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_change_pinsmk, container, false);
        ButterKnife.bind(this, fragmentView);
        ((MainActivity)getActivity()).showTabMenu(false);
        ((MainActivity)getActivity()).setTextTitle("Đổi PIN smartkey");

        if (((MainActivity) getActivity()).getMainPresenter() != null) {
            ((MainActivity) getActivity()).getMainPresenter().getPINSMK();
        }

        bus.register(this);
        dataManager.findIkyDevices()
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<IkyDevice>() {
                    @Override
                    public void onCompleted() {
                        etNewPINSMKFragment.setHint(mIkyDevice.getPINSmartkey());
                        etTimePINSMKFragment.setHint(mIkyDevice.getTimeSMK());
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(IkyDevice ikyDevice) {
                        mIkyDevice = ikyDevice;

                    }
                });
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bus.unregister(this);
    }

    @OnClick(R.id.ibSavePINSMKFragment)
    void OnClickedSave(){

        String sTime = "";
        if(etNewPINSMKFragment.getText().toString().equals("") || etNewPINSMKFragment.getText().toString().length() != 9){
            toast("PIN smartkey mới không hợp lệ");
            return;
        }


        if(etTimePINSMKFragment.getText().toString().equals(""))
        {
            sTime = etTimePINSMKFragment.getHint().toString();
        }
        else
        {
            sTime = etTimePINSMKFragment.getText().toString();
        }

        ((MainActivity)getActivity()).getMainPresenter()
                .changePINSMK(etNewPINSMKFragment.getText().toString(),sTime);

    }


    private void toast(final String ss){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), ss, Toast.LENGTH_SHORT).show();

            }
        });
    }



    @Subscribe
    public void eventUpdatePINSMK(BusEvent.UpdatePinSmartkey updatePINSMK){
        if(updatePINSMK.isSuccess){
            toast("Đổi PIN smartkey thành công ");
            mIkyDevice.setPINSmartkey(etNewPINSMKFragment.getText().toString());
            mIkyDevice.setPINSmartkey(etTimePINSMKFragment.getText().toString());
            saveIkyDevice(mIkyDevice);

        }else{
            toast("Lỗi khi đổi PIN smartkey");
        }
    }

    @Subscribe
    public void eventGetPINSMK(BusEvent.GetPinSmartkey getPINSMK) {

        etNewPINSMKFragment.setHint(getPINSMK.sPINSMK);
        etTimePINSMKFragment.setHint(getPINSMK.sTime);

        mIkyDevice.setPINSmartkey(getPINSMK.sPINSMK);
        mIkyDevice.setPINSmartkey(getPINSMK.sTime);
        saveIkyDevice(mIkyDevice);
    }

    public void saveIkyDevice(IkyDevice ikydevice){
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



}
