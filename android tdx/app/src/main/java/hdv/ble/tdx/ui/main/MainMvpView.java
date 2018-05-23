package hdv.ble.tdx.ui.main;

import hdv.ble.tdx.ui.base.MvpView;

/**
 * Created by Ann on 2/26/16.
 */
public interface MainMvpView extends MvpView {


    void showError(String ss);

    void toast(String ss);


    void setReceiveData(String data);

    void hideLoading();

    void setNameDevice(String ss);

    void setSendData(String data);

    void updateImageForStatus(byte lock, byte vibrae);

    void errorPin();
}
