package cz.anty.sasmanager.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import cz.anty.sasmanager.R;
import cz.anty.sasmanager.SASSplashActivity;
import cz.anty.utils.AppDataManager;
import cz.anty.utils.listItem.WidgetMultilineAdapter;
import cz.anty.utils.listItem.WidgetService;
import cz.anty.utils.sas.mark.MarksManager;

/**
 * Implementation of App Widget functionality.
 */
public class SASManageWidget extends AppWidgetProvider {

    private static Intent getUpdateIntent(Context context) {
        if (AppDataManager.isDebugMode(context)) Log.d("SASManageWidget", "getUpdateIntent");
        int[] allWidgetIds = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, SASManageWidget.class));

        Intent intent = new Intent(context.getApplicationContext(),
                SASManageWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        return intent;
    }

    public static void callUpdate(Context context) {
        if (AppDataManager.isDebugMode(context)) Log.d("SASManageWidget", "callUpdate");
        //context.sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, context, SASManageWidget.class));
        context.sendBroadcast(getUpdateIntent(context));
    }

    @SuppressLint("NewApi")
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        if (AppDataManager.isDebugMode(context)) Log.d("SASManageWidget", "onUpdate");
        appWidgetManager.updateAppWidget(appWidgetIds, new RemoteViews(
                context.getPackageName(), R.layout.sasmanage_widget_loading));

        String marks = updateMarks(context);

        //Log.d("UPDATE", "onUpdate creating remote views");
        //which layout to show on widget
        RemoteViews remoteViews;

        if (Build.VERSION.SDK_INT >= 11) {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.sasmanage_widget_new);
        } else {
            remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.sasmanage_widget_old);
        }

        //Log.d("UPDATE", "onUpdate setting onClick listeners");
        remoteViews.setOnClickPendingIntent(R.id.image_button_refresh,
                PendingIntent.getBroadcast(context, 0, getUpdateIntent(context), 0));

        remoteViews.setOnClickPendingIntent(R.id.relative_layout_widget_main,
                PendingIntent.getActivity(context, 0,
                        new Intent(context, SASSplashActivity.class), 0));

        //Log.d("UPDATE", "onUpdate checking if is logged in");
        if (!AppDataManager.isLoggedIn(AppDataManager.Type.SAS, context)) {
            //Log.d("UPDATE", "onUpdate not logged in");
            remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 11) {
            //Log.d("UPDATE", "onUpdate loading new version view");
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
            remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
            //remoteViews.setViewVisibility(R.id.linear_layout_marks, View.VISIBLE);
                    /*MultilineItem[] items = (MultilineItem[]) MarksManager.parseMarks(marks).toArray();
                    int len = items.length > 10 ? 10 : items.length;
                    for (int i = 0; i < len; i++) {

                    }*/
        }

        //Log.d("UPDATE", "onUpdate updating widget");
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        //Log.d("UPDATE", "onUpdate exiting");
    }

    @Override
    public void onEnabled(Context context) {
        if (AppDataManager.isDebugMode(context)) Log.d("SASManageWidget", "onEnabled");
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        if (AppDataManager.isDebugMode(context)) Log.d("SASManageWidget", "onDisabled");
        // Enter relevant functionality for when the last widget is disabled
    }

    private String updateMarks(Context context) {
        if (AppDataManager.isDebugMode(context)) Log.d("SASManageWidget", "updateMarks");
        return new MarksManager(context).toString();
    }
}

