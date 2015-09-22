package cz.anty.purkynkamanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;

import cz.anty.utils.ApplicationBase;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.update.UpdateConnector;

public class UpdateReceiver extends BroadcastReceiver {

    public static void checkUpdate(Context context) throws IOException, NumberFormatException {
        Integer latestCode = UpdateConnector.getLatestVersionCode();
        String latestName = UpdateConnector.getLatestVersionName();

        context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .edit().putInt(Constants.SETTING_NAME_LATEST_CODE, latestCode)
                .putString(Constants.SETTING_NAME_LATEST_NAME, latestName)
                .apply();

        if (isUpdateAvailable(context))
            showNotification(context);
    }

    private static void showNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .notify(Constants.NOTIFICATION_ID_UPDATE,
                        new NotificationCompat.Builder(context)
                                .setContentTitle(context.getString(R.string.notify_title_update))
                                .setContentText(String.format(context.getString(R.string.notify_text_update_new), getLatestName(context)))
                                .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(String.format(context.getString(R.string.notify_text_update_new), getLatestName(context)) + "\n"
                                                + String.format(context.getString(R.string.notify_text_update_old), BuildConfig.VERSION_NAME)))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(PendingIntent.getActivity(
                                context, 0, new Intent(context, MainActivity.class), 0))
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .build());
    }

    public static boolean isUpdateAvailable(Context context) {
        return getLatestCode(context) != BuildConfig.VERSION_CODE;
    }

    public static String getLatestName(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_LATEST_NAME, BuildConfig.VERSION_NAME);
    }

    public static int getLatestCode(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getInt(Constants.SETTING_NAME_LATEST_CODE, BuildConfig.VERSION_CODE);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        ApplicationBase.WORKER.startWorker(new Runnable() {
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
