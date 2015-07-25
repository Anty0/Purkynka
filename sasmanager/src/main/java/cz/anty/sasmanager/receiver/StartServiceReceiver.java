package cz.anty.sasmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.anty.sasmanager.SASManagerService;
import cz.anty.utils.AppDataManager;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppDataManager.isLoggedIn(AppDataManager.Type.SAS, context)) {
            Intent service = new Intent(context, SASManagerService.class);
            context.startService(service);
        }
    }
}
