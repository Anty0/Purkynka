package cz.anty.timetablemanager.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.Locale;

import cz.anty.timetablemanager.widget.TimetableLessonWidget;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.timetable.Timetable;

public class TimetableScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TScheduleReceiver", "onReceive");
        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent defaultIntent = new Intent(context, AttendanceReceiver.class);
        PendingIntent defaultPending = PendingIntent.getBroadcast(context, 0,
                defaultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        service.cancel(defaultPending);

        if ((context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, false)
                || context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_LESSON_WARNINGS, false)
                || AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,
                TimetableLessonWidget.class)).length > 0)
                && activeNetInfo != null && activeNetInfo.isConnected()) {

            /*Calendar cal = Calendar.getInstance();
            // start 30 seconds after boot completed
            cal.add(Calendar.SECOND, Constants.WAIT_TIME_FIRST_REPEAT);
            // fetch every 30 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), Constants.REPEAT_TIME_TEACHERS_ATTENDANCE, defaultPending);*/

            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            for (int d = 0, days_strings_idsLength = Timetable.DAYS_STRINGS_IDS.length; d < days_strings_idsLength; d++) {
                int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                int day = calendar.get(Calendar.DAY_OF_WEEK);

                if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                    for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
                        int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i];
                        if (minuteTime < requestedTime - 10) {
                            intent.putExtra(AttendanceReceiver.DAY, day - 2)
                                    .putExtra(AttendanceReceiver.LESSON_INDEX, i);
                            calendar.set(Calendar.HOUR_OF_DAY, Timetable.START_TIMES_HOURS[i]);
                            calendar.set(Calendar.MINUTE, Timetable.START_TIMES_MINUTES[i] - 10);
                            calendar.set(Calendar.MILLISECOND, 0);
                            service.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent
                                    .getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT));
                            //testSupplementation(context, day, i);
                            return;
                        }
                    }
                }

                calendar.add(Calendar.DAY_OF_WEEK, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            }

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, defaultPending);
        }
    }
}
