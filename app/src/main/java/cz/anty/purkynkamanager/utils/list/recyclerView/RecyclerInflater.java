package cz.anty.purkynkamanager.utils.list.recyclerView;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.Log;

/**
 * Created by anty on 16.10.15.
 *
 * @author anty
 */
public final class RecyclerInflater {

    private static final String LOG_TAG = "RecyclerInflater";

    public static RecyclerView inflateToActivity(Activity activity, RecyclerView.Adapter instanceOfAdapter) {
        return inflateToActivity(activity, null, instanceOfAdapter, null, null);
    }

    public static RecyclerView inflateToActivity(Activity activity, RecyclerView.Adapter instanceOfAdapter,
                                                 @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        return inflateToActivity(activity, null, instanceOfAdapter, null, itemTouchListener);
    }

    public static RecyclerView inflateToActivity(Activity activity, @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                                                 @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        return inflateToActivity(activity, layoutResourceId, instanceOfAdapter, null, itemTouchListener);
    }

    public static RecyclerView inflateToActivity(Activity activity, @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                                                 @Nullable Integer emptyViewId, @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        Log.d(LOG_TAG, "inflateToActivity");
        activity.setContentView(layoutResourceId == null ? R.layout.activity_recycler : layoutResourceId);
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.recyclerView);
        View emptyView = activity.findViewById(emptyViewId == null ? R.id.empty_view : emptyViewId);
        setValues(recyclerView, activity, instanceOfAdapter, emptyView, itemTouchListener);
        return recyclerView;
    }

    public static View inflate(Context context, @Nullable ViewGroup parent, boolean attachToRoot,
                               RecyclerView.Adapter instanceOfAdapter, @Nullable
                               RecyclerItemClickListener.ClickListener itemTouchListener) {
        return inflate(context, parent, attachToRoot, null, instanceOfAdapter, null, itemTouchListener);
    }

    public static View inflate(Context context, @Nullable ViewGroup parent, boolean attachToRoot,
                               @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                               @Nullable Integer emptyViewId, @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        return inflate(LayoutInflater.from(context), context, parent, attachToRoot, layoutResourceId, instanceOfAdapter, emptyViewId, itemTouchListener);
    }

    public static View inflate(LayoutInflater inflater, Context context, @Nullable ViewGroup parent, boolean attachToRoot,
                               @Nullable Integer layoutResourceId, RecyclerView.Adapter instanceOfAdapter,
                               @Nullable Integer emptyViewId, @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        Log.d(LOG_TAG, "inflate");
        View result = inflater.inflate(layoutResourceId == null
                ? R.layout.activity_recycler : layoutResourceId, parent, attachToRoot);
        RecyclerView recyclerView = (RecyclerView) result.findViewById(R.id.recyclerView);
        View emptyView = result.findViewById(emptyViewId == null ? R.id.empty_view : emptyViewId);
        setValues(recyclerView, context, instanceOfAdapter, emptyView, itemTouchListener);
        return result;
    }

    private static void setValues(RecyclerView recyclerView, Context context, RecyclerView.Adapter instanceOfAdapter,
                                  @Nullable View emptyView, @Nullable RecyclerItemClickListener.ClickListener itemTouchListener) {
        Log.d(LOG_TAG, "setValues");
        if (instanceOfAdapter instanceof RecyclerAdapter)
            ((RecyclerAdapter) instanceOfAdapter).setEmptyView(emptyView);

        //recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new SpecialItemAnimator(false));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (itemTouchListener != null)
            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener
                    (context, recyclerView, itemTouchListener));
        recyclerView.setAdapter(instanceOfAdapter);
        /*recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                LinearLayoutManager.VERTICAL));*/
    }
}
