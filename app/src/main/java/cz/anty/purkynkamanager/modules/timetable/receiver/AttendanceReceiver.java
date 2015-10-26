package cz.anty.purkynkamanager.modules.timetable.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.List;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.TimetableManageActivity;
import cz.anty.purkynkamanager.modules.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.attendance.AttendanceConnector;
import cz.anty.purkynkamanager.utils.other.attendance.man.Man;
import cz.anty.purkynkamanager.utils.other.attendance.man.Mans;
import cz.anty.purkynkamanager.utils.other.teacher.Teacher;
import cz.anty.purkynkamanager.utils.other.teacher.TeachersManager;
import cz.anty.purkynkamanager.utils.other.timetable.Lesson;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableManager;

public class AttendanceReceiver extends BroadcastReceiver {

    public static final String DAY = "DAY";
    public static final String LESSON_INDEX = "LESSON_INDEX";
    private static final String LOG_TAG = "AttendanceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        TimetableLessonWidget.callUpdate(context);

        int day = intent.getIntExtra(DAY, -1);
        int lessonIndex = intent.getIntExtra(LESSON_INDEX, -1);
        if (day != -1 && lessonIndex != -1) {
            if (context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                    .getBoolean(Constants.SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, false)) {
                testSupplementation(context, day, lessonIndex);
            }
            if (context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE)
                    .getBoolean(Constants.SETTING_NAME_DISPLAY_LESSON_WARNINGS, false)) {
                showLessonNotification(context, day, lessonIndex);
            }
        }

        context.sendBroadcast(new Intent(context, TimetableScheduleReceiver.class));
    }

    private void showLessonNotification(Context context, int day, int lessonIndex) {
        Log.d(LOG_TAG, "showLessonNotification day: " + day + " lessonIndex: " + lessonIndex);
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(context);
        Timetable[] timetables = TimetableSelectActivity.timetableManager.getTimetables();

        int i = 0;
        for (Timetable timetable : timetables) {
            Lesson lesson = timetable.getLesson(day, lessonIndex);
            if (lesson == null) continue;

            Notification n = new NotificationCompat.Builder(context)
                    .setContentTitle(lesson.getTitle(context, lessonIndex))
                    .setContentText(lesson.getText(context, lessonIndex))
                    .setSmallIcon(R.mipmap.ic_launcher_t)
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                            new Intent(context, TimetableManageActivity.class).putExtra(
                                    TimetableManageActivity.EXTRA_TIMETABLE_NAME, timetable.getName()), 0))
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                            //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                    .build();

            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(Constants.NOTIFICATION_ID_TIMETABLE_LESSON + i, n);

            if (i < 10) i++;
            else return;
        }

    }

    private void testSupplementation(final Context context, final int day, final int lessonIndex) {
        Log.d(LOG_TAG, "testSupplementation day: " + day + " lessonIndex: " + lessonIndex);
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(context);
        final Timetable[] timetables = TimetableSelectActivity.timetableManager.getTimetables();
        final AttendanceConnector connector = new AttendanceConnector();

        ApplicationBase.WORKER.startWorker(new Runnable() {
            @Override
            public void run() {
                for (int i = 0, timetablesLength = timetables.length; i < timetablesLength; i++) {
                    final Timetable timetable = timetables[i];
                    if (context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLE_ATTENDANCE, Context.MODE_PRIVATE)
                            .getLong(timetable.getName() + Constants.SETTING_NAME_ADD_LAST_NOTIFY, 0) +
                            Constants.WAIT_TIME_TEACHERS_ATTENDANCE > System.currentTimeMillis())
                        continue;
                    final Lesson lesson = timetable.getLesson(day, lessonIndex);
                    if (lesson == null) continue;

                    final int finalI = i < 10 ? i : 9;
                    try {
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
                        for (int j = 0; j < mans.size(); j++) {
                            man = mans.get(j);
                            if (man.getClassString().length() > 4) break;
                            else man = null;
                        }
                        if (man != null && Man.IsInSchoolState.NOT_IN_SCHOOL.equals(man.isInSchool())) {
                            Notification n = new NotificationCompat.Builder(context)
                                    .setContentTitle(String.format(context.getString(R.string
                                            .notify_title_substitution), lesson.getShortName()))
                                    .setContentText(String.format(context.getString(R.string
                                            .notify_text_teacher_is_not_here), man.getName()))
                                    .setSmallIcon(R.mipmap.ic_launcher_t)
                                    .setContentIntent(PendingIntent.getActivity(context, 0,
                                            new Intent(context, TimetableManageActivity.class).putExtra(
                                                    TimetableManageActivity.EXTRA_TIMETABLE_NAME, timetable.getName()), 0))
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                            //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                                    .build();

                            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                                    .notify(Constants.NOTIFICATION_ID_TEACHERS_ATTENDANCE + finalI, n);

                            context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLE_ATTENDANCE,
                                    Context.MODE_PRIVATE).edit().putLong(timetable.getName()
                                    + Constants.SETTING_NAME_ADD_LAST_NOTIFY, System.currentTimeMillis())
                                    .apply();
                        }
                    } catch (IOException | IndexOutOfBoundsException e) {
                        Log.d("AttendanceReceiver", "testSupplementation", e);
                    }
                }
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);
    }
}
