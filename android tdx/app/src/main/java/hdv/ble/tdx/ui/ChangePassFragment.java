package hdv.ble.tdx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
public class ChangePassFragment extends Fragment {


    @Bind(R.id.etOldPassChangeFragment)
    EditText etOldPassChangeFragment;
    @Bind(R.id.etNewPassChangeFragment)
    EditText etNewPassChangeFragment;
    @Bind(R.id.etNew2PassChangeFragment)
    EditText etNew2PassChangeFragment;
    @Bind(R.id.ibSaveFragmentChangePass)
    ImageButton ibSaveFragmentChangePass;


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
        View fragmentView = inflater.inflate(R.layout.fragment_changepass, container, false);
        ButterKnife.bind(this, fragmentView);
        ((MainActivity)getActivity()).showTabMenu(false);
        ((MainActivity)getActivity()).setTextTitle("Đổi mật khẩu");
        bus.register(this);

        dataManager.findIkyDevices()
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<IkyDevice>() {
                    @Override
                    public void onCompleted() {

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

    @OnClick(R.id.ibSaveFragmentChangePass)
    void OnClickedSave(){
        if(!etOldPassChangeFragment.getText().toString().equals(mIkyDevice.getPin())){
            toast("Mật khẩu cũ không đúng");
            return;
        }
        if(etNewPassChangeFragment.getText().length() != 4){
            toast("Nhập khẩu mới phải có 4 kí ");
            return;
        }

        if(!etNew2PassChangeFragment.getText().toString().equals(
                etNewPassChangeFragment.getText().toString() )){
            toast("Nhập khẩu mới không trùng nhau");
            return;
        }
        ((MainActivity)getActivity()).getMainPresenter()
                .changePin(etNewPassChangeFragment.getText().toString());
//        ((MainActivity)getActivity()).getMainPresenter()
//                .lock(true);


//        getActivity().onBackPressed();
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
    public void eventUpdatePin(BusEvent.UpdatePin updatePin){
        if(updatePin.isSuccess){
            toast("Đổi mật khẩu thành công ");
            mIkyDevice.setPin(etNewPassChangeFragment.getText().toString());
            saveIkyDevice(mIkyDevice);

        }else{

            toast("Lỗi khi đổi mật khẩu");

        }
    }

    public void saveIkyDevice(IkyDevice ikydevice){
        dataManager.setIkyDevice(ikydevice)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Intent ii = new Intent(getActivity(),SplashActivity.class);
                        getActivity().finish();
                        startActivity(ii);

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
