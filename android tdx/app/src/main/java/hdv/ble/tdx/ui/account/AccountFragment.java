package hdv.ble.tdx.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.data.model.IkyDevice;
import hdv.ble.tdx.ui.ChangeNameFragment;
import hdv.ble.tdx.ui.ChangePassFragment;
import hdv.ble.tdx.ui.SplashActivity;
import hdv.ble.tdx.ui.UserFragment;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;
import hdv.ble.tdx.util.PreferencesHelper;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Ann on 2/27/16.
 */
public class AccountFragment extends Fragment implements AccountMvpView {

    @Bind(R.id.tvChangeNameDeviceFragmentUser)
    TextView tvChangeNameDeviceFragmentUser;
    @Bind(R.id.tvPassFragmentUser)
    TextView tvPassFragmentUser;


    @Inject
    AccountPresenter statusPresenter;

    @Inject
    DataManager dataManager;

    @Inject
    PreferencesHelper preferencesHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this, fragmentView);
        statusPresenter.attachView(this);
        ((MainActivity)getActivity()).showTabMenu(true);
        ((MainActivity)getActivity()).showTitleUser(true);
        ((MainActivity)getActivity()).setTextTitle("Tài khoản");

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

                        if(ikyDevice != null ){
                            //tvInforUser.setText(ikyDevice.getInforUser());

                        }
                    }
                });
        return fragmentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        statusPresenter.detachView();
    }


    @Override
    public void showDialogInforUser() {
        ((MainActivity)getActivity()).showTitleUser(false);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment, new UserFragment())
                .addToBackStack(null)
                .commit();

    }

    @OnClick(R.id.tvChangeNameDeviceFragmentUser)
    void OnClickChangeName(){

        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment, new ChangeNameFragment())
                .addToBackStack(null)
                .commit();


    }

    @OnClick(R.id.tvPassFragmentUser)
    void OnClickChangePass(){
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment, new ChangePassFragment())
                .addToBackStack(null)
                .commit();

    }

    @OnClick(R.id.tvChangeDevFragmentUser)
    void OnClickedChangeDev(){
        dataManager.deleteIkyDevice()
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
