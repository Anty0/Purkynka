package cz.anty.utils.listItem;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.R;
import cz.anty.utils.sas.mark.Mark;
import cz.anty.utils.sas.mark.MarksManager;

/**
 * Created by anty on 22.7.15.
 *
 * @author anty
 */
@TargetApi(11)
public class WidgetMultilineAdapter implements RemoteViewsService.RemoteViewsFactory {

    public static final String EXTRA_MARKS_AS_STRING = "MARKS_STRING";

    private final ArrayList<MultilineItem> listItemList = new ArrayList<>();
    private final Context context;
    //private int appWidgetId;

    public WidgetMultilineAdapter(Context context, Intent intent) {
        if (AppDataManager.isDebugMode(context)) Log.d("WidgetMultilineAdapter", "<init>");
        this.context = context;
        /*appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);*/

        MultilineItem[] items;

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String marksAsString = extras.getString(EXTRA_MARKS_AS_STRING);
            if (marksAsString != null) {
                List<Mark> itemList = MarksManager.parseMarks(marksAsString);
                items = itemList.toArray(new MultilineItem[itemList.size()]);
            } else items = new MultilineItem[0];
        } else items = new MultilineItem[0];

        populateListItem(items);
    }

    private void populateListItem(MultilineItem[] items) {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "populateListItem");
        /*for (int i = 0; i < 10; i++) {
            LauncherActivity.ListItem listItem = new LauncherActivity.ListItem();
            listItem.heading = "Heading" + i;
            listItem.content = i
                    + " This is the content of the app widget listview.Nice content though";
            listItemList.add(listItem);
        }*/
        listItemList.clear();
        listItemList.addAll(Arrays.asList(items));
    }

    @Override
    public void onCreate() {
        if (AppDataManager.isDebugMode(context)) Log.d("WidgetMultilineAdapter", "onCreate");
    }

    @Override
    public void onDataSetChanged() {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "onDataSetChanged");
    }

    @Override
    public void onDestroy() {
        if (AppDataManager.isDebugMode(context)) Log.d("WidgetMultilineAdapter", "onDestroy");
    }

    @Override
    public int getCount() {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "getCount: " + listItemList.size());
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "getItemId: " + position);
        return position;
    }

    @Override
    public boolean hasStableIds() {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "hasStableIds: " + false);
        return false;
    }

    /*
    *Similar to getView of Adapter where instead of View
    *we return RemoteViews
    *
    */
    @Override
    public RemoteViews getViewAt(int position) {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "getViewAt: " + position);
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.text_multi_line_list_item);
        MultilineItem multilineItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.txtTitle, multilineItem.getTitle());
        remoteView.setTextViewText(R.id.txtTitle2, multilineItem.getText());
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "getLoadingView: " + null);
        return null;
    }

    @Override
    public int getViewTypeCount() {
        if (AppDataManager.isDebugMode(context))
            Log.d("WidgetMultilineAdapter", "getViewTypeCount: " + 0);
        return 1;
    }
}
