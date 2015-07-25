package cz.anty.utils.listItem;

import android.annotation.TargetApi;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

import cz.anty.utils.AppDataManager;

@TargetApi(11)
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        if (AppDataManager.isDebugMode(this)) Log.d("WidgetService", "onGetViewFactory");
        /*int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);*/

        return (new WidgetMultilineAdapter(this.getApplicationContext(), intent));
    }

}
