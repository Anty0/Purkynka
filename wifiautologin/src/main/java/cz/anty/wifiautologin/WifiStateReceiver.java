package cz.anty.wifiautologin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.ApplicationBase;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.wifi.WifiLogin;

public class WifiStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!AppDataManager.isWifiAutoLogin()
                || !AppDataManager.isLoggedIn(AppDataManager.Type.WIFI))
            return;

        final WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (wifiInfo == null) return;
        String wifiSSID = wifiInfo.getSSID();
        if (wifiSSID == null || !wifiSSID.contains(WifiLogin.WIFI_NAME)) return;

        Toast.makeText(context, String.format(context.getString(R.string
                        .toast_text_logging_to_wifi), wifiInfo.getSSID()),
                Toast.LENGTH_LONG).show();

        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                if (AppDataManager.isWifiWaitLogin()) {
                    try {
                        Thread.sleep(Constants.WAIT_TIME_WIFI_LOGIN);
                    } catch (InterruptedException e) {
                        Log.d("WifiStateReceiver", "onReceive", e);
                    }
                }

                if (!WifiLogin.tryLogin(AppDataManager.getUsername(AppDataManager.Type.WIFI),
                        AppDataManager.getPassword(AppDataManager.Type.WIFI)))
                    return;


                    /*Notification n = new NotificationCompat.Builder(context)
                            .setContentTitle(wifiInfo.getSSID())
                            .setContentText(context.getString(R.string.logged_in) + " " + wifiInfo.getSSID())
                            .setSmallIcon(R.mipmap.ic_launcher_wifi)
                                    //.setContentIntent(null)
                            .setAutoCancel(true)
                                    //.setDefaults(Notification.DEFAULT_ALL)
                                    //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                            .build();*/
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, String.format(context.getString(R.string
                                        .toast_text_logged_in_wifi), wifiInfo.getSSID()),
                                Toast.LENGTH_LONG).show();
                    }
                });
                    /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(3, n);
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        Log.d(null, null, e);
                    }
                    notificationManager.cancel(3);*/
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);

    }
}
