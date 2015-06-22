package cz.anty.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anty on 11.6.15.
 *
 * @author anty
 */
public class LoginDataManager {

    private static final String SAS_SETTINGS_NAME = "SASLoginData";
    private static final String WIFI_SETTINGS_NAME = "WIFILoginData";
    private static final List<Runnable> onChangeSAS = new ArrayList<>();
    private static final List<Runnable> onChangeWIFI = new ArrayList<>();

    public static synchronized void addOnChangeListener(Type type, Runnable onChange) {
        switch (type) {
            case WIFI:
                onChangeWIFI.add(onChange);
                break;
            case SAS:
            default:
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
        context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE).edit()
                .putString("LOGIN", ByteEncryption.xor(username))
                .putString("PASSWORD", ByteEncryption.xor(password))
                .putBoolean("LOGGED_IN", true)
                .apply();
        onChange(type);
    }

    public static synchronized void logout(Type type, Context context) {
        context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean("LOGGED_IN", false)
                .apply();
        onChange(type);
    }

    public static synchronized boolean isLoggedIn(Type type, Context context) {
        return context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE)
                .getBoolean("LOGGED_IN", false);
    }

    public static synchronized String getUsername(Type type, Context context) {
        return ByteEncryption.xor(context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE)
                .getString("LOGIN", ""));
    }

    public static synchronized String getPassword(Type type, Context context) {
        return ByteEncryption.xor(context.getSharedPreferences(type.toString(), Context.MODE_PRIVATE)
                .getString("PASSWORD", ""));
    }

    public static synchronized void setWifiAutoLogin(Context context, boolean toSet) {
        context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean("AUTO_LOGIN", toSet)
                .apply();
    }

    public static synchronized boolean isWifiAutoLogin(Context context) {
        return context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE)
                .getBoolean("AUTO_LOGIN", true);
    }

    public static synchronized void setWifiWaitLogin(Context context, boolean toSet) {
        context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE).edit()
                .putBoolean("WAIT_LOGIN", toSet)
                .apply();
    }

    public static synchronized boolean isWifiWaitLogin(Context context) {
        return context.getSharedPreferences(Type.WIFI.toString(), Context.MODE_PRIVATE)
                .getBoolean("WAIT_LOGIN", true);
    }

    public enum Type {
        SAS, WIFI;

        @Override
        public String toString() {
            switch (this) {
                case WIFI:
                    return WIFI_SETTINGS_NAME;
                case SAS:
                default:
                    return SAS_SETTINGS_NAME;
            }
        }
    }
}
