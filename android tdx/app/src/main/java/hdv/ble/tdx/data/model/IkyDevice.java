package hdv.ble.tdx.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ann on 2/24/16.
 */
public class IkyDevice implements Parcelable{
    String name;
    String pin;
    String address;
    String uuid;
    String username;
    String modelBike;
    String TimeSMK;
    String pinSmartkey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModelBike() {
        return modelBike;
    }

    public void setModelBike(String modelBike) {
        this.modelBike = modelBike;
    }

    public String getTimeSMK() {
        return TimeSMK;
    }

    public void setTimeSMK(String sTimeSMK) {
        this.TimeSMK = sTimeSMK;
    }

    public String getPINSmartkey() {return pinSmartkey;}

    public void setPINSmartkey(String pinSmartkey) {
        this.pinSmartkey = pinSmartkey;
    }


    public IkyDevice(){
        this.username = "Chưa xác định";
        this.modelBike = "Chưa xác định";
        this.TimeSMK = "20";
        this.pinSmartkey = "123456789";
    }

    public String getInforUser(){
        return String.format("%s\n%s\n",username,modelBike);
    }

    protected IkyDevice(Parcel in) {
        name = in.readString();
        pin = in.readString();
        address = in.readString();
        uuid = in.readString();
        username = in.readString();
        modelBike = in.readString();
        TimeSMK = in.readString();
        pinSmartkey = in.readString();
    }

    public static final Creator<IkyDevice> CREATOR = new Creator<IkyDevice>() {
        @Override
        public IkyDevice createFromParcel(Parcel in) {
            return new IkyDevice(in);
        }

        @Override
        public IkyDevice[] newArray(int size) {
            return new IkyDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(pin);
        dest.writeString(address);
        dest.writeString(uuid);
        dest.writeString(username);
        dest.writeString(modelBike);
        dest.writeString(TimeSMK);
        dest.writeString(pinSmartkey);
    }
}
