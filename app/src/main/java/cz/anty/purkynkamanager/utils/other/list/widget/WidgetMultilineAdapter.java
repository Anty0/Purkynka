package cz.anty.purkynkamanager.utils.other.list.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.attendance.man.TrackingMansManager;
import cz.anty.purkynkamanager.utils.other.list.listView.MultilineItem;
import cz.anty.purkynkamanager.utils.other.sas.mark.Mark;
import cz.anty.purkynkamanager.utils.other.sas.mark.MarksManager;

/**
 * Created by anty on 22.7.15.
 *
 * @author anty
 */
@TargetApi(11)
public class WidgetMultilineAdapter implements RemoteViewsService.RemoteViewsFactory {

    public static final String EXTRA_MARKS_AS_STRING = "MARKS_STRING";
    public static final String EXTRA_MANS_AS_STRING = "MANS_STRING";
    private static final String LOG_TAG = "WidgetMultilineAdapter";
    private final ArrayList<MultilineItem> mItems = new ArrayList<>();
    private final Context mContext;
    //private int appWidgetId;

    public WidgetMultilineAdapter(Context context, Intent intent) {
        Log.d(LOG_TAG, "<init>");
        mContext = context;
        /*appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);*/

        MultilineItem[] items;

        Bundle extras = intent.getExtras();
        if (extras == null) {
            items = new MultilineItem[0];
        } else {
            String marksAsString = extras.getString(EXTRA_MARKS_AS_STRING);
            String mansAsString = extras.getString(EXTRA_MANS_AS_STRING);
            if (marksAsString != null) {
                List<Mark> itemList = MarksManager.parseMarks(marksAsString);
                items = itemList.toArray(new MultilineItem[itemList.size()]);
            } else if (mansAsString != null) {
                items = new TrackingMansManager(mansAsString).get();
            } else items = new MultilineItem[0];
        }

        populateListItem(items);
    }

    private void populateListItem(MultilineItem[] items) {
        Log.d(LOG_TAG, "populateListItem");
        /*for (int i = 0; i < 10; i++) {
            LauncherActivity.ListItem listItem = new LauncherActivity.ListItem();
            listItem.heading = "Heading" + i;
            listItem.content = i
                    + " This is the content of the app widget listview.Nice content though";
            mItems.add(listItem);
        }*/
        mItems.clear();
        Collections.addAll(mItems, items);
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        Log.d(LOG_TAG, "onDataSetChanged");
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public int getCount() {
        Log.d(LOG_TAG, "getCount: " + mItems.size());
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        Log.d(LOG_TAG, "getItemId: " + position);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        Log.d(LOG_TAG, "hasStableIds: " + false);
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(LOG_TAG, "getViewAt: " + position);
        final RemoteViews remoteView = new RemoteViews(
                mContext.getPackageName(), R.layout.widget_list_item_multi_line_text);
        MultilineItem multilineItem = mItems.get(position);
        remoteView.setTextViewText(R.id.widget_text_view_title, multilineItem.getTitle(mContext, position));
        remoteView.setTextViewText(R.id.widget_text_view_text, multilineItem.getText(mContext, position));
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        Log.d(LOG_TAG, "getLoadingView: " + null);
        return null;
    }

    @Override
    public int getViewTypeCount() {
        Log.d(LOG_TAG, "getViewTypeCount: " + 0);
        return 1;
    }
}
