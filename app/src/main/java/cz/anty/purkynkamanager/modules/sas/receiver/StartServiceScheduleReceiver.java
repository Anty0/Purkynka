package cz.anty.purkynkamanager.modules.sas.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Random;

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
        if (AppDataManager.isSASMarksAutoUpdate() && Utils.isNetworkAvailable(context) &&
                AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            Random random = new Random();
            Calendar calendar = Calendar.getInstance();
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (hours >= 6 && hours < 17 && day != Calendar.SATURDAY && day != Calendar.SUNDAY) {
                service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        context.getSharedPreferences(Constants.SETTINGS_NAME_MARKS, Context.MODE_PRIVATE)
                                .getLong(Constants.SETTING_NAME_LAST_REFRESH, 0) + Constants
                                .REPEAT_TIME_SAS_MARKS_UPDATE, (long) ((float) Constants
                                .REPEAT_TIME_SAS_MARKS_UPDATE * ((float) (random.nextInt(3) - 1) / 10f)), pending);
            } else {
                if (hours >= 6 || day == Calendar.SATURDAY || day == Calendar.SUNDAY)
                    do {
                        calendar.add(Calendar.DAY_OF_WEEK, 1);
                        day = calendar.get(Calendar.DAY_OF_WEEK);
                    } while (day == Calendar.SATURDAY || day == Calendar.SUNDAY);

                calendar.set(Calendar.HOUR_OF_DAY, 6);
                calendar.set(Calendar.MINUTE, random.nextInt(50) + 5);
                service.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, getClass()), 0));
            }

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, pending);
        }
    }
}
