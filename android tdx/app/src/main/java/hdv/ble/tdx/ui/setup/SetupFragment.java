package hdv.ble.tdx.ui.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hdv.ble.tdx.R;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;

/**
 * Created by Ann on 2/27/16.
 */
public class SetupFragment extends Fragment implements SetupMvpView {

    @Bind(R.id.ibCMD1FragmentSetUp)
    ImageButton ibCMD1FragmentSetUp;

    @Bind(R.id.ibCMD2FragmentSetUp)
    ImageButton ibCMD2FragmentSetUp;

    @Bind(R.id.ibCMD3FragmentSetUp)
    ImageButton ibCMD3FragmentSetUp;

    @Bind(R.id.ibCMD4FragmentSetUp)
    ImageButton ibCMD4FragmentSetUp;

    @Bind(R.id.ivStatusConnect)
    ImageView ivStatusConnect;

    @Inject
    SetupPresenter setupPresenter;

    private boolean bCMD1Status = false;
    private boolean bCMD2Status = false;
    private boolean bCMD3Status = false;
    private boolean bCMD4Status = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_setup, container, false);
        ButterKnife.bind(this, fragmentView);

        setupPresenter.attachView(this);

        if (((MainActivity) getActivity()).getMainPresenter() != null) {
            ((MainActivity) getActivity()).getMainPresenter().sendCommandReadStatus();
        }

        return fragmentView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        setupPresenter.detachView();
    }

    @OnClick(R.id.ibCMD1FragmentSetUp)
    void onClickedCMD1()
    {
        ((MainActivity) getActivity()).getMainPresenter().sendCMD1((byte) (bCMD1Status ? 0 : 1 ));
    }

    @OnClick(R.id.ibCMD2FragmentSetUp)
    void onClickedCMD2()
    {
        ((MainActivity) getActivity()).getMainPresenter().sendCMD2((byte) (bCMD2Status ? 0 : 1 ));
    }

    @OnClick(R.id.ibCMD3FragmentSetUp)
    void onClickedCMD3()
    {
        ((MainActivity) getActivity()).getMainPresenter().sendCMD3((byte) (bCMD3Status ? 0 : 1 ));
    }

    @OnClick(R.id.ibCMD4FragmentSetUp)
    void onClickedCMD4()
    {
        ((MainActivity) getActivity()).getMainPresenter().sendCMD4((byte) (bCMD4Status ? 0 : 1 ));
    }

    @Override
    public void updateStatus(boolean bCMD1,boolean bCMD2,boolean bCMD3,boolean bCMD4)
    {
        bCMD1Status = bCMD1;
        bCMD2Status = bCMD2;
        bCMD3Status = bCMD3;
        bCMD4Status = bCMD4;

        if (bCMD1Status) {
            ibCMD1FragmentSetUp.setImageResource(R.drawable.ic_cmd1_on);
        } else {
            ibCMD1FragmentSetUp.setImageResource(R.drawable.ic_cmd1_off);
        }

        if (bCMD2Status) {
            ibCMD2FragmentSetUp.setImageResource(R.drawable.ic_cmd2_on);
        } else {
            ibCMD2FragmentSetUp.setImageResource(R.drawable.ic_cmd2_off);
        }

        if (bCMD3Status) {
            ibCMD3FragmentSetUp.setImageResource(R.drawable.ic_cmd3_on);
        } else {
            ibCMD3FragmentSetUp.setImageResource(R.drawable.ic_cmd3_off);
        }

        if (bCMD4Status) {
            ibCMD4FragmentSetUp.setImageResource(R.drawable.ic_cmd4_on);
        } else {
            ibCMD4FragmentSetUp.setImageResource(R.drawable.ic_cmd4_off);
        }

        updateStatusConnecttion(true);
    }

    @Override
    public void updateStatusConnecttion(boolean value) {
        if(value){
            ivStatusConnect.setImageResource(R.drawable.ic_connect);
        }else {
            ivStatusConnect.setImageResource(R.drawable.ic_disconnect);
        }
    }
}
