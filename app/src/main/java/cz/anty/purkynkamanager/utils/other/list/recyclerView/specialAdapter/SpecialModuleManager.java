package cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.SpecialItemAnimator;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.special.ICSpecialModule;
import cz.anty.purkynkamanager.utils.special.SASSpecialModule;
import cz.anty.purkynkamanager.utils.special.SFbSpecialModule;
import cz.anty.purkynkamanager.utils.special.ShareSpecialModule;
import cz.anty.purkynkamanager.utils.special.TimetableSpecialModule;
import cz.anty.purkynkamanager.utils.special.TrackingSpecialModule;
import cz.anty.purkynkamanager.utils.special.UpdateSpecialModule;
import cz.anty.purkynkamanager.utils.special.WifiSpecialModule;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class SpecialModuleManager {

    private static final String LOG_TAG = "SpecialModuleManager";
    private static final Comparator<SpecialItem> COMPARATOR = new Comparator<SpecialItem>() {
        @Override
        public int compare(SpecialItem lhs, SpecialItem rhs) {
            return rhs.getPriority() - lhs.getPriority();
        }
    };
    private final SharedPreferences mPreferences;
    private final SpecialRecyclerAdapter mAdapter;
    private final SpecialModule[] mModules;
    private final boolean mShowEnabled;
    private RecyclerInflater.RecyclerManager mRecyclerManager = null;
    private boolean mInitialized = false;

    private SpecialModuleManager(Context context, boolean
            showEnabled, SpecialModule... modules) {
        Log.d(LOG_TAG, "<init>");
        mModules = modules;
        mShowEnabled = showEnabled;
        mPreferences = context.getSharedPreferences
                (Constants.SETTINGS_NAME_MODULES, Context.MODE_PRIVATE);
        mAdapter = new SpecialRecyclerAdapter();
    }

    public static SpecialModuleManager getInstance(Context context, boolean showEnabled) {
        return new SpecialModuleManager(context, showEnabled,
                new UpdateSpecialModule(context), new SFbSpecialModule(context), new ShareSpecialModule(context),
                new TrackingSpecialModule(context), new SASSpecialModule(context), new ICSpecialModule(context),
                new TimetableSpecialModule(context), new WifiSpecialModule(context));
    }

    public boolean isInitialized() {
        Log.d(LOG_TAG, "isInitialized");
        return mInitialized;
    }

    public synchronized void bindRecyclerManager(@Nullable RecyclerInflater
            .RecyclerManager recyclerManager) {
        if (mRecyclerManager != null) {
            mRecyclerManager.setOnRefreshListener(null)
                    .setAdapter((RecyclerView.Adapter) null);
            mRecyclerManager = null;
        }
        if (recyclerManager == null) return;

        recyclerManager.setItemAnimator(new SpecialItemAnimator(true))
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        update();
                    }
                });
        synchronized (mAdapter) {
            recyclerManager.setAdapter(mAdapter);
        }
        mRecyclerManager = recyclerManager;
    }

    public synchronized void init() {
        Log.d(LOG_TAG, "init");
        if (mRecyclerManager != null)
            mRecyclerManager.setRefreshing(true);
        mInitialized = true;

        refreshItems();
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                module.init(mPreferences, this);
            }
        }
        if (mRecyclerManager != null)
            mRecyclerManager.setRefreshing(false);
    }

    public synchronized void update() {
        Log.d(LOG_TAG, "update");
        if (mRecyclerManager != null)
            mRecyclerManager.setRefreshing(true);
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                if (module.isInitialized())
                    module.update(mPreferences);
            }
        }
        refreshItems();
        if (mRecyclerManager != null)
            mRecyclerManager.setRefreshing(false);
    }

    public synchronized void saveState() {
        SharedPreferences.Editor editor = mPreferences.edit();
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                if (module.isInitialized())
                    module.onSaveState(editor);
            }
        }
        editor.apply();
    }

    /*void removeItem(SpecialItem item) {
        synchronized (mAdapter) {
            int index = mAdapter.getItemPosition(item);
            if (index != -1) {
                mAdapter.removeItem(item);
                //mAdapter.notifyItemRemoved(index);
            }
        }
    }

    void addItem(SpecialItem item) {
        synchronized (mAdapter) {
            int position = -1;
            ArrayList<SpecialItem> items = mAdapter.getItems();
            for (int i = 0, itemsSize = items.size(); i < itemsSize; i++) {
                if (items.get(i).getPriority() <= item.getPriority()) {
                    position = i;
                    break;
                }
            }
            if (position != -1)
                mAdapter.insertItem(item, position);
            else mAdapter.addItem(item);
        }
    }*/

    private ArrayList<SpecialItem> getAllItems() {
        ArrayList<SpecialItem> items = new ArrayList<>();
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                if (module.isInitialized())
                    Collections.addAll(items, module.getItems());
                else items.add(module.getLoadingItem());
            }
        }
        Collections.sort(items, COMPARATOR);
        return items;
    }

    private ArrayList<SpecialItem> getAllEnabledItems() {
        ArrayList<SpecialItem> items = new ArrayList<>();
        for (SpecialItem item : getAllItems()) {
            if (item.isVisible()) items.add(item);
        }
        return items;
    }

    private ArrayList<SpecialItem> getAllDisabledItems() {
        ArrayList<SpecialItem> items = new ArrayList<>();
        for (SpecialItem item : getAllItems()) {
            if (item instanceof SpecialItemHideImpl
                    && !((SpecialItemHideImpl) item)
                    .isEnabled()) items.add(item);
        }
        return items;
    }

    /*public void refreshItems() {
        synchronized (mAdapter) {
            Log.d(LOG_TAG, "refreshItems");
            mAdapter.clearItems();
            mAdapter.addAllItems(mShowEnabled ? getAllEnabledItems()
                    : getAllDisabledItems());
            mAdapter.notifyDataSetChanged();
        }
    }*/

    public void refreshItems() {
        synchronized (mAdapter) {
            SpecialItem firstItem = null;
            if (!mAdapter.isEmpty())
                firstItem = mAdapter.getItem(0);

            Log.d(LOG_TAG, "refreshItems");
            ArrayList<SpecialItem> items = mShowEnabled
                    ? getAllEnabledItems() : getAllDisabledItems();

            /*ArrayList<SpecialItem> removedItems = new ArrayList<>();
            ArrayList<SpecialItem> addedItems = new ArrayList<>();
            ArrayList<SpecialItem> movedItems = new ArrayList<>();
            for (SpecialItem item : oldItems) {
                if (items.contains(item)) {
                    movedItems.add(item);
                    continue;
                }
                removedItems.add(item);
            }
            for (SpecialItem item : items) {
                if (!oldItems.contains(item))
                    addedItems.add(item);
            }*/

            for (SpecialItem item : mAdapter.getItems()) {
                if (!items.contains(item)) {
                    mAdapter.removeItem(item);
                    /*mAdapter.notifyItemRemoved(mAdapter
                            .getItemPosition(item));*/
                }
            }

            for (SpecialItem item : items) {
                int index = mAdapter.getItemPosition(item);
                if (index != -1) {
                    mAdapter.setNotifyOnChange(false);
                    mAdapter.removeItem(item);
                    mAdapter.addItem(item);
                    mAdapter.notifyItemMoved(index,
                            mAdapter.getItemPosition(item));
                    mAdapter.setNotifyOnChange(true);
                    continue;
                }
                mAdapter.addItem(item);
                /*mAdapter.notifyItemInserted(mAdapter
                        .getItemPosition(item));*/
            }
            /*mAdapter.addAllItems(items);

            for (SpecialItem item : removedItems) {
                mAdapter.notifyItemRemoved(
                        oldItems.indexOf(item));
            }
            for (SpecialItem item : addedItems) {
                mAdapter.notifyItemInserted(
                        items.indexOf(item));
            }
            for (SpecialItem item : movedItems) {
                int from = oldItems.indexOf(item),
                        to = items.indexOf(item);
                if (from != to)
                    mAdapter.notifyItemMoved(from, to);
            }*/
            //mAdapter.notifyDataSetChanged();

            mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());

            if (firstItem != null && !mAdapter.isEmpty() &&
                    !mAdapter.getItem(0).equals(firstItem)) {
                mRecyclerManager.getRecyclerView().smoothScrollToPosition(0);
            }
        }
    }
}
