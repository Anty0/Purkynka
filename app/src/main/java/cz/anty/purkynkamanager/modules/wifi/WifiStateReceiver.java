package cz.anty.purkynkamanager.modules.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.wifi.WifiLogin;

public class WifiStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (!AppDataManager.isWifiAutoLogin()
                || !AppDataManager.isLoggedIn(AppDataManager.Type.WIFI))
            return;

        final WifiInfo wifiInfo = ((WifiManager) context
                .getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (wifiInfo == null) return;
        String wifiSSID = wifiInfo.getSSID();
        if (wifiSSID == null || !wifiSSID.contains(WifiLogin.WIFI_NAME)) return;

        Toast.makeText(context, String.format(context.getString(R.string
                        .toast_text_logging_to_wifi), wifiInfo.getSSID()),
                Toast.LENGTH_LONG).show();

        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                if (AppDataManager.isWifiWaitLogin())
                    Utils.threadSleep(Constants.WAIT_TIME_WIFI_LOGIN);

                if (!WifiLogin.tryLogin(AppDataManager.getUsername(AppDataManager.Type.WIFI),
                        AppDataManager.getPassword(AppDataManager.Type.WIFI)))
                    return;

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, String.format(context.getString(R.string
                                        .toast_text_logged_in_wifi), wifiInfo.getSSID()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);

    }
}
