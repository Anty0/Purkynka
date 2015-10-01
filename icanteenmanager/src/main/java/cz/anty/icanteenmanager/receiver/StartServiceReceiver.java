package cz.anty.icanteenmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.anty.icanteenmanager.ICService;
import cz.anty.utils.AppDataManager;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            context.startService(new Intent(context, ICService.class)
                    .putExtra(ICService.EXTRA_UPDATE_MONTH, true));
        } else {
            context.sendBroadcast(new Intent(context,
                    StartServiceScheduleReceiver.class));
        }
    }
}
