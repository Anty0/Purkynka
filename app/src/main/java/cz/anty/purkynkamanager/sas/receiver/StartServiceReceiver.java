package cz.anty.purkynkamanager.sas.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.anty.purkynkamanager.sas.SASManagerService;
import cz.anty.purkynkamanager.utils.AppDataManager;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            Intent service = new Intent(context, SASManagerService.class);
            context.startService(service);
        }
    }
}
