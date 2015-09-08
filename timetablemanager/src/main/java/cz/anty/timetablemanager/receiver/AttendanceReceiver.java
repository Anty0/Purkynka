package cz.anty.timetablemanager.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

import cz.anty.timetablemanager.R;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.attendance.AttendanceConnector;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.Mans;
import cz.anty.utils.teacher.Teacher;
import cz.anty.utils.teacher.TeachersManager;
import cz.anty.utils.thread.OnceRunThread;
import cz.anty.utils.timetable.Lesson;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableManager;

public class AttendanceReceiver extends BroadcastReceiver {

    public static final String DAY = "DAY";
    public static final String LESSON_INDEX = "LESSON_INDEX";

    private final OnceRunThread worker = new OnceRunThread();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AttendanceReceiver", "onReceive");
        int day = intent.getIntExtra(DAY, -1);
        int lessonIndex = intent.getIntExtra(LESSON_INDEX, -1);
        if (day != -1 && lessonIndex != -1 && context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, false)) {
            testSupplementation(context, day, lessonIndex);
        }

        context.sendBroadcast(new Intent(context, TimetableScheduleReceiver.class));
    }

    private void testSupplementation(final Context context, final int day, final int lessonIndex) {
        Log.d("AttendanceReceiver", "testSupplementation");
        Timetable[] timetables = new TimetableManager(context).getTimetables();
        final AttendanceConnector connector = new AttendanceConnector();
        for (final Timetable timetable : timetables) {
            if (context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLE_ATTENDANCE, Context.MODE_PRIVATE)
                    .getLong(timetable.getName() + Constants.SETTING_NAME_ADD_LAST_NOTIFY, 0) +
                    Constants.WAIT_TIME_TEACHERS_ATTENDANCE > System.currentTimeMillis())
                continue;
            worker.setPowerManager(context);
            worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    try {
                        Lesson lesson = timetable.getLesson(day, lessonIndex);
                        if (lesson == null) return;
                        String teacherName = lesson.getTeacher();
                        if (teacherName.length() != 4) {
                            String[] name = teacherName.split(" ");
                            teacherName = name[name.length - 1];
                        } else {
                            teacherName = teacherName.toLowerCase();
                            TeachersManager teachersManager = new TeachersManager(context);
                            for (Teacher teacher : teachersManager.get()) {
                                if (teacher.getShortcut().equals(teacherName)) {
                                    teacherName = teacher.getSurname();
                                    break;
                                }
                            }
                        }
                        List<Man> mans = Mans.parseMans(
                                connector.getSupElements(teacherName, 1));
                        Man man = null;
                        for (int i = 0; i < mans.size(); i++) {
                            man = mans.get(i);
                            if (man.getClassString().length() > 4) break;
                            else man = null;
                        }
                        if (man != null && Man.IsInSchoolState.IS_NOT_IN_SCHOOL.equals(man.isInSchool())) {
                            Notification n = new NotificationCompat.Builder(context)
                                    .setContentTitle(context.getString(R.string.notify_title_substitution)
                                            .replace(Constants.STRINGS_CONST_NAME, lesson.getShortName()))
                                    .setContentText(context.getString(R.string.notify_text_teacher_is_not_here)
                                            .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                                    .setSmallIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon T
                                    .setContentIntent(null)
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                            //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                                    .build();

                            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(Constants.NOTIFICATION_ID_TEACHERS_ATTENDANCE, n);
                            context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLE_ATTENDANCE, Context.MODE_PRIVATE).edit()
                                    .putLong(timetable.getName() + Constants.SETTING_NAME_ADD_LAST_NOTIFY, System.currentTimeMillis()).apply();
                        }
                    } catch (IOException | IndexOutOfBoundsException e) {
                        Log.d("AttendanceReceiver", "testSupplementation", e);
                    }
                }
            });
        }
    }
}
