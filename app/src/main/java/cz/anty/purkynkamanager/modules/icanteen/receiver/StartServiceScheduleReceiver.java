package cz.anty.purkynkamanager.modules.icanteen.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cz.anty.purkynkamanager.modules.icanteen.ICService;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;

public class StartServiceScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, StartServiceReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        service.cancel(pending);
        if (Utils.isNetworkAvailable(context)) {
            if ((AppDataManager.isICNotifyNewMonthLunches() || (ICSplashActivity.serviceManager != null
                    && ICSplashActivity.serviceManager.isConnected()
                    && ICSplashActivity.serviceManager.getBinder().isPendingOrders()))
                    && AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {

                service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        AppDataManager.getLastRefresh(AppDataManager.Type.I_CANTEEN) + Constants
                                .REPEAT_TIME_IC_LUNCHES_UPDATE, Constants
                                .REPEAT_TIME_IC_LUNCHES_UPDATE, pending);
            } else context.startService(new Intent(context, ICService.class)
                    .putExtra(ICService.EXTRA_UPDATE_MONTH, false));
        }
    }
}
