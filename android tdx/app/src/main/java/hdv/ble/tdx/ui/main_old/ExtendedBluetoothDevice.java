package hdv.ble.tdx.ui.main_old;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Ann on 2/29/16.
 */
public class ExtendedBluetoothDevice {

    int rssi;
    public final BluetoothDevice device;

    public ExtendedBluetoothDevice(final BluetoothDevice device, int rssi){
        this.device = device;
        this.rssi = rssi;
    }
}
