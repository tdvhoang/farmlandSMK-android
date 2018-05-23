package hdv.ble.tdx.ui.account;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import hdv.ble.tdx.BLEApplication;
import hdv.ble.tdx.data.BusEvent;
import hdv.ble.tdx.injection.ApplicationContext;
import hdv.ble.tdx.ui.base.BasePresenter;
import hdv.ble.tdx.util.PreferencesHelper;

/**
 * Created by Ann on 2/27/16.
 */
public class AccountPresenter extends BasePresenter<AccountMvpView> {

    private final Context context;
    @Inject
    Bus mEventBus;

    @Inject
    PreferencesHelper preferencesHelper;



    @Inject
    public AccountPresenter(@ApplicationContext Context context){
        BLEApplication.get(context).getComponent().inject(this);
        this.context = context;
    }

    @Override
    public void attachView(AccountMvpView mvpView) {
        super.attachView(mvpView);
        mEventBus.register(this);
        if(isViewAttached()){
//            getMvpView().updateStatusLock(preferencesHelper.getValue(PreferencesHelper.PREF_STATUS_LOCK,false));
//            getMvpView().updateStatusVibrate(preferencesHelper.getValue(PreferencesHelper.PREF_STATUS_VIBRATE,false));
//            getMvpView().updateStatusBattery((byte) preferencesHelper.getValue(PreferencesHelper.PREF_STATUS_BATTERY,-1));
        }

    }

    @Override
    public void detachView() {
        super.detachView();
        mEventBus.unregister(this);
    }

    private void saveStatus(BusEvent.UpdateStatus event){
        //preferencesHelper.setValue(PreferencesHelper.PREF_STATUS_LOCK, event.bLock);
        //preferencesHelper.setValue(PreferencesHelper.PREF_STATUS_VIBRATE, event.bVibrate);
        //preferencesHelper.setValue(PreferencesHelper.PREF_STATUS_BATTERY, event.battery);
    }


    @Subscribe
    public void eventUpdateStatus(BusEvent.UpdateStatus event) {
        if(isViewAttached()) {
//            getMvpView().updateStatusLock(event.bLock);
//            getMvpView().updateStatusVibrate(event.bVibrate);
//            getMvpView().updateStatusBattery(event.battery);
            saveStatus(event);
        }
    }

}


