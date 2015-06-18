package cz.anty.attendancemanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.OnceRunThread;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.Mans;
import cz.anty.utils.timetable.Lesson;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableManager;

public class AttendanceReceiver extends BroadcastReceiver {

    private static final long WAIT_TIME = 1000 * 60 * 15;

    private OnceRunThread worker = new OnceRunThread();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!context.getSharedPreferences("AttendanceData", Context.MODE_PRIVATE).getBoolean("DISPLAY_WARNING", false))
            return;

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int minuteTime = calendar.get(Calendar.HOUR) * 60 + calendar.get(Calendar.MINUTE);
        for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
            int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i];
            if (minuteTime < requestedTime && minuteTime > requestedTime - 15) {
                int day = calendar.get(Calendar.DAY_OF_WEEK) - 2;
                testSupplementation(context, day, i);
                break;
            }
        }
    }

    private void testSupplementation(final Context context, final int day, final int lessonIndex) {
        Timetable[] timetables = new TimetableManager(context).getTimetables();
        final AttendanceConnector connector = new AttendanceConnector();
        for (final Timetable timetable : timetables) {
            if (context.getSharedPreferences("ATTENDANCE", Context.MODE_PRIVATE).getLong(timetable.getName() + " LAST_NOTIFY", 0) + WAIT_TIME > System.currentTimeMillis())
                continue;
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    try {
                        Lesson lesson = timetable.getLesson(day, lessonIndex);
                        if (lesson == null) return;
                        List<Man> mans = Mans.parseMans(
                                connector.getSupElements(lesson.getTeacher(), 1));
                        Man man = null;
                        for (int i = 0; i < mans.size(); i++) {
                            man = mans.get(i);
                            if (man.getClassString().length() > 4) break;
                            else man = null;
                        }
                        if (man != null && !man.isInSchool()) {
                            Notification n = new NotificationCompat.Builder(context)
                                    .setContentTitle(lesson.getShortName() + " " + context.getString(R.string.notify_supplementation_title))
                                    .setContentText(man.getName() + " " + context.getString(R.string.notify_teacher_is_not_here))
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                            //.setContentIntent(null)
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                            //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                                    .build();

                            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(2, n);
                            context.getSharedPreferences("ATTENDANCE", Context.MODE_PRIVATE).edit()
                                    .putLong(timetable.getName() + " LAST_NOTIFY", System.currentTimeMillis()).apply();
                        }
                    } catch (IOException | IndexOutOfBoundsException | URISyntaxException e) {
                        Log.d(null, null, e);
                    }
                }
            });
        }
    }
}
