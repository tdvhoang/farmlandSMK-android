package hdv.ble.tdx.ui.setup;

import hdv.ble.tdx.ui.base.MvpView;

/**
 * Created by Ann on 2/27/16.
 */
public interface SetupMvpView extends MvpView {

    void updateStatus(boolean bCMD1,boolean bCMD2,boolean bCMD3,boolean bCMD4);

    void updateStatusConnecttion(boolean value);
}
