package cz.anty.purkynkamanager.modules.timetable.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
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
        Intent attendanceIntent = new Intent(context, TeacherAttendanceReceiver.class);
        PendingIntent attendancePending = PendingIntent.getBroadcast(context, 0,
                attendanceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        service.cancel(attendancePending);

        Intent timetableIntent = new Intent(context, TimetableNotificationReceiver.class);
        PendingIntent timetablePending = PendingIntent.getBroadcast(context, 0,
                timetableIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        service.cancel(timetablePending);

        boolean startNotificationReceiver = context.getSharedPreferences(Constants
                .SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE).getBoolean(Constants
                .SETTING_NAME_DISPLAY_LESSON_WARNINGS, false) || AppWidgetManager
                .getInstance(context).getAppWidgetIds(new ComponentName(context,
                        TimetableLessonWidget.class)).length > 0;
        boolean startAttendanceReceiver = context.getSharedPreferences(Constants
                .SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE).getBoolean(Constants
                .SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, true)
                && Utils.isNetworkAvailable(context);
        if (startNotificationReceiver || startAttendanceReceiver) {

            /*Calendar cal = Calendar.getInstance();
            // start 30 seconds after boot completed
            cal.add(Calendar.SECOND, Constants.WAIT_TIME_FIRST_REPEAT);
            // fetch every 30 seconds
            // InexactRepeating allows Android to optimize the energy consumption
            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), Constants.REPEAT_TIME_TEACHERS_ATTENDANCE, attendancePending);*/

            Calendar calendar = Calendar.getInstance();
            Log.d(LOG_TAG, "onReceive startTime: " + calendar.getTime());
            for (int d = 0, days_strings_idsLength = Timetable.DAYS_STRINGS_IDS.length; d < days_strings_idsLength; d++) {
                int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                int day = calendar.get(Calendar.DAY_OF_WEEK);

                if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                    for (int i = 0; i < Timetable.MAX_LESSONS + 1; i++) {
                        int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i];
                        if (minuteTime < requestedTime - 10) {
                            attendanceIntent.putExtra(DAY, day - 2).putExtra(LESSON_INDEX, i);
                            timetableIntent.putExtra(DAY, day - 2).putExtra(LESSON_INDEX, i);

                            calendar.set(Calendar.HOUR_OF_DAY, Timetable.START_TIMES_HOURS[i]);
                            calendar.set(Calendar.MINUTE, Timetable.START_TIMES_MINUTES[i] - 10);
                            calendar.set(Calendar.MILLISECOND, 0);

                            if (startAttendanceReceiver && i != 0 && i < Timetable.MAX_LESSONS)
                                service.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent
                                        .getBroadcast(context, 0, attendanceIntent, PendingIntent.FLAG_CANCEL_CURRENT));

                            if (startNotificationReceiver) {
                                if (i == 3) {
                                    calendar.add(Calendar.MINUTE, -10);
                                    if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                        calendar.add(Calendar.MINUTE, 10);
                                        service.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent
                                                .getBroadcast(context, 0, new Intent(context, TimetableScheduleReceiver.class),
                                                        PendingIntent.FLAG_CANCEL_CURRENT));
                                        return;
                                    }
                                }
                                service.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), PendingIntent
                                        .getBroadcast(context, 0, timetableIntent, PendingIntent.FLAG_CANCEL_CURRENT));
                            }

                            /*Log.d(LOG_TAG, "onReceive actualMinuteTime: " + minuteTime + " requestedMinuteTime: " + requestedTime
                                    + " setTime1:" + calendar.getTime() + " setTime2: " + calendar.getTimeInMillis()
                                    + " hour: " + calendar.get(Calendar.HOUR_OF_DAY) + " minute: " + calendar.get(Calendar.MINUTE)
                                    + " day1: " + (day - 2) + " day2: " + calendar.get(Calendar.DAY_OF_WEEK)
                                    + " millisecond: " + calendar.get(Calendar.MILLISECOND));*/
                            //testSupplementation(context, day, i);
                            return;
                        }
                    }
                }

                calendar.add(Calendar.DAY_OF_WEEK, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            }

            // service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
            // REPEAT_TIME, attendancePending);
        }
    }
}
