package cz.anty.sasmanager.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;

public class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, StartServiceReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (AppDataManager.isSASMarksAutoUpdate(context) && activeNetInfo != null && activeNetInfo.isConnected() &&
                AppDataManager.isLoggedIn(AppDataManager.Type.SAS, context)) {
            Calendar cal = Calendar.getInstance();
            // start 30 seconds after boot completed
            cal.add(Calendar.SECOND, Constants.WAIT_TIME_FIRST_REPEAT);
            // fetch every 30 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.cancel(pending);
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), Constants.REPEAT_TIME_SAS_MARKS_UPDATE, pending);

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, pending);
        } else {
            service.cancel(pending);
        }
    }
}
