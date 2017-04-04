package cz.anty.purkynkamanager.modules.sas.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.sas.SASManagerService;
import cz.anty.purkynkamanager.modules.sas.SASSplashActivity;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetMultilineAdapter;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetService;
import cz.anty.purkynkamanager.utils.other.sas.mark.Mark;
import cz.anty.purkynkamanager.utils.other.sas.mark.MarksManager;
import proguard.annotation.Keep;
import proguard.annotation.KeepName;

/**
 * Implementation of App Widget functionality.
 */
@Keep
@KeepName
public class SASManageWidget extends WidgetProvider {

    private static final String LOG_TAG = "SASManageWidget";
    private static final String REQUEST_UPDATE = "REQUEST_MARKS_UPDATE";

    public static void callUpdate(Context context, @Nullable String marks) {
        Log.d(LOG_TAG, "callUpdate");
        context.sendBroadcast(getUpdateIntent(context, marks, false));
    }

    private static Intent getUpdateIntent(Context context, @Nullable String marks,
                                          boolean requestUpdateMarks) {
        Log.d(LOG_TAG, "getUpdateIntent");
        return new Intent(context.getApplicationContext(), SASManageWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        getAllWidgetIds(context, SASManageWidget.class))
                .putExtra(WidgetMultilineAdapter.EXTRA_MARKS_AS_STRING, marks)
                .putExtra(REQUEST_UPDATE, requestUpdateMarks);
    }

    @Override
    protected boolean showAsEmpty(Context context, int[] appWidgetIds) {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.SAS);
    }

    @Override
    protected CharSequence getTitle(Context context, int[] appWidgetIds) {
        return context.getText(R.string.app_name_sas);
    }

    @Override
    protected int getTopColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.colorPrimaryS);
    }

    @Override
    protected int getBackgroundColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.navigationBarColorS);
    }

    @Override
    protected PendingIntent getRefreshPendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getBroadcast(context, 0, getUpdateIntent(context, null, true), 0);
    }

    @Override
    protected PendingIntent getTitlePendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getActivity(context, 0,
                new Intent(context, SASSplashActivity.class), 0);
    }

    @Override
    protected Intent getListWidgetServiceIntent(Context context, int[] appWidgetIds) {
        String marks = getLastIntent().getStringExtra(WidgetMultilineAdapter.EXTRA_MARKS_AS_STRING);
        if (marks == null) marks = new MarksManager(context).toString();
        return new Intent(context, WidgetService.class)
                .putExtra(WidgetMultilineAdapter.EXTRA_MARKS_AS_STRING, marks);
    }

    @Override
    protected MultilineItem[] getListItems(Context context, int[] appWidgetIds) {
        String marks = getLastIntent().getStringExtra(WidgetMultilineAdapter.EXTRA_MARKS_AS_STRING);
        if (marks != null) {
            List<Mark> markList = MarksManager.parseMarks(marks);
            return markList.toArray(new MultilineItem[markList.size()]);
        }
        return new MarksManager(context).get(MarksManager.Semester.AUTO);
    }

    @Override
    protected boolean onStartLoading(Context context, int[] appWidgetIds) {
        if (getLastIntent().getBooleanExtra(REQUEST_UPDATE, false) &&
                AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            context.startService(new Intent(context, SASManagerService.class)
                    .putExtra(SASManagerService.FORCE_UPDATE_WIDGET, true));
            return false;
        }
        return true;
    }

    /*@SuppressLint("NewApi")
    @Override
    public synchronized void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");
        appWidgetManager.updateAppWidget(appWidgetIds, new RemoteViews(
                context.getPackageName(), R.layout.widget_sasmanage_loading));

        if (lastIntent.getBooleanExtra(REQUEST_UPDATE, false) &&
                AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            context.startService(new Intent(context, SASManagerService.class)
                    .putExtra(SASManagerService.FORCE_UPDATE_WIDGET, true));
            return;
        }

        String marks = lastIntent.getStringExtra(WidgetMultilineAdapter.EXTRA_MARKS_AS_STRING);
        marks = marks == null ? updateMarks(context) : marks;

        //Log.d("UPDATE", "onUpdate creating remote views");
        //which layout to show on widget
        RemoteViews remoteViews;

        if (Build.VERSION.SDK_INT >= 11) {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.widget_sasmanage_new);
        } else {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.widget_sasmanage_old);
        }

        //Log.d("UPDATE", "onUpdate setting onClick listeners");
        remoteViews.setOnClickPendingIntent(R.id.image_button_refresh,
                PendingIntent.getBroadcast(context, 0, getUpdateIntent(context, null, true), 0));

        remoteViews.setOnClickPendingIntent(R.id.relative_layout_widget_main,
                PendingIntent.getActivity(context, 0,
                        new Intent(context, SASSplashActivity.class), 0));

        //Log.d("UPDATE", "onUpdate checking if is logged in");
        if (!AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
            //Log.d("UPDATE", "onUpdate not logged in");
            remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 11) {
            //Log.d("UPDATE", "onUpdate loading new version view");
            //remoteViews.setViewVisibility(R.id.list_view_marks, View.VISIBLE);
            //RemoteViews Service needed to provide adapter for ListView
            Intent svcIntent = new Intent(context, WidgetService.class);
            //passing app widget id to that RemoteViews Service
            //svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.putExtra(WidgetMultilineAdapter.EXTRA_MARKS_AS_STRING, marks);
            //setting a unique Uri to the intent
            //don't know its purpose to me right now
            svcIntent.setData(Uri.parse(
                    svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
            //setting adapter to listView of the widget
            if (Build.VERSION.SDK_INT >= 14)
                remoteViews.setRemoteAdapter(R.id.list_view_marks, svcIntent);
            else {
                for (int appWidgetId : appWidgetIds) {
                    //noinspection deprecation
                    remoteViews.setRemoteAdapter(appWidgetId, R.id.list_view_marks, svcIntent);
                }
            }

            //setting an empty view in case of no data
            remoteViews.setEmptyView(R.id.list_view_marks, R.id.empty_view);
        } else {
            //Log.d("UPDATE", "onUpdate loading old version view");
            List<Mark> itemList = MarksManager.parseMarks(marks);
            if (itemList.isEmpty())
                remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
            else {
                remoteViews.setViewVisibility(R.id.widget_main_layout, View.VISIBLE);
                //remoteViews.removeAllViews(R.id.widget_main_layout);
                int len = itemList.size();
                len = len > 7 ? 7 : len;
                for (int i = 0; i < len; i++) {
                    MultilineItem multilineItem = itemList.get(i);
                    RemoteViews itemRemoteViews = new RemoteViews(
                            context.getPackageName(), R.layout.widget_list_item_multi_line_text);
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_title, multilineItem.getTitle(context, i));
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_text, multilineItem.getText(context, i));
                    remoteViews.addView(R.id.widget_main_layout, itemRemoteViews);
                }
            }
            //remoteViews.setViewVisibility(R.id.linear_layout_marks, View.VISIBLE);
                    *//*MultilineItem[] items = (MultilineItem[]) MarksManager.parseMarks(marks).toArray();
                    int len = items.length > 10 ? 10 : items.length;
                    for (int i = 0; i < len; i++) {

                    }*//*
        }

        //Log.d("UPDATE", "onUpdate updating widget");
        // Instruct the widget manager to update the widget
        //ComponentName thisWidget = new ComponentName(context, SASManageWidget.class);
        //appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        //Log.d("UPDATE", "onUpdate exiting");
    }

    private String updateMarks(Context context) {
        Log.d(LOG_TAG, "updateMarks");
        return new MarksManager(context).toString();
    }*/
}

