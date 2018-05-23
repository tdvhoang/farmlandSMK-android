package hdv.ble.tdx.util;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import hdv.ble.tdx.injection.ApplicationContext;


public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "iky_pref";
    public static final String PREF_DEVICE_IKY = "PREF_DEVICE_IKY";
    public static final String PREF_DEVICE_NAME = "PREF_DEVICE_NAME";
    public static final String PREF_DEVICE_ADDRESS = "PREF_DEVICE_ADDRESS";
    public static final String PREF_DEVICE_UUID = "PREF_DEVICE_UUID";
    public static final String PREF_STATUS_LOCK = "PREF_STATUS_LOCK";
    public static final String PREF_STATUS_BATTERY = "PREF_STATUS_PIN";
    public static final String PREF_STATUS_VIBRATE = "PREF_STATUS_VIBRATE";

    private final SharedPreferences mPref;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }
    public void setValue(String key, String value){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key,value);
        editor.commit();
    }
    public void setValue(String key, boolean value){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }
    public void setValue(String key, int value){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public void clearValue(String key){
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key,null);
        editor.commit();

    }
    public String getValue(String key, String stringdefault){
        return mPref.getString(key,stringdefault);
    }
    public boolean getValue(String key, boolean stringdefault){
        return mPref.getBoolean(key,stringdefault);
    }
    public int getValue(String key, int stringdefault){
        return mPref.getInt(key,stringdefault);
    }

}
