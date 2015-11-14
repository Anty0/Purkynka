package cz.anty.purkynkamanager.modules.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;

import java.io.IOException;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.wifi.WifiLogin;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "WifiStateReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!AppDataManager.isWifiAutoLogin()
                || !AppDataManager.isLoggedIn(AppDataManager.Type.WIFI))
            return;

        final WifiInfo wifiInfo = ((WifiManager) context
                .getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (wifiInfo == null) return;
        final String wifiSSID = wifiInfo.getSSID();
        if (wifiSSID == null || !wifiSSID.contains(WifiLogin.WIFI_NAME)) return;

        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                if (AppDataManager.isWifiWaitLogin())
                    Utils.threadSleep(Constants.WAIT_TIME_WIFI_LOGIN);

                try {
                    if (WifiLogin.tryLogin(context, AppDataManager.getUsername(AppDataManager.Type.WIFI),
                            AppDataManager.getPassword(AppDataManager.Type.WIFI),
                            new Handler(context.getMainLooper()), wifiSSID, true)) {
                        AppDataManager.addWifiSuccessfulLoginAttempt();
                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG, "onReceive", e);
                }
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);

    }
}
