package cz.anty.purkynkamanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;

import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.thread.OnceRunThread;
import cz.anty.utils.update.UpdateConnector;

public class UpdateReceiver extends BroadcastReceiver {

    //private static final long DEFER_TIME = 1000 * 60 * 60 * 20;
    private static final OnceRunThread worker = new OnceRunThread();

    public static void checkUpdate(Context context) throws IOException, NumberFormatException {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        Integer latestCode = UpdateConnector.getLatestVersionCode();
        String latestName = UpdateConnector.getLatestVersionName();
        /*long latestTime = preferences.getInt("LATEST_CODE", BuildConfig.VERSION_CODE) == BuildConfig.VERSION_CODE ?
                System.currentTimeMillis() : preferences.getLong("LATEST_TIME", System.currentTimeMillis());*/
        boolean showNotification = !latestCode.equals(preferences.getInt(Constants.SETTING_NAME_LATEST_CODE, BuildConfig.VERSION_CODE));

        preferences.edit().putInt(Constants.SETTING_NAME_LATEST_CODE, latestCode)
                .putString(Constants.SETTING_NAME_LATEST_NAME, latestName)
                        //.putLong("LATEST_TIME", latestTime)
                .apply();

        if (showNotification)
            showNotification(context);
    }

    private static void showNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(Constants.NOTIFICATION_ID_UPDATE,
                        new NotificationCompat.Builder(context)
                                .setContentTitle(context.getString(R.string.notify_title_update))
                                .setContentText(context.getString(R.string.notify_text_update_new)
                                        .replace(Constants.STRINGS_CONST_VERSION, getLatestName(context)))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(context.getString(R.string.notify_text_update_new)
                                                .replace(Constants.STRINGS_CONST_VERSION, getLatestName(context)) + "\n" +
                                                context.getString(R.string.notify_text_update_old)
                                                        .replace(Constants.STRINGS_CONST_VERSION, BuildConfig.VERSION_NAME)))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(PendingIntent.getActivity(
                                context, 0, new Intent(context, MainActivity.class), 0))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                                //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                        .build());
    }

    public static boolean isUpdateAvailable(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        //preferences.edit().putBoolean("CANT_START", updateAvailable && getDeferTime(context) <= 0).apply();
        return preferences.getInt(Constants.SETTING_NAME_LATEST_CODE, BuildConfig.VERSION_CODE) != BuildConfig.VERSION_CODE;
    }

    public static String getLatestName(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_LATEST_NAME, BuildConfig.VERSION_NAME);
    }

    public static int getLatestCode(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getInt(Constants.SETTING_NAME_LATEST_CODE, BuildConfig.VERSION_CODE);
    }

    /*public static long getDeferTime(Context context) {
        return DEFER_TIME - (System.currentTimeMillis() -
                context.getSharedPreferences("MainData", Context.MODE_PRIVATE)
                        .getLong("LATEST_TIME", System.currentTimeMillis()));
    }*/

    @Override
    public void onReceive(final Context context, Intent intent) {
        //SharedPreferences preferences = context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        /*if (preferences.getInt("ACTUAL_CODE", -1) != BuildConfig.VERSION_CODE) {
            preferences.edit().clear().putInt("ACTUAL_CODE", BuildConfig.VERSION_CODE).apply();
        }*/

        worker.setPowerManager(context);
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    checkUpdate(context);
                } catch (IOException | NumberFormatException e) {
                    Log.d("UpdateReceiver", "onReceive", e);
                }
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);
    }
}
