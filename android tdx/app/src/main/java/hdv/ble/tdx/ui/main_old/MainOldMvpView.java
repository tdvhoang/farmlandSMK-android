package hdv.ble.tdx.ui.main_old;

import hdv.ble.tdx.ui.base.MvpView;

/**
 * Created by Ann on 1/29/16.
 */
public interface MainOldMvpView extends MvpView {

    void showLoading();

    void hideLoading();

    void showDevicesScaned();

    void updateDeviceScaned(ExtendedBluetoothDevice device);

    void updateOptionsMenu();

    void showDialogPin();



}
