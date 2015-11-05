package cz.anty.purkynkamanager.utils.other.list.recyclerView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.items.TextMultilineItem;

/**
 * Created by anty on 29.9.15.
 *
 * @author anty
 */
public class AutoLoadMultilineRecyclerAdapter extends MultilineRecyclerAdapter<MultilineItem> {

    private static final String LOG_TAG = "AutoLoadMultilineRecyclerAdapter";
    private static final String LOADING_ITEM_TAG = "LOADING_ITEM";

    private TextMultilineItem mLoadingItem;
    private OnLoadNextPageListener mOnLoadNextListListener;
    private int mPage = 1;
    private boolean mAutoLoad = true;

    public AutoLoadMultilineRecyclerAdapter(Context context, @Nullable OnLoadNextPageListener onLoadNextListListener) {
        super(R.layout.list_item_multi_line_loading);
        mLoadingItem = new TextMultilineItem(context.getText(R.string.wait_text_loading),
                context.getText(R.string.wait_text_please_wait));
        mLoadingItem.setTag(LOADING_ITEM_TAG);
        mOnLoadNextListListener = onLoadNextListListener;
    }

    @Override
    protected AutoLoadMultilineViewHolder getViewHolderInstance(View view) {
        return new AutoLoadMultilineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder<MultilineItem> holder, int position) {
        Log.d(LOG_TAG, "onBindViewHolder: " + position);
        if (super.getItemCount() == position) {
            mPage++;
            if (mOnLoadNextListListener != null)
                mOnLoadNextListListener.onLoadNextPage(this, mPage);
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void clearItems() {
        Log.d(LOG_TAG, "clearItems");
        super.clearItems();
        mPage = 1;
    }

    @Override
    public MultilineItem getItem(int position) {
        Log.d(LOG_TAG, "getItem: " + position);
        if (position == super.getItemCount()) {
            return mLoadingItem;
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(LOG_TAG, "getCount");
        int count = super.getItemCount();
        //Log.d(LOG_TAG, "getCount orig: " + count + " result: " + (mAutoLoad ? count + 1 : count));
        return mAutoLoad ? count + 1 : count;
    }

    public void setAutoLoad(boolean autoLoad) {
        Log.d(LOG_TAG, "setAutoLoad: " + autoLoad);
        if (mAutoLoad != autoLoad) {
            mAutoLoad = autoLoad;
            if (isNotifyOnChange())
                if (autoLoad) notifyItemInserted(super.getItemCount());
                else notifyItemRemoved(super.getItemCount());
        }
    }

    public interface OnLoadNextPageListener {

        void onLoadNextPage(AutoLoadMultilineRecyclerAdapter multilineAdapter, int page);
    }

    private static class AutoLoadMultilineViewHolder extends MultilineViewHolder<MultilineItem> {

        private static final String LOG_TAG = "AutoLoadMultilineViewHolder";

        private final ProgressBar loadingProgressBar;

        public AutoLoadMultilineViewHolder(View itemView) {
            super(itemView);
            Log.d(LOG_TAG, "<init>");
            loadingProgressBar = (ProgressBar) itemView
                    .findViewById(R.id.progress_loading);
        }

        @Override
        protected void onBindViewHolder(MultilineItem item, int position) {
            Log.d(LOG_TAG, "onBindViewHolder");
            loadingProgressBar.setVisibility((item instanceof TextMultilineItem && LOADING_ITEM_TAG
                    .equals(((TextMultilineItem) item).getTag())) ? View.VISIBLE : View.GONE);
            super.onBindViewHolder(item, position);
        }
    }
}
