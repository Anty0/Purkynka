package cz.anty.purkynkamanager.utils.list.recyclerView;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import cz.anty.purkynkamanager.utils.Log;

/**
 * Created by anty on 28.9.15.
 *
 * @author anty
 */
public abstract class RecyclerAdapter<T, VH extends RecyclerAdapter
        .ItemViewHolder<T>> extends ArrayRecyclerAdapter<T, VH> {

    private static final String LOG_TAG = "RecyclerAdapter";
    private final Context mContext;
    private final int mLayoutResourceId;
    private View mEmptyView = null;

    public RecyclerAdapter(Context context, int layoutResourceId) {
        Log.d(LOG_TAG, "<init> layoutResourceId: " + layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    public RecyclerAdapter(Context context, int layoutResourceId, Collection<? extends T> data) {
        super(data);
        Log.d(LOG_TAG, "<init> layoutResourceId: " + layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    public RecyclerAdapter(Context context, int layoutResourceId, T... data) {
        super(data);
        Log.d(LOG_TAG, "<init> layoutResourceId: " + layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(LOG_TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutResourceId, parent, false);

        return getViewHolderInstance(view);
    }

    protected abstract VH getViewHolderInstance(View view);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Log.d(LOG_TAG, "onBindViewHolder");
        holder.onBindViewHolder(getItem(position), position);
    }

    @Override
    protected void onDataSetChanged() {
        updateEmptyView();
        super.onDataSetChanged();
    }

    private void updateEmptyView() {
        new Handler(mContext.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (mEmptyView != null)
                    mEmptyView.setVisibility(getItemCount() == 0
                            ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        updateEmptyView();
    }

    public static abstract class ItemViewHolder<I> extends RecyclerView.ViewHolder {

        private static final String LOG_TAG = "ItemViewHolder";

        public ItemViewHolder(View itemView) {
            super(itemView);
            Log.d(LOG_TAG, "<init>");
        }

        protected abstract void onBindViewHolder(I item, int position);
    }
}