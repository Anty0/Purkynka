package cz.anty.purkynkamanager.utils.other.list.listView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;

/**
 * Created by anty on 9.8.15.
 *
 * @author anty
 */
@Deprecated
public class AutoLoadMultilineAdapter extends MultilineAdapter<MultilineItem> {

    private static final String LOG_TAG = "AutoLoadMultilineAdapter";

    private MultilineItem loadingItem;
    private OnLoadNextListListener onLoadNextListListener;
    private int page = 1;
    private boolean autoLoad = true;
    private boolean notifyOnChange = true;

    public AutoLoadMultilineAdapter(Context context, @Nullable OnLoadNextListListener onLoadNextListListener) {
        super(context);
        init(context, onLoadNextListListener);
    }

    public AutoLoadMultilineAdapter(Context context, int layoutResourceId, @Nullable OnLoadNextListListener onLoadNextListListener) {
        super(context, layoutResourceId);
        init(context, onLoadNextListListener);
    }

    public AutoLoadMultilineAdapter(Context context, int layoutResourceId, @Nullable OnLoadNextListListener onLoadNextListListener, MultilineItem[] data) {
        super(context, layoutResourceId, data);
        init(context, onLoadNextListListener);
    }

    private void init(Context context, @Nullable OnLoadNextListListener onLoadNextListListener) {
        Log.d("ALMultilineAdapter", "init");
        this.loadingItem = new TextMultilineItem(context.getText(R.string.wait_text_loading),
                context.getText(R.string.wait_text_please_wait)/*, R.layout.list_item_multi_line_loading*/);
        this.onLoadNextListListener = onLoadNextListListener;
    }

    @Override
    public void clear() {
        Log.d(LOG_TAG, "clear");
        super.clear();
        page = 1;
    }

    @Override
    public View generateView(int position, View convertView, ViewGroup parent) {
        Log.d("ALMultilineAdapter", "generateView: " + position);
        if (super.getCount() == position) {
            page++;
            if (onLoadNextListListener != null)
                onLoadNextListListener.onLoadNextList(this, page);
        }
        return super.generateView(position, convertView, parent);
    }

    @Override
    public MultilineItem getItem(int position) {
        Log.d(LOG_TAG, "getItem: " + position);
        if (position == super.getCount()) {
            return loadingItem;
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public int getCount() {
        Log.d(LOG_TAG, "getCount start");
        int count = super.getCount();
        Log.d(LOG_TAG, "getCount orig: " + count + " result: " + (autoLoad ? count + 1 : count));
        return autoLoad ? count + 1 : count;
    }

    @Override
    public void setNotifyOnChange(boolean notifyOnChange) {
        Log.d(LOG_TAG, "setNotifyOnChange: " + notifyOnChange);
        super.setNotifyOnChange(notifyOnChange);
        this.notifyOnChange = notifyOnChange;
    }

    @Override
    public void notifyDataSetChanged() {
        Log.d(LOG_TAG, "notifyDataSetChange");
        super.notifyDataSetChanged();
    }

    public void setAutoLoad(boolean autoLoad) {
        Log.d(LOG_TAG, "setAutoLoad: " + autoLoad);
        this.autoLoad = autoLoad;
        if (notifyOnChange)
            notifyDataSetChanged();
    }

    @Deprecated
    public interface OnLoadNextListListener {

        void onLoadNextList(AutoLoadMultilineAdapter multilineAdapter, int page);
    }
}
