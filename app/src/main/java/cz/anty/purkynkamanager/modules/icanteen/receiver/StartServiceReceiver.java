package cz.anty.purkynkamanager.modules.icanteen.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.anty.purkynkamanager.modules.icanteen.ICService;
import cz.anty.purkynkamanager.utils.other.AppDataManager;

public class StartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            context.startService(new Intent(context, ICService.class)
                    .putExtra(ICService.EXTRA_UPDATE_MONTH, true));
            return;
        }
        context.sendBroadcast(new Intent(context,
                StartServiceScheduleReceiver.class));
    }
}
