package hdv.ble.tdx.data.local;


import android.content.ContentValues;
import android.database.Cursor;

import hdv.ble.tdx.data.model.IkyDevice;

public class Db {

    public Db() { }

    public static final class BeaconTable {
        public static final String TABLE_NAME = "ikydevice";

        public static final String COLUMN_UUID = "uuid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PIN = "pin";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_USER = "user";
        public static final String COLUMN_MODELBIKE = "modelbike";
        public static final String COLUMN_TIME_SMK = "timesmk";
        public static final String COLUMN_PIN_SMARTKEY = "pinsmartkey";

        public static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_UUID + " TEXT NOT NULL," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_PIN + " TEXT NOT NULL," +
                        COLUMN_USER + " TEXT NOT NULL," +
                        COLUMN_MODELBIKE + " TEXT NOT NULL," +
                        COLUMN_TIME_SMK + " TEXT NOT NULL," +
                        COLUMN_ADDRESS + " TEXT NOT NULL," +
                        COLUMN_PIN_SMARTKEY + " TEXT NOT NULL" +
                        " );";

        public static ContentValues toContentValues(IkyDevice ikyDevice) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_UUID, ikyDevice.getUuid());
            values.put(COLUMN_NAME, ikyDevice.getName());
            values.put(COLUMN_PIN, ikyDevice.getPin());
            values.put(COLUMN_ADDRESS, ikyDevice.getAddress());
            values.put(COLUMN_USER, ikyDevice.getUsername());
            values.put(COLUMN_MODELBIKE, ikyDevice.getModelBike());
            values.put(COLUMN_TIME_SMK, ikyDevice.getTimeSMK());
            values.put(COLUMN_PIN_SMARTKEY, ikyDevice.getPINSmartkey());
            return values;
        }

        public static IkyDevice parseCursor(Cursor cursor) {
            IkyDevice ikyDevice = new IkyDevice();
            ikyDevice.setUuid(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UUID)));
            ikyDevice.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            ikyDevice.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
            ikyDevice.setPin(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIN)));
            ikyDevice.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER)));
            ikyDevice.setModelBike(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODELBIKE)));
            ikyDevice.setTimeSMK(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME_SMK)));
            ikyDevice.setPINSmartkey(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PIN_SMARTKEY)));
            return ikyDevice;
        }
    }

}
