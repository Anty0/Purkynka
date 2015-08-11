package cz.anty.utils.listItem;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.R;

/**
 * Created by anty on 9.8.15.
 *
 * @author anty
 */
public class AutoLoadMultilineAdapter extends MultilineAdapter {

    private Context context;
    private MultilineItem loadingItem;
    private OnLoadNextListListener onLoadNextListListener;
    private int page = 1;
    private boolean autoLoad = true;
    private boolean notifyOnChange = true;

    public AutoLoadMultilineAdapter(Context context, int layoutResourceId, @Nullable OnLoadNextListListener onLoadNextListListener) {
        super(context, layoutResourceId);
        init(context, onLoadNextListListener);
    }

    public AutoLoadMultilineAdapter(Context context, int layoutResourceId, @Nullable OnLoadNextListListener onLoadNextListListener, MultilineItem[] data) {
        super(context, layoutResourceId, data);
        init(context, onLoadNextListListener);
    }

    private void init(Context context, @Nullable OnLoadNextListListener onLoadNextListListener) {
        if (AppDataManager.isDebugMode(context)) Log.d("ALMultilineAdapter", "init");
        this.context = context;
        this.loadingItem = new TextMultilineItem(context.getString(R.string.wait_text_loading),
                context.getString(R.string.wait_text_please_wait), R.layout.loading_multi_line_list_item);
        this.onLoadNextListListener = onLoadNextListListener;
    }

    @Override
    public void clear() {
        if (AppDataManager.isDebugMode(context)) Log.d("ALMultilineAdapter", "clear");
        super.clear();
        page = 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (AppDataManager.isDebugMode(context))
            Log.d("ALMultilineAdapter", "getView: " + position);
        if (super.getCount() == position) {
            page++;
            if (onLoadNextListListener != null)
                onLoadNextListListener.onLoadNextList(this, page);
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public MultilineItem getItem(int position) {
        if (AppDataManager.isDebugMode(context))
            Log.d("ALMultilineAdapter", "getItem: " + position);
        if (position == super.getCount()) {
            return loadingItem;
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public int getCount() {
        if (AppDataManager.isDebugMode(context)) Log.d("ALMultilineAdapter", "getCount start");
        int count = super.getCount();
        if (AppDataManager.isDebugMode(context))
            Log.d("ALMultilineAdapter", "getCount orig: " + count + " result: " + (autoLoad ? count + 1 : count));
        return autoLoad ? count + 1 : count;
    }

    @Override
    public void setNotifyOnChange(boolean notifyOnChange) {
        if (AppDataManager.isDebugMode(context))
            Log.d("ALMultilineAdapter", "setNotifyOnChange: " + notifyOnChange);
        super.setNotifyOnChange(notifyOnChange);
        this.notifyOnChange = notifyOnChange;
    }

    @Override
    public void notifyDataSetChanged() {
        if (AppDataManager.isDebugMode(context)) Log.d("ALMultilineAdapter", "notifyDataSetChange");
        super.notifyDataSetChanged();
    }

    public void setAutoLoad(boolean autoLoad) {
        if (AppDataManager.isDebugMode(context))
            Log.d("ALMultilineAdapter", "setAutoLoad: " + autoLoad);
        this.autoLoad = autoLoad;
        if (notifyOnChange)
            notifyDataSetChanged();
    }

    public interface OnLoadNextListListener {

        void onLoadNextList(AutoLoadMultilineAdapter multilineAdapter, int page);
    }
}
