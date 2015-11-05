package cz.anty.purkynkamanager.utils.other.list.recyclerView.base;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.SpecialItemAnimator;

/**
 * Created by anty on 16.10.15.
 *
 * @author anty
 */
public final class RecyclerInflater {

    private static final String LOG_TAG = "RecyclerInflater";

    public static ActivityInflater inflateToActivity(Activity activity) {
        return new ActivityInflater(activity);
    }

    public static ViewInflater inflate(Context context, @Nullable ViewGroup parent,
                                       boolean attachToRoot) {
        return new ViewInflater(context, parent, attachToRoot);
    }

    private static RecyclerManager setValues(Context context, View mainView,
                                             boolean useSwipeRefresh) {
        Log.d(LOG_TAG, "setValues");
        SwipeRefreshLayout[] refreshLayouts = new SwipeRefreshLayout[]{
                (SwipeRefreshLayout) mainView.findViewById(R.id.recycler_swipe_refresh_layout),
                (SwipeRefreshLayout) mainView.findViewById(R.id.empty_swipe_refresh_layout)
        };

        EmptyRecyclerView recyclerView = (EmptyRecyclerView)
                mainView.findViewById(R.id.recyclerView);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setFrame(refreshLayouts[0]);
        recyclerView.setEmptyView(refreshLayouts[1]);
        recyclerView.setItemAnimator(new SpecialItemAnimator(false));
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        /*recyclerView.addItemDecoration(new DividerItemDecoration(activity,
                LinearLayoutManager.VERTICAL));*/
        return new RecyclerManager(context, mainView,
                recyclerView, refreshLayouts, useSwipeRefresh);
    }

    public interface Inflater {

        int DEFAULT_LAYOUT_ID = R.layout.activity_recycler;

        Inflater setLayoutResourceId(@LayoutRes @Nullable Integer layoutResourceId);

        Inflater useSwipeRefresh(boolean useSwipeRefresh);

        RecyclerManager inflate();
    }

    public static class ActivityInflater implements Inflater {

        private final Activity mActivity;
        @LayoutRes
        private Integer mLayoutResourceId = DEFAULT_LAYOUT_ID;
        private boolean mUseSwipeRefresh = true;

        private ActivityInflater(Activity activity) {
            mActivity = activity;
        }

        @Override
        public ActivityInflater setLayoutResourceId(@LayoutRes Integer layoutResourceId) {
            mLayoutResourceId = layoutResourceId;
            return this;
        }

        @Override
        public ActivityInflater useSwipeRefresh(boolean useSwipeRefresh) {
            mUseSwipeRefresh = useSwipeRefresh;
            return this;
        }

        @Override
        public RecyclerManager inflate() {
            mActivity.setContentView(mLayoutResourceId);
            return setValues(mActivity, mActivity.getWindow().getDecorView(), mUseSwipeRefresh);
        }
    }

    public static class ViewInflater implements Inflater {

        private final Context mContext;
        private final ViewGroup mParent;
        private final boolean mAttachToRoot;

        private LayoutInflater mInflater = null;
        @LayoutRes
        private Integer mLayoutResourceId = DEFAULT_LAYOUT_ID;
        private boolean mUseSwipeRefresh = true;

        private ViewInflater(Context context, @Nullable ViewGroup parent, boolean attachToRoot) {
            mContext = context;
            mParent = parent;
            mAttachToRoot = attachToRoot;
        }

        public ViewInflater setLayoutInflater(LayoutInflater inflater) {
            mInflater = inflater;
            return this;
        }

        @Override
        public ViewInflater setLayoutResourceId(@LayoutRes Integer layoutResourceId) {
            mLayoutResourceId = layoutResourceId;
            return this;
        }

        @Override
        public ViewInflater useSwipeRefresh(boolean useSwipeRefresh) {
            mUseSwipeRefresh = useSwipeRefresh;
            return this;
        }

        @Override
        public RecyclerManager inflate() {
            View result = (mInflater == null ? LayoutInflater.from(mContext) : mInflater)
                    .inflate(mLayoutResourceId, mParent, mAttachToRoot);
            return setValues(mContext, result, mUseSwipeRefresh);
        }
    }

    public static class RecyclerManager {

        private final static String LOG_TAG = "RecyclerManager";

        /*private final Runnable mUpdateSwipe = new Runnable() {
            @Override
            public void run() {
                boolean toSet;
                if (mRecyclerView.getChildCount() == 0) {
                    toSet = true;
                } else {
                    View child = mRecyclerView.getChildAt(0);
                    toSet = mRecyclerView.getChildAdapterPosition(child) == 0
                            && child.getTop() >= 0;
                }
                mSwipeRefreshLayout.setEnabled(toSet);
            }
        };
        private final Runnable mUpdateSwipePost = new Runnable() {
            @Override
            public void run() {
                mThreadHandler.postDelayed(
                        new Runnable() {
                    @Override
                    public void run() {
                        mUpdateSwipe.run();
                    }
                }, 500);
            }
        };*/

        private final Context mContext;
        private final Handler mThreadHandler;
        private final View mMainView;
        private final RecyclerView mRecyclerView;
        private final SwipeRefreshLayout[] mSwipeRefreshLayouts;

