package cz.anty.wifiautologin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.thread.OnceRunThread;
import cz.anty.utils.wifi.WifiLogin;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final OnceRunThread worker = new OnceRunThread(null);

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!LoginDataManager.isWifiAutoLogin(context) || !LoginDataManager.isLoggedIn(LoginDataManager.Type.WIFI, context))
            return;

        final WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (wifiInfo.getSSID().contains(WifiLogin.WIFI_NAME) &&
                WifiLogin.tryLogin(LoginDataManager.getUsername(LoginDataManager.Type.WIFI, context),
                        LoginDataManager.getPassword(LoginDataManager.Type.WIFI, context))) {

            worker.setPowerManager((PowerManager) context.getSystemService(Context.POWER_SERVICE));
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
                            Toast toast = new Toast(context);
                            toast.setText(context.getString(R.string.logged_in) + " " + wifiInfo.getSSID());
                            toast.show();
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
            });
        }
    }
}
