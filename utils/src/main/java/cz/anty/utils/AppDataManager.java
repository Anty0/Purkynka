package cz.anty.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anty on 11.6.15.
 *
 * @author anty
 */
public class AppDataManager {

    private static final int SAVE_VERSION = 1;

    private static final List<Runnable> onChangeSAS = new ArrayList<>();
    private static final List<Runnable> onChangeWIFI = new ArrayList<>();

    public static synchronized void addOnChangeListener(Type type, Runnable onChange) {
        switch (type) {
            case WIFI:
                if (!onChangeWIFI.contains(onChange))
                    onChangeWIFI.add(onChange);
                break;
            case SAS:
            default:
                if (!onChangeSAS.contains(onChange))
                    onChangeSAS.add(onChange);
                break;
        }
    }

    public static synchronized void removeOnChangeListener(Type type, Runnable onChange) {
        switch (type) {
            case WIFI:
                onChangeWIFI.remove(onChange);
                break;
            case SAS:
            default:
                onChangeSAS.remove(onChange);
                break;
        }
    }

    private static synchronized void onChange(Type type) {
        List<Runnable> runnableList;
        switch (type) {
            case WIFI:
                runnableList = onChangeWIFI;
                break;
            case SAS:
            default:
                runnableList = onChangeSAS;
                break;
        }
        for (Runnable runnable : runnableList) {
            runnable.run();
        }
    }

    static synchronized void init(Context context) {
        Type.init(context);
        Log.d("AppDataManager", "init");
        SharedPreferences preferences = Type.DEBUG.getSharedPreferences();
        if (preferences.getInt(Constants.SETTING_NAME_DATA_SAVE_VERSION, -1)
                != SAVE_VERSION) {
            Type.clearSettings();
            preferences.edit()
                    .putInt(Constants.SETTING_NAME_DATA_SAVE_VERSION, SAVE_VERSION)
                    .apply();
        }
    }

    public static synchronized void login(Type type, String username, String password) {
        Log.d("AppDataManager", "login type: " + type);
        type.getSharedPreferences().edit()
                .putString(Constants.SETTING_NAME_LOGIN, ByteEncryption.xorToByte(username))
                .putString(Constants.SETTING_NAME_PASSWORD, ByteEncryption.xorToByte(password))
                .putBoolean(Constants.SETTING_NAME_LOGGED_IN, true)
                .apply();
        onChange(type);
    }

    public static synchronized void logout(Type type) {
        Log.d("AppDataManager", "logout type: " + type);
        type.getSharedPreferences().edit()
                .putBoolean(Constants.SETTING_NAME_LOGGED_IN, false)
                .apply();
        onChange(type);
    }

    public static synchronized boolean isLoggedIn(Type type) {
        Log.d("AppDataManager", "isLoggedIn type: " + type);
        return type.getSharedPreferences()
                .getBoolean(Constants.SETTING_NAME_LOGGED_IN, false);
    }

    public static synchronized String getUsername(Type type) {
        Log.d("AppDataManager", "getUsername type: " + type);
        return ByteEncryption.xorFromByte(type.getSharedPreferences()
                .getString(Constants.SETTING_NAME_LOGIN, ""));
    }

    public static synchronized String getPassword(Type type) {
        Log.d("AppDataManager", "getPassword type: " + type);
        return ByteEncryption.xorFromByte(type.getSharedPreferences()
                .getString(Constants.SETTING_NAME_PASSWORD, ""));
    }

    public static synchronized boolean isSASMarksAutoUpdate() {
        Log.d("AppDataManager", "isSASMarksAutoUpdate");
        return Type.SAS.getSharedPreferences()
                .getBoolean(Constants.SETTING_NAME_MARKS_UPDATE, true);
    }

    public static synchronized void setSASMarksAutoUpdate(boolean toSet) {
        Log.d("AppDataManager", "setSASMarksAutoUpdate toSet: " + toSet);
        Type.SAS.getSharedPreferences().edit()
                .putBoolean(Constants.SETTING_NAME_MARKS_UPDATE, toSet)
                .apply();
    }

    public static synchronized boolean isWifiAutoLogin() {
        Log.d("AppDataManager", "isWifiAutoLogin");
        return Type.WIFI.getSharedPreferences()
                .getBoolean(Constants.SETTING_NAME_AUTO_LOGIN, true);
    }

    public static synchronized void setWifiAutoLogin(boolean toSet) {
        Log.d("AppDataManager", "setWifiAutoLogin toSet: " + toSet);
        Type.WIFI.getSharedPreferences().edit()
                .putBoolean(Constants.SETTING_NAME_AUTO_LOGIN, toSet)
                .apply();
    }

    public static synchronized boolean isWifiWaitLogin() {
        Log.d("AppDataManager", "isWifiWaitLogin");
        return Type.WIFI.getSharedPreferences()
                .getBoolean(Constants.SETTING_NAME_WAIT_LOGIN, true);
    }

    public static synchronized void setWifiWaitLogin(boolean toSet) {
        Log.d("AppDataManager", "setWifiWaitLogin toSet: " + toSet);
        Type.WIFI.getSharedPreferences().edit()
                .putBoolean(Constants.SETTING_NAME_WAIT_LOGIN, toSet)
                .apply();
    }

    public static synchronized boolean isDebugMode() {
        return Type.DEBUG.getSharedPreferences()
                .getBoolean(Constants.SETTING_NAME_DEBUG_MODE, false);
    }

    public static synchronized void setDebugMode(boolean toSet) {
        Type.DEBUG.getSharedPreferences().edit()
                .putBoolean(Constants.SETTING_NAME_DEBUG_MODE, toSet)
                .apply();
        Log.setDebug(toSet);
    }

    public enum Type {
        SAS, WIFI, I_CANTEEN, DEBUG;

        private static SharedPreferences SAS_PREFS, WIFI_PREFS, I_CANTEEN_PREFS, DEBUG_PREFS;

        static void init(Context context) {
            SAS_PREFS = context.getSharedPreferences(SAS.toString(), Context.MODE_PRIVATE);
            WIFI_PREFS = context.getSharedPreferences(WIFI.toString(), Context.MODE_PRIVATE);
            I_CANTEEN_PREFS = context.getSharedPreferences(I_CANTEEN.toString(), Context.MODE_PRIVATE);
            DEBUG_PREFS = context.getSharedPreferences(DEBUG.toString(), Context.MODE_PRIVATE);
            Log.setDebug(isDebugMode());
        }

        static void clearSettings() {
            SAS_PREFS.edit().clear().apply();
            WIFI_PREFS.edit().clear().apply();
            I_CANTEEN_PREFS.edit().clear().apply();
            DEBUG_PREFS.edit().clear().apply();
        }

        SharedPreferences getSharedPreferences() {
            switch (this) {
                case DEBUG:
                    return DEBUG_PREFS;
                case I_CANTEEN:
                    return I_CANTEEN_PREFS;
                case WIFI:
                    return WIFI_PREFS;
                case SAS:
                default:
                    return SAS_PREFS;
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case DEBUG:
                    return Constants.SETTINGS_NAME_DEBUG;
                case I_CANTEEN:
                    return Constants.SETTINGS_NAME_I_CANTEEN;
                case WIFI:
                    return Constants.SETTINGS_NAME_WIFI;
                case SAS:
                default:
                    return Constants.SETTINGS_NAME_SAS;
            }
        }
    }
}
