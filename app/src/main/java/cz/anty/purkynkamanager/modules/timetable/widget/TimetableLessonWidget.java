package cz.anty.purkynkamanager.modules.timetable.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.timetable.TimetableManageActivity;
import cz.anty.purkynkamanager.modules.timetable.TimetableSelectActivity;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;
import cz.anty.purkynkamanager.utils.other.timetable.Lesson;
import cz.anty.purkynkamanager.utils.other.timetable.Timetable;
import cz.anty.purkynkamanager.utils.other.timetable.TimetableManager;
import proguard.annotation.Keep;
import proguard.annotation.KeepName;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link TimetableLessonWidgetConfigureActivity TimetableLessonWidgetConfigureActivity}
 */
@Keep
@KeepName
public class TimetableLessonWidget extends WidgetProvider {

    public static final String EXTRA_TIMETABLE_NAME = "TIMETABLE_NAME";
    private Timetable actualTimetable = null;

    public static void callUpdate(Context context) {
        context.sendBroadcast(getUpdateIntent(context));
    }

    private static Intent getUpdateIntent(Context context) {
        return new Intent(context.getApplicationContext(), TimetableLessonWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        getAllWidgetIds(context, TimetableLessonWidget.class));
    }

    @Override
    protected boolean hasDifferentWidgets() {
        return true;
    }

    @Override
    protected int getTopVisibility(Context context, int[] appWidgetIds) {
        return View.GONE;
    }

    @Override
    protected CharSequence getTitle(Context context, int[] appWidgetIds) {
        CharSequence toReturn = context.getText(R.string.activity_title_timetable_manage);
        if (actualTimetable != null)
            toReturn = actualTimetable.getName() + " - " + toReturn;
        return toReturn;
    }

    @Override
    protected int getTopColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.colorPrimaryT);
    }

    @Override
    protected int getBackgroundColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.navigationBarColorT);
    }

    @Override
    protected PendingIntent getRefreshPendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getBroadcast(context, 0, getUpdateIntent(context), 0);
    }

    @Override
    protected PendingIntent getTitlePendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getActivity(context, 0,
                new Intent(context, TimetableManageActivity.class).putExtra(TimetableManageActivity
                        .EXTRA_TIMETABLE_NAME, actualTimetable == null ? null : actualTimetable.getName()), 0);
    }

    @Override
    protected boolean onStartLoading(Context context, int[] appWidgetIds) {
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(context);
        return true;
    }

    @Override
    protected boolean onStartUpdate(Context context, int[] appWidgetIds) {
        String timetableName = getLastIntent().getStringExtra(EXTRA_TIMETABLE_NAME);
        actualTimetable = TimetableSelectActivity.timetableManager
                .getTimetableByName(timetableName != null ? timetableName :
                        TimetableLessonWidgetConfigureActivity
                                .loadPref(context, appWidgetIds[0]));
        return true;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        actualTimetable = null;
    }

    @Override
    protected void setRemoteAdapter(Context context, int[] appWidgetIds, RemoteViews remoteViews) {

    }

    @Override
    protected RemoteViews getDataContent(Context context, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context
                .getPackageName(), R.layout.widget_content_timetable_lesson);
        PendingIntent onClickIntent = getTitlePendingIntent(context, appWidgetIds);
        remoteViews.setOnClickPendingIntent(R.id.list_item_actual, onClickIntent);
        remoteViews.setOnClickPendingIntent(R.id.list_item_next, onClickIntent);

        if (actualTimetable == null) {
            remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        } else {
            remoteViews.setViewVisibility(R.id.main_relative_layout, View.VISIBLE);
            initTexts(context, actualTimetable, remoteViews);
        }

        return remoteViews;
    }

    /*@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        appWidgetManager.updateAppWidget(appWidgetIds, new RemoteViews(
                context.getPackageName(), R.layout.widget_timetable_lesson_loading));
        // There may be multiple widgets active, so update all of them
        if (TimetableSelectActivity.timetableManager == null)
            TimetableSelectActivity.timetableManager = new TimetableManager(context);

        for (int appWidgetId : appWidgetIds) {
            Timetable timetable = TimetableSelectActivity.timetableManager
                    .getTimetableByName(TimetableLessonWidgetConfigureActivity
                            .loadPref(context, appWidgetId));

            RemoteViews remoteViews = new RemoteViews(context
                    .getPackageName(), R.layout.widget_content_timetable_lesson);
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
    }*/

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
                                minuteTime < requestedTime - 55) {
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
}

