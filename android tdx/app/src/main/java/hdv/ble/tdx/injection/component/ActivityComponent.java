package hdv.ble.tdx.injection.component;

import dagger.Component;
import hdv.ble.tdx.injection.PerActivity;
import hdv.ble.tdx.injection.module.ActivityModule;
import hdv.ble.tdx.ui.ChangeNameFragment;
import hdv.ble.tdx.ui.ChangePassFragment;
import hdv.ble.tdx.ui.InforFragment;
import hdv.ble.tdx.ui.SplashActivity;
import hdv.ble.tdx.ui.UserFragment;
import hdv.ble.tdx.ui.account.AccountFragment;
import hdv.ble.tdx.ui.control.ControlActivity;
import hdv.ble.tdx.ui.main.MainActivity;
import hdv.ble.tdx.ui.main_old.MainOldActivity;
import hdv.ble.tdx.ui.setup.SetupFragment;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(ControlActivity controlActivity);
    
    void inject(MainActivity mainActivity);

    void inject(SetupFragment setupFragment);

    void inject(AccountFragment statusFragment);

    void inject(SplashActivity splashActivity);

    void inject(UserFragment userFragment);

    void inject(ChangePassFragment changePassFragment);

    void inject(MainOldActivity mainOldActivity);

    void inject(ChangeNameFragment changeNameFragment);

    void inject(InforFragment inforFragment);

}

