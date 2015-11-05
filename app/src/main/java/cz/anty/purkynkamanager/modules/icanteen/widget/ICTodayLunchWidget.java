package cz.anty.purkynkamanager.modules.icanteen.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import cz.anty.purkynkamanager.ApplicationBase;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.LunchesManager;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;

/**
 * Created by anty on 23.10.15.
 *
 * @author anty
 */
public class ICTodayLunchWidget extends WidgetProvider {

    private static final String LOG_TAG = "ICTodayLunchWidget";
    private static final String REQUEST_UPDATE = "REQUEST_LUNCHES_UPDATE";

    public static void callUpdate(Context context, boolean refresh) {
        Log.d(LOG_TAG, "callUpdate");
        context.sendBroadcast(getUpdateIntent(context, refresh));
    }

    private static Intent getUpdateIntent(Context context, boolean refresh) {
        Log.d(LOG_TAG, "getUpdateIntent refresh: " + refresh);
        return new Intent(context.getApplicationContext(), ICTodayLunchWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        getAllWidgetIds(context, ICTodayLunchWidget.class))
                .putExtra(REQUEST_UPDATE, refresh);
    }

    @Override
    protected boolean showAsEmpty(Context context, int[] appWidgetIds) {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN);
    }

    @Override
    protected CharSequence getTitle(Context context, int[] appWidgetIds) {
        return context.getText(R.string.widget_label_next_lunch);
    }

    @Override
    protected int getTopColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.colorPrimaryIC);
    }

    @Override
    protected int getBackgroundColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.navigationBarColorIC);
    }

    @Override
    protected PendingIntent getRefreshPendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getBroadcast(context, 0, getUpdateIntent(context, true), 0);
    }

    @Override
    protected PendingIntent getTitlePendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getActivity(context, 0,
                new Intent(context, ICSplashActivity.class), 0);
    }

    @Override
    protected boolean onStartLoading(final Context context, int[] appWidgetIds) {
        if (!showAsEmpty(context, appWidgetIds) &&
                getLastIntent().getBooleanExtra(REQUEST_UPDATE, false)) {
            Log.d(LOG_TAG, "onStartLoading refreshing month");
            if (ICSplashActivity.serviceManager == null ||
                    !ICSplashActivity.serviceManager.isConnected()) {
                ICSplashActivity.initService(context, ApplicationBase.WORKER, null);
                return false;
            }
            ICSplashActivity.serviceManager.getBinder().refreshMonth();
            return false;
        }

        return true;
    }

    @Override
    protected void setRemoteAdapter(Context context, int[] appWidgetIds, RemoteViews remoteViews) {

    }

    @Override
    protected RemoteViews getDataContent(Context context, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context
                .getPackageName(), R.layout.widget_content_icanteen_next_lunch);
        remoteViews.setOnClickPendingIntent(R.id.list_item_next_lunch,
                getTitlePendingIntent(context, appWidgetIds));

        if (showAsEmpty(context, appWidgetIds)) {
            remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        } else {
            MonthLunchDay[] lunchDays = getLunchDays(context);
            MonthLunchDay lunchDay = lunchDays.length > 0 ? lunchDays[0] : null;
            if (lunchDay != null) {
                remoteViews.setViewVisibility(R.id.list_item_next_lunch, View.VISIBLE);
                remoteViews.setTextViewText(R.id.text_view_title, lunchDay.getTitle(context, MultilineItem.NO_POSITION));
                remoteViews.setTextViewText(R.id.text_view_text, lunchDay.getText(context, MultilineItem.NO_POSITION));
            } else remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        }

        return remoteViews;
    }

    private MonthLunchDay[] getLunchDays(Context context) {
        if (ICSplashActivity.serviceManager != null &&
                ICSplashActivity.serviceManager.isConnected()) {
            return ICSplashActivity.serviceManager
                    .getBinder().getNewMonthFast();
        }

        return new LunchesManager(context).getNewMonthLunches();
    }

}
