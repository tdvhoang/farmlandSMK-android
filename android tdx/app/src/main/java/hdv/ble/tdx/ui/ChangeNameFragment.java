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
public class ChangeNameFragment extends Fragment {


    @Bind(R.id.etNewNameChangeFragment)
    EditText etNewNameChangeFragment;


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
        View fragmentView = inflater.inflate(R.layout.fragment_changename, container, false);
        ButterKnife.bind(this, fragmentView);
        ((MainActivity)getActivity()).showTabMenu(false);
        ((MainActivity)getActivity()).setTextTitle("Đổi tên thiết bị");

        bus.register(this);

        dataManager.findIkyDevices()
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<IkyDevice>() {
                    @Override
                    public void onCompleted() {
                        etNewNameChangeFragment.setHint(mIkyDevice.getName());
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

    @OnClick(R.id.ibSaveFragmentChangeName)
    void OnClickedSave(){

        if(etNewNameChangeFragment.getText().toString().equals("")){
            toast("Tên mới không đúng");
            return;
        }

        ((MainActivity)getActivity()).getMainPresenter()
                .rename(etNewNameChangeFragment.getText().toString());



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
    public void eventUpdatName(BusEvent.UpdateName updateName){
        if(updateName.isSuccess){
            toast("Đổi tên thành công ");
            mIkyDevice.setName(etNewNameChangeFragment.getText().toString());
            saveIkyDevice(mIkyDevice);

        }else{
            toast("Lỗi khi đổi tên");
        }
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
