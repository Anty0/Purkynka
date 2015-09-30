package cz.anty.attendancemanager.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import cz.anty.attendancemanager.R;
import cz.anty.attendancemanager.TrackingActivity;
import cz.anty.attendancemanager.receiver.TrackingReceiver;
import cz.anty.utils.Log;
import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.TrackingMansManager;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.widgetList.WidgetMultilineAdapter;
import cz.anty.utils.list.widgetList.WidgetService;
import cz.anty.utils.thread.OnceRunThread;

/**
 * Implementation of App Widget functionality.
 */
public class TrackingWidget extends AppWidgetProvider {

    private Intent lastIntent = null;

    public static void callUpdate(Context context, @Nullable String mans) {
        Log.d("TrackingWidget", "callUpdate");
        //context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, SASManageWidget.class));
        context.sendBroadcast(getUpdateIntent(context, mans));
    }

    private static Intent getUpdateIntent(Context context, @Nullable String mans) {
        Log.d("TrackingWidget", "getUpdateIntent");
        int[] allWidgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, TrackingWidget.class));

        return new Intent(context.getApplicationContext(), TrackingWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                .putExtra(WidgetMultilineAdapter.EXTRA_MANS_AS_STRING, mans);
    }

    private String updateMans(final Context context) {
        OnceRunThread worker = new OnceRunThread(context);
        final StringBuilder builder = new StringBuilder();
        try {
            worker.waitToWorkerStop(worker.startWorker(new Runnable() {
                @Override
                public void run() {
                    builder.append(TrackingReceiver.refreshTrackingMans(context, null, false));
                }
            }));
        } catch (InterruptedException e) {
            Log.d("TrackingWidget", "onUpdate", e);
        }
        return builder.toString();
    }

    @Override
    public synchronized void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.d("TrackingWidget", "onReceive");
        lastIntent = intent;
        super.onReceive(context, intent);
    }

    @SuppressLint("NewApi")
    @Override
    public synchronized void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("TrackingWidget", "onUpdate");
        appWidgetManager.updateAppWidget(appWidgetIds, new RemoteViews(
                context.getPackageName(), R.layout.tracking_widget_loading));

        String mans = lastIntent.getStringExtra(WidgetMultilineAdapter.EXTRA_MANS_AS_STRING);
        mans = mans == null ? updateMans(context) : mans;

        //Log.d("UPDATE", "onUpdate creating remote views");
        //which layout to show on widget
        RemoteViews remoteViews;

        if (Build.VERSION.SDK_INT >= 11) {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.tracking_widget_new);
        } else {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.tracking_widget_old);
        }

        //Log.d("UPDATE", "onUpdate setting onClick listeners");
        remoteViews.setOnClickPendingIntent(R.id.image_button_refresh,
                PendingIntent.getBroadcast(context, 0, getUpdateIntent(context, null), 0));

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
            svcIntent.putExtra(WidgetMultilineAdapter.EXTRA_MANS_AS_STRING, mans);
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
            Man[] itemList = new TrackingMansManager(mans).get();
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
                            context.getPackageName(), R.layout.text_widget_multi_line_list_item);
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_title, multilineItem.getTitle(context, i));
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_text, multilineItem.getText(context, i));
                    //if (AppDataManager.isDebugMode(context)) Log.d("TrackingWidget", "onUpdate itemRemoteViews: " + itemRemoteViews + " remoteViews: " + remoteViews);
                    remoteViews.addView(R.id.widget_main_layout, itemRemoteViews);
                }
            }
            //remoteViews.setViewVisibility(R.id.linear_layout_marks, View.VISIBLE);
                    /*MultilineItem[] items = (MultilineItem[]) MarksManager.parseMarks(mans).toArray();
                    int len = items.length > 10 ? 10 : items.length;
                    for (int i = 0; i < len; i++) {

                    }*/
        }

        //Log.d("UPDATE", "onUpdate updating widget");
        // Instruct the widget manager to update the widget
        //if (AppDataManager.isDebugMode(context)) Log.d("TrackingWidget", "onUpdate updateWidgets");
        //ComponentName thisWidget = new ComponentName(context, TrackingWidget.class);
        //appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        //Log.d("UPDATE", "onUpdate exiting");
    }

    @Override
    public void onEnabled(Context context) {
        Log.d("TrackingWidget", "onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        Log.d("TrackingWidget", "onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }
}

