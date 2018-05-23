package hdv.ble.tdx.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;

/**
 * Created by Ann on 2/28/16.
 */
public class UserFragment extends Fragment {


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
        ((MainActivity)getActivity()).showTabMenu(false);
        ((MainActivity)getActivity()).setTextTitle("Thông tin cá nhân");

        return fragmentView;
    }

//    @OnClick(R.id.tvDeviceFragmentUser)
//    void OnClickDevice(){
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        fm.beginTransaction()
//                .replace(R.id.fragment, new DeviceFragment())
//                .addToBackStack(null)
//                .commit();
//
//    }

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

}
