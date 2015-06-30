package cz.anty.purkynkamanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;

import cz.anty.utils.thread.OnceRunThread;
import cz.anty.utils.update.UpdateConnector;

public class UpdateReceiver extends BroadcastReceiver {

    private static final long DEFER_TIME = 1000 * 60 * 60 * 20;
    private static final OnceRunThread worker = new OnceRunThread(null);

    public static void checkUpdate(Context context) throws IOException {
        SharedPreferences preferences = context.getSharedPreferences("MainData", Context.MODE_PRIVATE);
        Integer latestCode = UpdateConnector.getLatestVersionCode();
        String latestName = UpdateConnector.getLatestVersionName();
        long latestTime = preferences.getInt("LATEST_CODE", BuildConfig.VERSION_CODE) == BuildConfig.VERSION_CODE ?
                System.currentTimeMillis() : preferences.getLong("LATEST_TIME", System.currentTimeMillis());
        boolean showNotification = !latestCode.equals(preferences.getInt("LATEST_CODE", BuildConfig.VERSION_CODE));

        preferences.edit().putInt("LATEST_CODE", latestCode)
                .putString("LATEST_NAME", latestName)
                .putLong("LATEST_TIME", latestTime).apply();

        if (showNotification)
            showNotification(context);
    }

    private static void showNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(3, new NotificationCompat.Builder(context)
                        .setContentTitle(context.getString(R.string.notification_update_title))
                        .setContentText(context.getString(R.string.notification_update_text_old) + " " +
                                BuildConfig.VERSION_NAME + " " + context.getString(R.string.notification_update_text_new) +
                                " " + getLatestName(context))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(PendingIntent.getActivity(
                                context, 0, new Intent(context, MainActivity.class), 0))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                                //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                        .build());
    }

    public static boolean isUpdateAvailable(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MainData", Context.MODE_PRIVATE);
        boolean updateAvailable = preferences.getInt("LATEST_CODE", BuildConfig.VERSION_CODE) != BuildConfig.VERSION_CODE;
        preferences.edit().putBoolean("CANT_START", updateAvailable && getDeferTime(context) <= 0).apply();
        return updateAvailable;
    }

    public static String getLatestName(Context context) {
        return context.getSharedPreferences("MainData", Context.MODE_PRIVATE)
                .getString("LATEST_NAME", BuildConfig.VERSION_NAME);
    }

    public static long getDeferTime(Context context) {
        return DEFER_TIME - (System.currentTimeMillis() -
                context.getSharedPreferences("MainData", Context.MODE_PRIVATE)
                        .getLong("LATEST_TIME", System.currentTimeMillis()));
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("MainData", Context.MODE_PRIVATE);
        if (preferences.getInt("ACTUAL_CODE", -1) != BuildConfig.VERSION_CODE) {
            preferences.edit().clear().putInt("ACTUAL_CODE", BuildConfig.VERSION_CODE).apply();
        }

        worker.setPowerManager((PowerManager) context.getSystemService(Context.POWER_SERVICE));
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    checkUpdate(context);
                } catch (IOException ignored) {
                }
            }
        });
    }
}
