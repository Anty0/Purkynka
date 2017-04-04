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
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.PostNotificationCanceler;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.attendance.AttendanceConnector;
import cz.anty.purkynkamanager.utils.other.attendance.man.Man;
import cz.anty.purkynkamanager.utils.other.attendance.man.Mans;
import cz.anty.purkynkamanager.utils.other.teacher.Teacher;
import cz.anty.purkynkamanager.utils.other.teacher.TeachersManager;
import cz.anty.purkynkamanager.utils.other.timetable.Lesson;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableManager;

public class TeacherAttendanceReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "TeacherAttendanceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        int day = intent.getIntExtra(TimetableScheduleReceiver.DAY, -1);
        int lessonIndex = intent.getIntExtra(TimetableScheduleReceiver.LESSON_INDEX, -1);
        if (day != -1 && lessonIndex != -1 && context.getSharedPreferences(Constants
                .SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE).getBoolean(Constants
                .SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS, true)) {
            testSupplementation(context, day, lessonIndex);
        }

        context.sendBroadcast(new Intent(context, TimetableScheduleReceiver.class));
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
                                    .setContentTitle(Utils.getFormattedText(context, R.string
                                            .notify_title_substitution, lesson.getShortName()))
                                    .setContentText(Utils.getFormattedText(context, R.string
                                            .notify_text_teacher_is_not_here, man.getName()))
                                    .setSmallIcon(R.mipmap.ic_launcher_t_no_border)
                                    .setContentIntent(PendingIntent.getActivity(context, 0,
                                            new Intent(context, TimetableManageActivity.class).putExtra(
                                                    TimetableManageActivity.EXTRA_TIMETABLE_NAME, timetable.getName()), 0))
                                    .setAutoCancel(true)
                                    .setDefaults(Notification.DEFAULT_ALL)
                                            //.addAction(R.mipmap.ic_launcher, "And more", pIntent)
                                    .build();

                            int notificationId = Constants.NOTIFICATION_ID_TEACHERS_ATTENDANCE + finalI;
                            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                                    .notify(notificationId, n);

                            PostNotificationCanceler.postNotificationCancel(context,
                                    notificationId, Constants.WAIT_TIME_TEACHERS_ATTENDANCE);
                        }
                    } catch (IOException | IndexOutOfBoundsException e) {
                        Log.d("TeacherAttendanceReceiver", "testSupplementation", e);
                    }
                }
            }
        }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);
    }
}
