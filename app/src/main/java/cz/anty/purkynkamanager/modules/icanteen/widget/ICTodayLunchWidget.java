package cz.anty.purkynkamanager.modules.icanteen.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.LunchesManager;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import cz.anty.purkynkamanager.utils.other.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;

/**
 * Created by anty on 23.10.15.
 *
 * @author anty
 */
public class ICTodayLunchWidget extends WidgetProvider {

    public static void callUpdate(Context context) {
        context.sendBroadcast(getUpdateIntent(context));
    }

    private static Intent getUpdateIntent(Context context) {
        return new Intent(context.getApplicationContext(), ICTodayLunchWidget.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        getAllWidgetIds(context, ICTodayLunchWidget.class));
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
        return Utils.getColor(context, R.color.colorPrimaryIC);
    }

    @Override
    protected int getBackgroundColor(Context context, int[] appWidgetIds) {
        return Utils.getColor(context, R.color.navigationBarColorIC);
    }

    @Override
    protected PendingIntent getRefreshPendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getBroadcast(context, 0, getUpdateIntent(context), 0);
    }

    @Override
    protected PendingIntent getTitlePendingIntent(Context context, int[] appWidgetIds) {
        return PendingIntent.getActivity(context, 0,
                new Intent(context, ICSplashActivity.class), 0);
    }

    @Override
    protected RemoteViews getDataContent(Context context, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context
                .getPackageName(), R.layout.widget_content_icanteen_next_lunch);
        remoteViews.setOnClickPendingIntent(R.id.list_item_actual,
                getTitlePendingIntent(context, appWidgetIds));

        if (showAsEmpty(context, appWidgetIds)) {
            remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        } else {
            MonthLunchDay[] lunchDays = new LunchesManager(context).getNewMonthLunches();
            MonthLunchDay lunchDay = lunchDays.length > 0 ? lunchDays[0] : null;
            if (lunchDay != null) {
                remoteViews.setViewVisibility(R.id.list_item_next_lunch, View.VISIBLE);
                remoteViews.setTextViewText(R.id.text_view_title, lunchDay.getTitle(context, MultilineItem.NO_POSITION));
                remoteViews.setTextViewText(R.id.text_view_text, lunchDay.getText(context, MultilineItem.NO_POSITION));
            } else remoteViews.setViewVisibility(R.id.empty_view, View.VISIBLE);
        }

        return remoteViews;
    }

}
