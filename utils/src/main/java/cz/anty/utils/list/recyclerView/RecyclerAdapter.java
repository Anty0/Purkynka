package cz.anty.utils.list.recyclerView;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import cz.anty.utils.Log;
import cz.anty.utils.R;

/**
 * Created by anty on 28.9.15.
 *
 * @author anty
 */
public abstract class RecyclerAdapter<T, VH extends RecyclerAdapter.ItemViewHolder<T>> extends ArrayRecyclerAdapter<T, VH> {

    private final int mLayoutResourceId;

    public RecyclerAdapter(int layoutResourceId) {
        Log.d(getClass().getSimpleName(), "<init> layoutResourceId: " + layoutResourceId);
        mLayoutResourceId = layoutResourceId;
    }

    public RecyclerAdapter(int layoutResourceId, Collection<? extends T> data) {
        super(data);
        Log.d(getClass().getSimpleName(), "<init> layoutResourceId: " + layoutResourceId);
        mLayoutResourceId = layoutResourceId;
    }

    public RecyclerAdapter(int layoutResourceId, T... data) {
        super(data);
        Log.d(getClass().getSimpleName(), "<init> layoutResourceId: " + layoutResourceId);
        mLayoutResourceId = layoutResourceId;
    }

    public static RecyclerView inflateToActivity(Activity activity, @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                                                 @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        Log.d(RecyclerAdapter.class.getSimpleName(), "inflateToActivity");
        activity.setContentView(layoutResourceId == null ? R.layout.activity_recycler : layoutResourceId);
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerView);
        setValues(recyclerView, activity, instanceOfAdapter, itemTouchListener);
        return recyclerView;
    }

    public static View inflate(Context context, @Nullable ViewGroup parent, boolean attachToRoot,
                               @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                               @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        return inflate(LayoutInflater.from(context), context, parent, attachToRoot, layoutResourceId, instanceOfAdapter, itemTouchListener);
    }

    public static View inflate(LayoutInflater inflater, Context context, @Nullable ViewGroup parent, boolean attachToRoot,
                               @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                               @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        Log.d(RecyclerAdapter.class.getSimpleName(), "inflate");
        View result = inflater.inflate(layoutResourceId == null
                ? R.layout.activity_recycler : layoutResourceId, parent, attachToRoot);
        RecyclerView recyclerView = (RecyclerView) result.findViewById(R.id.recyclerView);
        setValues(recyclerView, context, instanceOfAdapter, itemTouchListener);
        return result;
    }

    private static void setValues(RecyclerView recyclerView, Context context, RecyclerView.Adapter instanceOfAdapter,
                                  @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        Log.d(RecyclerAdapter.class.getSimpleName(), "setValues");
        //recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new SpecialItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (itemTouchListener != null)
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(context, recyclerView, itemTouchListener));
        recyclerView.setAdapter(instanceOfAdapter);
        /*recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                LinearLayoutManager.VERTICAL));*/
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(getClass().getSimpleName(), "onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(mLayoutResourceId, parent, false);

        return getViewHolderInstance(view);
    }

    protected abstract VH getViewHolderInstance(View view);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Log.d(getClass().getSimpleName(), "onBindViewHolder");
        holder.onBindViewHolder(getItem(position), position);
    }

    public static abstract class ItemViewHolder<I> extends RecyclerView.ViewHolder {

        public ItemViewHolder(View itemView) {
            super(itemView);
            Log.d(getClass().getSimpleName(), "<init>");
        }

        protected abstract void onBindViewHolder(I item, int position);
    }
}