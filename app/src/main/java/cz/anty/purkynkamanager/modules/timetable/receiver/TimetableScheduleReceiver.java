package cz.anty.purkynkamanager.modules.timetable.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.util.Calendar;

import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;

public class TimetableScheduleReceiver extends BroadcastReceiver {

    public static final String DAY = "DAY";
    public static final String LESSON_INDEX = "LESSON_INDEX";
    private static final String LOG_TAG = "TimetableScheduleReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        AlarmManager service = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent defaultIntent = new Intent(context, TeacherAttendanceReceiver.class);
        // TODO: 12.11.2015 move part of functionality from TeacherAttendanceReceiver to TimetableNotificationReceiver
        PendingIntent defaultPending = PendingIntent.getBroadcast(context, 0,
                defaultIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        service.cancel(defaultPending);

        if ((context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, true)
                && activeNetInfo != null && activeNetInfo.isConnected()
                && (!context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_USE_ONLY_WIFI, false) || !((WifiManager) context
                .getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID().equals("<unknown ssid>")))
                || context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_LESSON_WARNINGS, false)
                || AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context,
                TimetableLessonWidget.class)).length > 0) {

            /*Calendar cal = Calendar.getInstance();
            // start 30 seconds after boot completed
            cal.add(Calendar.SECOND, Constants.WAIT_TIME_FIRST_REPEAT);
            // fetch every 30 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), Constants.REPEAT_TIME_TEACHERS_ATTENDANCE, defaultPending);*/

            Calendar calendar = Calendar.getInstance();
            Log.d(LOG_TAG, "onReceive startTime: " + calendar.getTime());
            for (int d = 0, days_strings_idsLength = Timetable.DAYS_STRINGS_IDS.length; d < days_strings_idsLength; d++) {
                int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                int day = calendar.get(Calendar.DAY_OF_WEEK);

                if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                    for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
                        int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i];
                        if (minuteTime < requestedTime - 10) {
                            defaultIntent.putExtra(DAY, day - 2).putExtra(LESSON_INDEX, i);
                            calendar.set(Calendar.HOUR_OF_DAY, Timetable.START_TIMES_HOURS[i]);
                            calendar.set(Calendar.MINUTE, Timetable.START_TIMES_MINUTES[i] - 10);
                            calendar.set(Calendar.MILLISECOND, 0);
                            service.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent
                                    .getBroadcast(context, 0, defaultIntent, PendingIntent.FLAG_CANCEL_CURRENT));
                            Log.d(LOG_TAG, "onReceive actualMinuteTime: " + minuteTime + " requestedMinuteTime: " + requestedTime
                                    + " setTime1:" + calendar.getTime() + " setTime2: " + calendar.getTimeInMillis()
                                    + " hour: " + calendar.get(Calendar.HOUR_OF_DAY) + " minute: " + calendar.get(Calendar.MINUTE)
                                    + " day1: " + (day - 2) + " day2: " + calendar.get(Calendar.DAY_OF_WEEK)
                                    + " millisecond: " + calendar.get(Calendar.MILLISECOND));
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
