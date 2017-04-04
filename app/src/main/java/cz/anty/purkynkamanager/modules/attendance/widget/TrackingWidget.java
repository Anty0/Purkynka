package cz.anty.purkynkamanager.modules.attendance.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.attendance.TrackingActivity;
import cz.anty.purkynkamanager.modules.attendance.receiver.TrackingReceiver;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetMultilineAdapter;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetService;
import proguard.annotation.Keep;
import proguard.annotation.KeepName;

/**
 * Implementation of App Widget functionality.
 */
@Keep
@KeepName
public class TrackingWidget extends WidgetProvider {

    private static final String LOG_TAG = "TrackingWidget";
    private static final String EXTRA_REFRESH_MANS = "REFRESH_MANS";

    public static void callUpdate(Context context, boolean refreshMans) {
        Log.d(LOG_TAG, "callUpdate");
        context.sendBroadcast(getUpdateIntent(context, refreshMans));
    }

    private static Intent getUpdateIntent(Context context, boolean refreshMans) {
        Log.d(LOG_TAG, "getUpdateIntent");
        return new Intent(context.getApplicationContext(), TrackingWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        getAllWidgetIds(context, TrackingWidget.class))
                .putExtra(EXTRA_REFRESH_MANS, refreshMans);
    }

    @Override
    protected boolean showAsEmpty(Context context, int[] appWidgetIds) {
        return !context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_ITEM_UNLOCKED_BONUS, false);
    }

    @Override
    protected CharSequence getTitle(Context context, int[] appWidgetIds) {
        return context.getText(R.string.activity_title_tracking);
    }

    @Override
    protected int getTopColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.colorPrimaryA);
    }

    @Override
    protected int getBackgroundColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.navigationBarColorA);
    }

    @Override
    protected PendingIntent getRefreshPendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getBroadcast(context, 0, getUpdateIntent(context, true), 0);
    }

    @Override
    protected PendingIntent getTitlePendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getActivity(context, 0,
                new Intent(context, TrackingActivity.class), 0);
    }

    @Override
    protected Intent getListWidgetServiceIntent(Context context, int[] appWidgetIds) {
        return new Intent(context, WidgetService.class)
                .putExtra(WidgetMultilineAdapter.EXTRA_MANS_AS_STRING,
                        TrackingActivity.mansManager.toString());
    }

    @Override
    protected MultilineItem[] getListItems(Context context, int[] appWidgetIds) {
        return TrackingActivity.mansManager.get();
    }

    @Override
    protected boolean onStartLoading(final Context context, int[] appWidgetIds) {
        if (getLastIntent().getBooleanExtra(EXTRA_REFRESH_MANS, false)) {
            ApplicationBase.WORKER.startWorker(new Runnable() {
                @Override
                public void run() {
                    TrackingActivity.mansManager = TrackingReceiver
                            .refreshTrackingMans(context, TrackingActivity.mansManager, false);
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callUpdate(context, false);
                        }
                    });
                }
            }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);
            return false;
        }
        if (TrackingActivity.mansManager == null)
            TrackingActivity.mansManager = new
                    TrackingMansManager(context);
        return true;
    }

    /*@SuppressLint("NewApi")
    @Override
    public synchronized void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");
        appWidgetManager.updateAppWidget(appWidgetIds, new RemoteViews(
                context.getPackageName(), R.layout.widget_tracking_loading));

        if (lastIntent.getBooleanExtra(EXTRA_REFRESH_MANS, true)) {
            ApplicationBase.WORKER.startWorker(new Runnable() {
                @Override
                public void run() {
                    TrackingActivity.mansManager = TrackingReceiver
                            .refreshTrackingMans(context, TrackingActivity.mansManager, false);
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateWidget(context, appWidgetManager, appWidgetIds);
                        }
                    });
                }
            }, Build.VERSION.SDK_INT >= 11 ? goAsync() : null);
            return;
        }
        if (TrackingActivity.mansManager == null)
            TrackingActivity.mansManager = new
                    TrackingMansManager(context);
        updateWidget(context, appWidgetManager, appWidgetIds);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //Log.d("UPDATE", "onUpdate creating remote views");
        //which layout to show on widget
        RemoteViews remoteViews;

        if (Build.VERSION.SDK_INT >= 11) {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.widget_tracking_new);
        } else {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.widget_tracking_old);
        }


        //Log.d("UPDATE", "onUpdate setting onClick listeners");
        remoteViews.setOnClickPendingIntent(R.id.image_button_refresh,
                PendingIntent.getBroadcast(context, 0, getUpdateIntent(context, true), 0));

        remoteViews.setOnClickPendingIntent(R.id.relative_layout_widget_main,
                PendingIntent.getActivity(context, 0,
                        new Intent(context, TrackingActivity.class), 0));

        //Log.d("UPDATE", "onUpdate checking if is logged in");
        if (Build.VERSION.SDK_INT >= 11) {
            //Log.d("UPDATE", "onUpdate loading new version view");
            //remoteViews.setViewVisibility(R.id.list_view_marks, View.VISIBLE);
            //RemoteViews Service needed to provide adapter for ListView
            Intent svcIntent = new Intent(context, WidgetService.class);
            //passing app widget id to that RemoteViews Service
            //svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.putExtra(WidgetMultilineAdapter.EXTRA_MANS_AS_STRING,
                    TrackingActivity.mansManager.toString());
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
            //Log.d("UPDATE", "onUpdate loading old version view");List<Mark> itemList = MarksManager.parseMarks(marks);
            Man[] itemList = TrackingActivity.mansManager.get();
            if (itemList.length == 0)
                remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
            else {
                remoteViews.setViewVisibility(R.id.widget_main_layout, View.VISIBLE);
                //remoteViews.removeAllViews(R.id.widget_main_layout);
                int len = itemList.length;
                len = len > 7 ? 7 : len;
                for (int i = 0; i < len; i++) {
                    MultilineItem multilineItem = itemList[i];
                    RemoteViews itemRemoteViews = new RemoteViews(
                            context.getPackageName(), R.layout.widget_list_item_multi_line_text);
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_title, multilineItem.getTitle(context, i));
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_text, multilineItem.getText(context, i));
                    //if (AppDataManager.isDebugMode(context)) Log.d(LOG_TAG, "onUpdate itemRemoteViews: " + itemRemoteViews + " remoteViews: " + remoteViews);
                    remoteViews.addView(R.id.widget_main_layout, itemRemoteViews);
                }
            }
            //remoteViews.setViewVisibility(R.id.linear_layout_marks, View.VISIBLE);
                    *//*MultilineItem[] items = (MultilineItem[]) MarksManager.parseMarks(mans).toArray();
                    int len = items.length > 10 ? 10 : items.length;
                    for (int i = 0; i < len; i++) {

                    }*//*
        }

        //Log.d("UPDATE", "onUpdate updating widget");
        // Instruct the widget manager to update the widget
        //if (AppDataManager.isDebugMode(context)) Log.d(LOG_TAG, "onUpdate updateWidgets");
        //ComponentName thisWidget = new ComponentName(context, TrackingWidget.class);
        //appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        //Log.d("UPDATE", "onUpdate exiting");
    }*/
}

