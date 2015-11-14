package cz.anty.purkynkamanager.modules.icanteen.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

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

        if ((AppDataManager.isICNotifyNewMonthLunches() || (ICSplashActivity.serviceManager != null
                && ICSplashActivity.serviceManager.isConnected()
                && ICSplashActivity.serviceManager.getBinder().isPendingOrders()))
                && Utils.isNetworkAvailable(context) &&
                AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)) {
            Calendar cal = Calendar.getInstance();
            // start 30 seconds after boot completed
            cal.add(Calendar.SECOND, Constants.WAIT_TIME_FIRST_REPEAT);
            // fetch every 30 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.cancel(pending);
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), Constants.REPEAT_TIME_IC_LUNCHES_UPDATE, pending);
            // TODO: 12.11.2015 longer wait between two refreshes when user disconnect and connect to internet
            // TODO: 12.11.2015 refresh only between 6:00 and 17:00

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, pending);
        } else {
            service.cancel(pending);
        }
    }
}
