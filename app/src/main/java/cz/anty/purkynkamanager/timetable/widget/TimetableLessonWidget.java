package cz.anty.purkynkamanager.timetable.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.timetable.TimetableManageActivity;
import cz.anty.purkynkamanager.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.utils.timetable.Lesson;
import cz.anty.purkynkamanager.utils.timetable.Timetable;
import cz.anty.purkynkamanager.utils.timetable.TimetableManager;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TimetableLessonWidgetConfigureActivity TimetableLessonWidgetConfigureActivity}
 */
public class TimetableLessonWidget extends AppWidgetProvider {

    public static void callUpdate(Context context) {
        context.sendBroadcast(getUpdateIntent(context));
    }

    private static Intent getUpdateIntent(Context context) {
        int[] allWidgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, TimetableLessonWidget.class));

        return new Intent(context.getApplicationContext(),
                TimetableLessonWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetManager.updateAppWidget(appWidgetIds, new RemoteViews(
                context.getPackageName(), R.layout.timetable_lesson_widget_loading));
        // There may be multiple widgets active, so update all of them
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(context);

        for (int appWidgetId : appWidgetIds) {
            Timetable timetable = TimetableSelectActivity.timetableManager
                    .getTimetableByName(TimetableLessonWidgetConfigureActivity
                            .loadPref(context, appWidgetId));

            RemoteViews remoteViews = new RemoteViews(context
                    .getPackageName(), R.layout.timetable_lesson_widget);
            remoteViews.setOnClickPendingIntent(R.id.main_frame_layout, PendingIntent
                    .getActivity(context, 0, new Intent(context, TimetableManageActivity.class).putExtra(TimetableManageActivity
                            .EXTRA_TIMETABLE_NAME, timetable == null ? null : timetable.getName()), 0));

            if (timetable == null) {
                remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
            } else {
                remoteViews.setViewVisibility(R.id.main_relative_layout, View.VISIBLE);
                initTexts(context, timetable, remoteViews);
            }

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }

    private void initTexts(Context context, Timetable timetable, RemoteViews remoteViews) {
        Calendar calendar = Calendar.getInstance();
        for (int d = 0; d < 7; d++) {
            int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
                    int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i] + 45;
                    if (minuteTime < requestedTime) {
                        Lesson actualLesson = timetable.getLesson(day - 2, i);
                        Lesson nextLesson = timetable.getNextLesson(day - 2, i);

                        if (actualLesson != null &&
                                minuteTime < requestedTime - 60) {
                            nextLesson = actualLesson;
                            actualLesson = null;
                        }

                        if (actualLesson == null) {
                            remoteViews.setTextViewText(R.id.widget_text_view_title, context.getText(R.string.list_item_text_no_actual_lesson));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                remoteViews.setViewPadding(R.id.widget_text_view_title, 1, 8, 1, 8);
                            remoteViews.setViewVisibility(R.id.widget_text_view_text, View.GONE);
                        } else {
                            remoteViews.setTextViewText(R.id.widget_text_view_title, actualLesson.getTitle(context, i));
                            remoteViews.setTextViewText(R.id.widget_text_view_text, actualLesson.getText(context, i));
                        }

                        if (nextLesson == null) {
                            remoteViews.setTextViewText(R.id.text_view_title, context.getText(R.string.list_item_text_no_next_lesson));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                remoteViews.setViewPadding(R.id.text_view_title, 1, 8, 1, 8);
                            remoteViews.setViewVisibility(R.id.text_view_text, View.GONE);
                        } else {
                            int index = timetable.getLessonIndex(nextLesson);
                            remoteViews.setTextViewText(R.id.text_view_title, nextLesson.getTitle(context, index));
                            remoteViews.setTextViewText(R.id.text_view_text, nextLesson.getText(context, index));
                        }
                        return;
                    }
                }
            }

            calendar.add(Calendar.DAY_OF_WEEK, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            TimetableLessonWidgetConfigureActivity
                    .deleteTitlePref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

