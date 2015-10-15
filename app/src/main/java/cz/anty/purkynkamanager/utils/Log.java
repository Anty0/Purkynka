package cz.anty.purkynkamanager.utils;

/**
 * Created by anty on 7.9.15.
 *
 * @author anty
 */
public class Log {

    private static boolean DEBUG = false;

    static void setDebug(boolean DEBUG) {
        Log.DEBUG = DEBUG;
    }

    public static void d(String tag, String msg) {
        if (DEBUG) android.util.Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable tr) {
        android.util.Log.d(tag, msg, tr);
    }

    public static void v(String tag, String msg) {
        if (DEBUG) android.util.Log.v(tag, msg);
    }

    public static void v(String tag, String msg, Throwable tr) {
        android.util.Log.v(tag, msg, tr);
    }
}
