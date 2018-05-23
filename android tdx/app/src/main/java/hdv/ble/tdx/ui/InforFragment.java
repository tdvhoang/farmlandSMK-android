package hdv.ble.tdx.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import hdv.ble.tdx.R;
import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.ui.base.BaseActivity;
import hdv.ble.tdx.ui.main.MainActivity;

/**
 * Created by Ann on 2/28/16.
 */
public class InforFragment extends Fragment {

    @Inject
    Bus bus;

    private TextView tvVersion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_infor, container, false);
        ButterKnife.bind(this, fragmentView);
        ((MainActivity)getActivity()).setTextTitle("Thông tin sản phẩm");
        tvVersion = (TextView)fragmentView.findViewById(R.id.tvVersion);
        bus.register(this);

        ((MainActivity) getActivity()).getMainPresenter().readVersion();

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bus.unregister(this);
    }

    @Subscribe
    public void eventVersion(BusEvent.EventVersion eventVersion){
        tvVersion.setText("Phiên bản: " + eventVersion.version);
    }
}
