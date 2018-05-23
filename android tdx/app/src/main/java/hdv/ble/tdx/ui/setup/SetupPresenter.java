package hdv.ble.tdx.ui.setup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.service.BluetoothLeService;
import hdv.ble.tdx.ui.base.BasePresenter;

/**
 * Created by Ann on 2/27/16.
 */
public class SetupPresenter extends BasePresenter<SetupMvpView> {

    DataManager dataManager;

    @Inject
    Bus mEventBus;

    @Inject
    public SetupPresenter(DataManager dataManager) {

        this.dataManager = dataManager;

    }

    @Override
    public void attachView(SetupMvpView mvpView) {
        super.attachView(mvpView);
        mEventBus.register(this);
    }

    @Override
    public void detachView() {
        super.detachView();
        mEventBus.unregister(this);
    }

    @Subscribe
    public void eventUpdateStatus(BusEvent.UpdateStatus event){
        if(isViewAttached())
        {
            getMvpView().updateStatus(event.bCMD1,event.bCMD2,event.bCMD3,event.bCMD4);
        }
    }

    @Subscribe
    public void eventConnect(BusEvent.Connect event){
        if(isViewAttached()) {
            if (event.state == BluetoothLeService.STATE_DISCONVERED) {
                getMvpView().updateStatusConnecttion(true);
            } else if (event.state == BluetoothLeService.STATE_DISCONNECTED) {
                getMvpView().updateStatusConnecttion(false);
            }
        }
    }

}
