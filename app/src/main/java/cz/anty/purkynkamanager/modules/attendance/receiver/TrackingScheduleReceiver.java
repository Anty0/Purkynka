package cz.anty.purkynkamanager.modules.attendance.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import cz.anty.purkynkamanager.modules.attendance.TrackingActivity;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;

public class TrackingScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TrackingReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        if (TrackingActivity.mansManager == null)
            TrackingActivity.mansManager = new TrackingMansManager(context);

        if (context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_TRACKING_ATTENDANCE_WARNINGS, true) &&
                TrackingActivity.mansManager.get().length != 0 && Utils.isNetworkAvailable(context)) {
            Calendar cal = Calendar.getInstance();
            // start 30 seconds after boot completed
            cal.add(Calendar.SECOND, Constants.WAIT_TIME_FIRST_REPEAT);
            // fetch every 30 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.cancel(pending);
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), Constants.REPEAT_TIME_TRACKING_ATTENDANCE, pending);

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, pending);
        } else {
            service.cancel(pending);
        }
    }
}