        private RecyclerManager(Context context, View mainView, RecyclerView
                recyclerView, SwipeRefreshLayout[] swipeRefreshLayouts, boolean useSwipeRefresh) {
            Log.d(LOG_TAG, "<init>");
            mContext = context;
            mThreadHandler = new Handler(context.getMainLooper());
            mMainView = mainView;
            mRecyclerView = recyclerView;
            mSwipeRefreshLayouts = swipeRefreshLayouts;

            for (SwipeRefreshLayout refreshLayout : mSwipeRefreshLayouts) {
                refreshLayout.setEnabled(useSwipeRefresh);
                refreshLayout.setColorSchemeColors(
                        ContextCompat.getColor(context, R.color.colorPrimary),
                        ContextCompat.getColor(context, R.color.colorPrimaryAS),
                        ContextCompat.getColor(context, R.color.colorPrimaryIC),
                        ContextCompat.getColor(context, R.color.colorPrimaryT),
                        ContextCompat.getColor(context, R.color.colorPrimaryW)
                );
            }
            /*if (mUseSwipeRefresh) {
                mSwipeRefreshLayout.addCanChildScrollUpListener(
                        new SpecialSwipeRefreshLayout.CanChildScrollUpListener() {
                            @Override
                            public boolean canChildScrollUp() {
                                RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                                if (adapter == null || adapter.getItemCount() == 0) return false;

                                View child = mRecyclerView.getChildAt(0);
                                return mRecyclerView.getChildAdapterPosition(child) != 0
                                        || child.getTop() < mRecyclerView.getPaddingTop();
                            }

                            @Override
                            public void onExtraTouchEvent(MotionEvent ev) {
                                mRecyclerView.onTouchEvent(ev);
                            }
                        });*/
                /*mRecyclerView.addOnScrollListener(
                        new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                mUpdateSwipe.run();
                            }
                        });
                final GestureDetector gestureDetector = new GestureDetector(context,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onDown(MotionEvent e) {
                                totalY = 0;
                                return super.onDown(e);
                            }

                            int totalY = 0;
                            @Override
                            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                                Log.d(LOG_TAG, "onScroll distanceX: " + distanceX + " distanceY: " + distanceY + " totalY: " + totalY);
                                totalY += distanceY;
                                if (totalY > 0) {
                                    totalY = 0;
                                    e2.setAction(MotionEvent.ACTION_UP);
                                    mSwipeRefreshLayout.onTouchEvent(e2);
                                    mSwipeRefreshLayout.setEnabled(false);
                                }
                                return false;
                            }
                        });
                mSwipeRefreshLayout.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return gestureDetector.onTouchEvent(event);
                    }
                });*/
            //}
        }

        public Context getContext() {
            Log.d(LOG_TAG, "getContext");
            return mContext;
        }

        public synchronized View getBaseView() {
            Log.d(LOG_TAG, "getBaseView");
            return mMainView;
        }

        public synchronized <T extends MultilineItem> RecyclerManager setAdapter
                (@LayoutRes int layoutResourceId, T... adapterData) {
            return setAdapter(new MultilineRecyclerAdapter<>(layoutResourceId, adapterData));
        }

        public synchronized <T extends MultilineItem> RecyclerManager setAdapter(T... adapterData) {
            return setAdapter(new MultilineRecyclerAdapter<>(adapterData));
        }

        public synchronized RecyclerManager setAdapter(RecyclerView.Adapter adapter) {
            Log.d(LOG_TAG, "setAdapter");
            mRecyclerView.setAdapter(adapter);
            return this;
        }

        public synchronized RecyclerManager setItemAnimator(RecyclerView.ItemAnimator animator) {
            Log.d(LOG_TAG, "setItemAnimator");
            mRecyclerView.setItemAnimator(animator);
            return this;
        }

        public RecyclerView getRecyclerView() {
            Log.d(LOG_TAG, "getRecyclerView");
            return mRecyclerView;
        }

        public synchronized RecyclerManager setItemTouchListener(RecyclerItemClickListener.ClickListener
                                                                         itemTouchListener) {
            Log.d(LOG_TAG, "setItemTouchListener");
            mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener
                    (mContext, mRecyclerView, itemTouchListener));
            return this;
        }

        public synchronized RecyclerManager setOnRefreshListener
                (SwipeRefreshLayout.OnRefreshListener listener) {
            Log.d(LOG_TAG, "setOnRefreshListener");
            for (SwipeRefreshLayout refreshLayout : mSwipeRefreshLayouts)
                refreshLayout.setOnRefreshListener(listener);
            return this;
        }

        public synchronized RecyclerManager setRefreshing(final boolean refreshing) {
            Log.d(LOG_TAG, "setRefreshing " + refreshing);
            final Runnable setRefreshing = new Runnable() {
                @Override
                public void run() {
                    for (SwipeRefreshLayout refreshLayout : mSwipeRefreshLayouts)
                        refreshLayout.setRefreshing(refreshing);
                }
            };

            if (mThreadHandler.getLooper().getThread().equals(Thread.currentThread()))
                setRefreshing.run();
            else mThreadHandler.post(setRefreshing);
            return this;
        }
    }
}
