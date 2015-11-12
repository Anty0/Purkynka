package cz.anty.purkynkamanager.utils.other.list.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;

/**
 * Created by anty on 23.10.15.
 *
 * @author anty
 */
public abstract class WidgetProvider extends AppWidgetProvider {

    private static final String LOG_TAG = "WidgetProvider";
    private Intent mLastIntent = null;

    public static RemoteViews getContent(Context context, int[] appWidgetIds, Intent dataIntent,
                                         Class<? extends WidgetProvider> widgetClass, ContentType type)
            throws IllegalAccessException, InstantiationException {
        if (!context.getMainLooper().getThread().equals(Thread.currentThread()))
            throw new RuntimeException("Can't get widget content on non default thread");

        WidgetProvider provider = widgetClass.newInstance();
        provider.mLastIntent = dataIntent;
        if (!provider.onStartLoading(context, appWidgetIds)) return null;

        if (provider.hasDifferentWidgets() && appWidgetIds.length > 0)
            appWidgetIds = new int[]{appWidgetIds[0]};

        if (!provider.onStartUpdate(context, appWidgetIds)) return null;
        return type.get(provider, context, appWidgetIds);
    }

    public static int[] getAllWidgetIds(Context context,
                                        Class<? extends BroadcastReceiver> widgetClass) {
        Log.d(LOG_TAG, "getAllWidgetIds");
        return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, widgetClass));
    }

    public Intent getLastIntent() {
        Log.d(LOG_TAG, "getLastIntent");
        return mLastIntent;
    }

    protected boolean hasDifferentWidgets() {
        return false;
    }

    protected boolean showAsEmpty(Context context, int[] appWidgetIds) {
        return false;
    }

    protected int getTopVisibility(Context context, int[] appWidgetIds) {
        return View.VISIBLE;
    }

    protected CharSequence getTitle(Context context, int[] appWidgetIds) {
        return context.getText(R.string.app_name);
    }

    @ColorInt
    protected int getTopColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.colorPrimary);
    }

    @ColorInt
    protected int getBackgroundColor(Context context, int[] appWidgetIds) {
        return ContextCompat.getColor(context, R.color.navigationBarColor);
    }

    protected PendingIntent getRefreshPendingIntent(Context context, int[] appWidgetIds) {
        return null;
    }

    protected PendingIntent getTitlePendingIntent(Context context, int[] appWidgetIds) {
        return null;
    }

    @TargetApi(11)
    protected Intent getListWidgetServiceIntent(Context context, int[] appWidgetIds) {
        return new Intent(context, WidgetService.class);
    }

    protected MultilineItem[] getListItems(Context context, int[] appWidgetIds) {
        return new MultilineItem[0];
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive");
        mLastIntent = intent;
        super.onReceive(context, intent);
    }

    protected boolean onStartLoading(Context context, int[] appWidgetIds) {
        return true;
    }

    protected boolean onStartUpdate(Context context, int[] appWidgetIds) {
        return true;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(LOG_TAG, "onUpdate");
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews loadingRemoteViews = getBaseContent(context, appWidgetIds);
        loadingRemoteViews.addView(R.id.content_frame_layout, new RemoteViews(
                context.getPackageName(), R.layout.widget_content_loading));
        appWidgetManager.updateAppWidget(appWidgetIds, loadingRemoteViews);

        if (!onStartLoading(context, appWidgetIds)) return;

        if (hasDifferentWidgets()) {
            for (int appWidgetId : appWidgetIds)
                updateWidgets(context, appWidgetManager, new int[]{appWidgetId});
            return;
        }
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    protected void updateWidgets(Context context, AppWidgetManager appWidgetManager,
                                 int[] appWidgetIds) {
        if (!onStartUpdate(context, appWidgetIds)) return;
        appWidgetManager.updateAppWidget(appWidgetIds, getFullContent(context, appWidgetIds));
    }

    protected RemoteViews getBaseContent(Context context, int[] appWidgetIds) {
        RemoteViews baseRemoteViews = new RemoteViews(
                context.getPackageName(), R.layout.widget_base_layout);
        baseRemoteViews.setTextViewText(R.id.text_view_title, getTitle(context, appWidgetIds));

        int backgroundColor = getBackgroundColor(context, appWidgetIds);
        backgroundColor = Color.argb(
                Color.alpha(backgroundColor) - 30,
                Color.red(backgroundColor),
                Color.green(backgroundColor),
                Color.blue(backgroundColor)
        );
        baseRemoteViews.setInt(R.id.content_frame_layout, "setBackgroundColor", backgroundColor);

        baseRemoteViews.setInt(R.id.relative_layout_widget_main, "setBackgroundColor",
                getTopColor(context, appWidgetIds));
        baseRemoteViews.setViewVisibility(R.id.relative_layout_widget_main,
                getTopVisibility(context, appWidgetIds));
        baseRemoteViews.removeAllViews(R.id.content_frame_layout);
        return baseRemoteViews;
    }

    protected RemoteViews getFullContent(Context context, int[] appWidgetIds) {
        RemoteViews contentRemoteViews = getBaseContent(context, appWidgetIds);

        contentRemoteViews.setOnClickPendingIntent(R.id.image_button_refresh,
                getRefreshPendingIntent(context, appWidgetIds));
        contentRemoteViews.setOnClickPendingIntent(R.id.relative_layout_widget_main,
                getTitlePendingIntent(context, appWidgetIds));

        contentRemoteViews.addView(R.id.content_frame_layout,
                getDataContent(context, appWidgetIds));

        if (Build.VERSION.SDK_INT >= 11)
            setRemoteAdapter(context, appWidgetIds, contentRemoteViews);

        return contentRemoteViews;
    }

    protected void setRemoteAdapter(Context context, int[] appWidgetIds, RemoteViews remoteViews) {
        Intent widgetIntent = getListWidgetServiceIntent(context, appWidgetIds);
        widgetIntent.setData(Uri.parse(widgetIntent.toUri(Intent.URI_INTENT_SCHEME)));

        if (Build.VERSION.SDK_INT >= 14)
            remoteViews.setRemoteAdapter(R.id.content_list_view, widgetIntent);
        else {
            for (int appWidgetId : appWidgetIds) {
                //noinspection deprecation
                remoteViews.setRemoteAdapter(appWidgetId, R.id.content_list_view, widgetIntent);
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    protected RemoteViews getDataContent(Context context, int[] appWidgetIds) {
        RemoteViews contentRemoteViews;

        if (showAsEmpty(context, appWidgetIds)) {
            contentRemoteViews = new RemoteViews(context
                    .getPackageName(), R.layout.base_empty_view);
            return contentRemoteViews;
        }

        if (Build.VERSION.SDK_INT >= 11) {
            contentRemoteViews = new RemoteViews(context
                    .getPackageName(), R.layout.widget_content_new);

            contentRemoteViews.setEmptyView(R.id.content_list_view, R.id.empty_view);
        } else {
            MultilineItem[] itemList = getListItems(context, appWidgetIds);
            if (itemList.length == 0)
                contentRemoteViews = new RemoteViews(context
                        .getPackageName(), R.layout.base_empty_view);
            else {
                contentRemoteViews = new RemoteViews(context
                        .getPackageName(), R.layout.widget_content_old);
                contentRemoteViews.removeAllViews(R.id.content_list_linear_layout);

                int len = itemList.length;
                len = len > 7 ? 7 : len;
                for (int i = 0; i < len; i++) {
                    MultilineItem multilineItem = itemList[i];
                    RemoteViews itemRemoteViews = new RemoteViews(
                            context.getPackageName(), R.layout.widget_list_item_multi_line_text);
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_title, multilineItem.getTitle(context, i));
                    itemRemoteViews.setTextViewText(R.id.widget_text_view_text, multilineItem.getText(context, i));
                    contentRemoteViews.addView(R.id.content_list_linear_layout, itemRemoteViews);
                }
            }
        }
        return contentRemoteViews;

    }

    public enum ContentType {
        FULL_CONTENT, BASE_CONTENT, DATA_CONTENT;

        private RemoteViews get(WidgetProvider provider, Context context, int[] appWidgetIds) {
            switch (this) {
                case DATA_CONTENT:
                    return provider.getDataContent(context, appWidgetIds);
                case BASE_CONTENT:
                    return provider.getBaseContent(context, appWidgetIds);
                case FULL_CONTENT:
                default:
                    return provider.getFullContent(context, appWidgetIds);
            }
        }
    }
}
