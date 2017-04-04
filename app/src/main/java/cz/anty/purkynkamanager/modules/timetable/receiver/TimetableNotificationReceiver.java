package cz.anty.purkynkamanager.modules.timetable.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.TimetableManageActivity;
import cz.anty.purkynkamanager.modules.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.modules.timetable.widget.TimetableLessonWidget;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.timetable.Lesson;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableManager;

/**
 * Created by anty on 11.11.2015.
 *
 * @author anty
 */
public class TimetableNotificationReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "TimetableNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        TimetableLessonWidget.callUpdate(context);

        int day = intent.getIntExtra(TimetableScheduleReceiver.DAY, -1);
        int lessonIndex = intent.getIntExtra(TimetableScheduleReceiver.LESSON_INDEX, -1);
        if (day != -1 && lessonIndex != -1 && context.getSharedPreferences(Constants
                .SETTINGS_NAME_TIMETABLES, Context.MODE_PRIVATE).getBoolean(Constants
                .SETTING_NAME_DISPLAY_LESSON_WARNINGS, false)) {
            showLessonNotifications(context, day, lessonIndex);
        }

        context.sendBroadcast(new Intent(context, TimetableScheduleReceiver.class));
    }

    private void showLessonNotifications(Context context, int day, int lessonIndex) {
        Log.d(LOG_TAG, "showLessonNotification day: " + day + " lessonIndex: " + lessonIndex);
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(context);
        Timetable[] timetables = TimetableSelectActivity.timetableManager.getTimetables();

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < 10; i++)
            notificationManager.cancel(Constants.NOTIFICATION_ID_TIMETABLE_LESSON + i);

        int i = -1;
        for (Timetable timetable : timetables) {
            i++;
            if (i >= 10) return;

            Lesson lesson = null;
            if (lessonIndex < Timetable.MAX_LESSONS)
                lesson = timetable.getLesson(day, lessonIndex);

            CharSequence title, text;

            if (lesson == null) {
                if (lessonIndex < Timetable.MAX_LESSONS - 1 && lessonIndex > 0 &&
                        timetable.getLesson(day, lessonIndex + 1) != null &&
                        timetable.getLesson(day, lessonIndex - 1) != null) {
                    title = context.getText(R.string.list_item_text_no_actual_lesson);
                    text = Lesson.getTimeString(lessonIndex);
                } else continue;
            } else {
                title = lesson.getTitle(context, lessonIndex);
                text = lesson.getText(context, lessonIndex);
            }

            Notification n = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher_t_no_border)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                            new Intent(context, TimetableManageActivity.class).putExtra(
                                    TimetableManageActivity.EXTRA_TIMETABLE_NAME, timetable.getName()), 0))
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .build();

            notificationManager.notify(Constants.NOTIFICATION_ID_TIMETABLE_LESSON + i, n);
        }
    }
}
