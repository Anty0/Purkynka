package cz.anty.timetablemanager.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Locale;

import cz.anty.timetablemanager.R;
import cz.anty.utils.timetable.Lesson;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableManager;

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
        for (int appWidgetId : appWidgetIds) {
            Timetable timetable = new TimetableManager(context)
                    .getTimetableByName(TimetableLessonWidgetConfigureActivity
                            .loadPref(context, appWidgetId));

            RemoteViews remoteViews = new RemoteViews(context
                    .getPackageName(), R.layout.timetable_lesson_widget);

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
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        for (int d = 0; d < 7; d++) {
            int minuteTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            int day = calendar.get(Calendar.DAY_OF_WEEK);

            if (day != Calendar.SUNDAY && day != Calendar.SATURDAY) {
                for (int i = 0; i < Timetable.MAX_LESSONS; i++) {
                    int requestedTime = Timetable.START_TIMES_HOURS[i] * 60 + Timetable.START_TIMES_MINUTES[i];
                    if (minuteTime < requestedTime) {
                        Lesson actualLesson = timetable.getLesson(day - 2, i);
                        Lesson nextLesson = timetable.getNextLesson(day - 2, i);

                        if (actualLesson == null) {
                            remoteViews.setTextViewText(R.id.widget_text_view_title, context.getString(R.string.list_item_text_no_actual_lesson));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                remoteViews.setViewPadding(R.id.widget_text_view_title, 1, 8, 1, 8);
                            remoteViews.setViewVisibility(R.id.widget_text_view_text, View.GONE);
                        } else {
                            remoteViews.setTextViewText(R.id.widget_text_view_title, actualLesson.getTitle(context, i));
                            remoteViews.setTextViewText(R.id.widget_text_view_text, actualLesson.getText(context, i));
                        }

                        if (nextLesson == null) {
                            remoteViews.setTextViewText(R.id.text_view_title, context.getString(R.string.list_item_text_no_next_lesson));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                remoteViews.setViewPadding(R.id.text_view_title, 1, 8, 1, 8);
                            remoteViews.setViewVisibility(R.id.text_view_text, View.GONE);
                        } else {
                            remoteViews.setTextViewText(R.id.text_view_title, nextLesson.getTitle(context, i + 1).substring(3));
                            remoteViews.setTextViewText(R.id.text_view_text, nextLesson.getText(context, i + 1).substring(3));
                        }
                        return;
                    }
                }
            }

            calendar.add(Calendar.DAY_OF_WEEK, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
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

