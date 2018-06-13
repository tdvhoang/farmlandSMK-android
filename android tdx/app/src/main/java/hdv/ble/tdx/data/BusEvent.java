package hdv.ble.tdx.data;

/**
 * Created by Ann on 2/9/16.
 */
public class BusEvent {
    public static class Connect {
        public int state;
    }

    public static class SendData {
        public byte[] msg;
    }

    public static class ReceiveData{
        public byte[] values;
    }

    public static class ShowError {
        public String message;
    }

    public static class UpdateStatus {

        public boolean bCMD1;
        public boolean bCMD2;
        public boolean bCMD3;
        public boolean bCMD4;
    }

    public static class EventLocation{

        public boolean bStatus;
        public byte rssi_current;
        public byte rssi_turnon;
        public byte rssi_turnoff;
        public byte time;

        public EventLocation(boolean bStatus, byte rssi_current, byte rssi_turnon, byte rssi_turnoff, byte time) {
            this.bStatus = bStatus;
            this.rssi_current = rssi_current;
            this.rssi_turnon = rssi_turnon;
            this.rssi_turnoff = rssi_turnoff;
            this.time = time;
        }
    }

    public static class EventVersion{
        public String version;

        public EventVersion(String version) {
            this.version = version;
        }
    }


    public static class UpdatePin {

        public boolean isSuccess;

    }

    public static class UpdateName{

        public boolean isSuccess;

    }

    public static class GetPinSmartkey{

        public String sPINSMK;
        public String sTime;

        public GetPinSmartkey(String _sPINSMK, String _sTime ) {
            this.sPINSMK = _sPINSMK;
            this.sTime = _sTime;
        }
    }

    public static class StartEmergency{

        public boolean isSuccess;
        public byte status;
    }

    public static class UpdatePinSmartkey{

        public boolean isSuccess;
        public UpdatePinSmartkey(boolean _isSuccess) {
            this.isSuccess = _isSuccess;
        }
    }
}
