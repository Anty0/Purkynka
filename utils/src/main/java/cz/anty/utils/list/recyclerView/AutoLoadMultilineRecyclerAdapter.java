package cz.anty.utils.list.recyclerView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import cz.anty.utils.Log;
import cz.anty.utils.R;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.listView.TextMultilineItem;

/**
 * Created by anty on 29.9.15.
 *
 * @author anty
 */
public class AutoLoadMultilineRecyclerAdapter extends MultilineRecyclerAdapter<MultilineItem> {

    private static final String LOADING_ITEM_TAG = "LOADING_ITEM";

    private TextMultilineItem mLoadingItem;
    private OnLoadNextPageListener mOnLoadNextListListener;
    private int mPage = 1;
    private boolean mAutoLoad = true;

    public AutoLoadMultilineRecyclerAdapter(Context context, @Nullable OnLoadNextPageListener onLoadNextListListener) {
        super(R.layout.loading_multi_line_list_item);
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
        Log.d(getClass().getSimpleName(), "onBindViewHolder: " + position);
        if (super.getItemCount() == position) {
            mPage++;
            if (mOnLoadNextListListener != null)
                mOnLoadNextListListener.onLoadNextPage(this, mPage);
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void clearItems() {
        Log.d(getClass().getSimpleName(), "clearItems");
        super.clearItems();
        mPage = 1;
    }

    @Override
    public MultilineItem getItem(int position) {
        Log.d(getClass().getSimpleName(), "getItem: " + position);
        if (position == super.getItemCount()) {
            return mLoadingItem;
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public int getItemCount() {
        Log.d(getClass().getSimpleName(), "getCount");
        int count = super.getItemCount();
        //Log.d(getClass().getSimpleName(), "getCount orig: " + count + " result: " + (mAutoLoad ? count + 1 : count));
        return mAutoLoad ? count + 1 : count;
    }

    public void setAutoLoad(boolean autoLoad) {
        Log.d(getClass().getSimpleName(), "setAutoLoad: " + autoLoad);
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

        private final ProgressBar loadingProgressBar;

        public AutoLoadMultilineViewHolder(View itemView) {
            super(itemView);
            Log.d(getClass().getSimpleName(), "<init>");
            loadingProgressBar = (ProgressBar) itemView
                    .findViewById(R.id.progress_loading);
        }

        @Override
        protected void onBindViewHolder(MultilineItem item, int position) {
            Log.d(getClass().getSimpleName(), "onBindViewHolder");
            loadingProgressBar.setVisibility((item instanceof TextMultilineItem && LOADING_ITEM_TAG
                    .equals(((TextMultilineItem) item).getTag())) ? View.VISIBLE : View.GONE);
            super.onBindViewHolder(item, position);
        }
    }
}
