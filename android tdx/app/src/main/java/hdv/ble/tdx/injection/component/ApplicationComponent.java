package hdv.ble.tdx.injection.component;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import hdv.ble.tdx.BLEApplication;
import hdv.ble.tdx.data.DataManager;
import hdv.ble.tdx.injection.ApplicationContext;
import hdv.ble.tdx.injection.module.ApplicationModule;
import hdv.ble.tdx.service.BluetoothLeService;
import hdv.ble.tdx.ui.control.ControlPresenter;
import hdv.ble.tdx.ui.main.MainPresenter;
import hdv.ble.tdx.ui.account.AccountPresenter;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(BLEApplication BLEApplication);
    void inject(BluetoothLeService bluetoothLeService);
    void inject(ControlPresenter controlPresenter);
    void inject(MainPresenter mainPresenter);
    void inject(AccountPresenter statusPresenter);

    @ApplicationContext
    Context context();
    Application application();
//    EventPosterHelper eventPosterHelper();
    Bus eventBus();
    DataManager dataManager();

}
