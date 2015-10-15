package cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.list.recyclerView.SpecialItemAnimator;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class SpecialModuleManager {

    private static final Comparator<SpecialItem> COMPARATOR = new Comparator<SpecialItem>() {
        @Override
        public int compare(SpecialItem lhs, SpecialItem rhs) {
            return rhs.getPriority() - lhs.getPriority();
        }
    };

    private final Context mContext;
    private final SharedPreferences mPreferences;
    private final SpecialRecyclerAdapter mAdapter;
    private final SpecialModule[] mModules;
    private final boolean mShowEnabled;
    private boolean mInitialized = false;

    public SpecialModuleManager(RecyclerView recyclerView, boolean showEnabled, SpecialModule... modules) {
        Log.d(getClass().getSimpleName(), "<init>");
        mModules = modules;
        mShowEnabled = showEnabled;
        mContext = recyclerView.getContext();
        mPreferences = mContext.getSharedPreferences
                (Constants.SETTINGS_NAME_MODULES, Context.MODE_PRIVATE);
        mAdapter = new SpecialRecyclerAdapter();
        mAdapter.setNotifyOnChange(false);
        reInit(recyclerView);
    }

    public boolean isInitialized() {
        Log.d(getClass().getSimpleName(), "isInitialized");
        return mInitialized;
    }

    public synchronized void reInit(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setItemAnimator(new SpecialItemAnimator());
        synchronized (mAdapter) {
            recyclerView.setAdapter(mAdapter);
        }
    }

    public synchronized void init() {
        Log.d(getClass().getSimpleName(), "init");
        mInitialized = true;

        refreshItems();
        Runnable onChange = new Runnable() {
            @Override
            public void run() {
                new Handler(mContext.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                refreshItems();
                            }
                        });
            }
        };
        OnRemoveItem onRemoveItem = new OnRemoveItem() {
            @Override
            public void removeItem(final SpecialItem item) {
                new Handler(mContext.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                if (mShowEnabled) {
                                    SpecialModuleManager.this
                                            .removeItem(item);
                                    return;
                                }
                                refreshItems();
                            }
                        });
            }
        };

        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                module.init(mPreferences, onChange, onRemoveItem);
            }
        }
    }

    public synchronized void update() {
        Log.d(getClass().getSimpleName(), "update");
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                if (module.isInitialized())
                    module.update(mPreferences);
            }
        }
        refreshItems();
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

    private void removeItem(SpecialItem item) {
        synchronized (mAdapter) {
            int index = mAdapter.getItemPosition(item);
            if (index != -1) {
                mAdapter.removeItem(item);
                mAdapter.notifyItemRemoved(index);
            }
        }
    }

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

    public void refreshItems() {
        synchronized (mAdapter) {
            Log.d(getClass().getSimpleName(), "refreshItems");
            mAdapter.clearItems();
            mAdapter.addAllItems(mShowEnabled ? getAllEnabledItems()
                    : getAllDisabledItems());
            mAdapter.notifyDataSetChanged();
        }
    }

    /*public void refreshItems() {
        synchronized (mAdapter) {
            Log.d(getClass().getSimpleName(), "refreshItems");
            //List<SpecialItem> oldItems = mAdapter.getItems();
            ArrayList<SpecialItem> items = new ArrayList<>();
            for (SpecialItem item : getAllItems()) {
                if (item.isVisible()) items.add(item);
            }

            *//*ArrayList<SpecialItem> removedItems = new ArrayList<>();
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
            }*//*

            for (SpecialItem item : mAdapter.getItems()) {
                if (!items.contains(item)) {
                    mAdapter.removeItem(item);
                    mAdapter.notifyItemRemoved(mAdapter
                            .getItemPosition(item));
                }
            }

            for (SpecialItem item : items) {
                int index = mAdapter.getItemPosition(item);
                if (index != -1) {
                    mAdapter.removeItem(item);
                    mAdapter.notifyItemRemoved(index);
                    *//*mAdapter.addItem(item);
                    mAdapter.notifyItemMoved(index,
                            mAdapter.getItemPosition(item));
                    continue;*//*
                }
                mAdapter.addItem(item);
                mAdapter.notifyItemInserted(mAdapter
                        .getItemPosition(item));
            }
            *//*mAdapter.addAllItems(items);

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
            }*//*
            //mAdapter.notifyDataSetChanged();
        }
    }*/

    public interface OnRemoveItem {
        void removeItem(SpecialItem item);
    }
}
