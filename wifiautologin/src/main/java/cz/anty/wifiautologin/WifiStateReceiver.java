package cz.anty.wifiautologin;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.OnceRunThread;
import cz.anty.utils.wifi.WifiLogin;

public class WifiStateReceiver extends BroadcastReceiver {

    private final OnceRunThread worker = new OnceRunThread();

    @Override
    public void onReceive(final Context context, Intent intent) {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (LoginDataManager.isWifiWaitLogin(context)) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Log.d(null, null, e);
                    }
                }
                if (!LoginDataManager.isWifiAutoLogin(context) || !LoginDataManager.isLoggedIn(LoginDataManager.Type.WIFI, context))
                    return;
                WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                if (wifiInfo.getSSID().contains(WifiLogin.WIFI_NAME) &&
                    WifiLogin.tryLogin(LoginDataManager.getUsername(LoginDataManager.Type.WIFI, context),
                            LoginDataManager.getPassword(LoginDataManager.Type.WIFI, context))) {

                    Notification n = new NotificationCompat.Builder(context)
                            .setContentTitle(wifiInfo.getSSID())
                            .setContentText(context.getString(R.string.logged_in) + " " + wifiInfo.getSSID())
                            .setSmallIcon(R.mipmap.ic_launcher_wifi)
                                    //.setContentIntent(null)
                            .setAutoCancel(true)
                                    //.setDefaults(Notification.DEFAULT_ALL)
                                    //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(3, n);
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        Log.d(null, null, e);
                    }
                    notificationManager.cancel(3);
                }
            }
        });
    }
}
