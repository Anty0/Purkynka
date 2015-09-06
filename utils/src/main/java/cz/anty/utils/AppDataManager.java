package cz.anty.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anty on 11.6.15.
 *
 * @author anty
 */
public class AppDataManager {

    private static final List<Runnable> onChangeSAS = new ArrayList<>();
    private static final List<Runnable> onChangeWIFI = new ArrayList<>();
    private static Boolean DEBUG = null;

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

    public static synchronized void login(Type type, Context context, String username, String password) {
        if (isDebugMode(context)) Log.d("AppDataManager", "login type: " + type);
        context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_LOGIN, ByteEncryption.xor(username))
                .putString(Constants.SETTING_NAME_PASSWORD, ByteEncryption.xor(password))
                .putBoolean(Constants.SETTING_NAME_LOGGED_IN, true)
                .apply();
        onChange(type);
    }

    public static synchronized void logout(Type type, Context context) {
        if (isDebugMode(context)) Log.d("AppDataManager", "logout type: " + type);
        context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_LOGGED_IN, false)
                .apply();
        onChange(type);
    }

    public static synchronized boolean isLoggedIn(Type type, Context context) {
        if (isDebugMode(context)) Log.d("AppDataManager", "isLoggedIn type: " + type);
        return context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_LOGGED_IN, false);
    }

    public static synchronized String getUsername(Type type, Context context) {
        if (isDebugMode(context)) Log.d("AppDataManager", "getUsername type: " + type);
        return ByteEncryption.xor(context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_LOGIN, ""));
    }

    public static synchronized String getPassword(Type type, Context context) {
        if (isDebugMode(context)) Log.d("AppDataManager", "getPassword type: " + type);
        return ByteEncryption.xor(context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_PASSWORD, ""));
    }

    public static synchronized void setSASMarksAutoUpdate(Context context, boolean toSet) {
        context.getSharedPreferences(Type.SAS.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_MARKS_UPDATE, toSet)
                .apply();
    }

    public static synchronized boolean isSASMarksAutoUpdate(Context context) {
        return context.getSharedPreferences(Type.SAS.toString(), Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_MARKS_UPDATE, true);
    }

    public static synchronized void setWifiAutoLogin(Context context, boolean toSet) {
        context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_AUTO_LOGIN, toSet)
                .apply();
    }

    public static synchronized boolean isWifiAutoLogin(Context context) {
        return context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_AUTO_LOGIN, true);
    }

    public static synchronized void setWifiWaitLogin(Context context, boolean toSet) {
        context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_WAIT_LOGIN, toSet)
                .apply();
    }

    public static synchronized boolean isWifiWaitLogin(Context context) {
        return context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_WAIT_LOGIN, true);
    }

    public static synchronized void setDebugMode(Context context, boolean toSet) {
        context.getSharedPreferences(Type.DEBUG.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_DEBUG_MODE, toSet)
                .apply();
        DEBUG = toSet;
    }

    public static synchronized boolean isDebugMode(@Nullable Context context) {
        if (DEBUG == null)
            if (context != null)
                DEBUG = context.getSharedPreferences(Type.DEBUG.toString(), Context.MODE_PRIVATE)
                        .getBoolean(Constants.SETTING_NAME_DEBUG_MODE, false);
            else return false;
        return DEBUG;
    }

    public enum Type {
        SAS, WIFI, I_CANTEEN, DEBUG;

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
