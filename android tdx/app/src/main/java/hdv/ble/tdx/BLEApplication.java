package hdv.ble.tdx;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import hdv.ble.tdx.injection.component.ApplicationComponent;
import hdv.ble.tdx.injection.component.DaggerApplicationComponent;
import hdv.ble.tdx.injection.module.ApplicationModule;

/**
 * Created by Ann on 2/12/16.
 */
public class BLEApplication extends Application {

    @Inject Bus mEventBus;
    ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);
        mEventBus.register(this);

    }

    public static BLEApplication get(Context context) {
        return (BLEApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        return applicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }
}
