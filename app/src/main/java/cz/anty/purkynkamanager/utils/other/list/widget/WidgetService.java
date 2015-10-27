package cz.anty.purkynkamanager.utils.other.list.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.widget.RemoteViewsService;

import cz.anty.purkynkamanager.utils.other.Log;

@TargetApi(11)
public class WidgetService extends RemoteViewsService {

    private static final String LOG_TAG = "WidgetService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(LOG_TAG, "onGetViewFactory");
        /*int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);*/

        return (new WidgetMultilineAdapter(getApplicationContext(), intent));
    }

}
