package cz.anty.wifiautologin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import cz.anty.utils.LoginDataManager;
import cz.anty.utils.OnceRunThread;
import cz.anty.utils.wifi.WifiLogin;

public class WifiStateReceiver extends BroadcastReceiver {

    private OnceRunThread worker = new OnceRunThread();

    @Override
    public void onReceive(final Context context, Intent intent) {
        worker.startWorker(new Runnable() {
            @Override
            public void run() {
                if (!LoginDataManager.isWifiAutoLogin(context) || !LoginDataManager.isLoggedIn(LoginDataManager.Type.WIFI, context))
                    return;
                WifiInfo wifiInfo = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
                if (wifiInfo.getSSID().contains(WifiLogin.WIFI_NAME)) {
                    WifiLogin.tryLogin(LoginDataManager.getUsername(LoginDataManager.Type.WIFI, context),
                            LoginDataManager.getPassword(LoginDataManager.Type.WIFI, context));
                }
            }
        });
    }
}
