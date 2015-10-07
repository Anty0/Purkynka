package cz.anty.utils.list.recyclerView.specialAdapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import cz.anty.utils.Log;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public class SpecialModuleManager {

    private final Context mContext;
    private final SpecialRecyclerAdapter mAdapter;
    private final SpecialModule[] mModules;
    private boolean mInitialized = false;

    public SpecialModuleManager(RecyclerView recyclerView, SpecialModule... modules) {
        Log.d(getClass().getSimpleName(), "<init>");
        mModules = modules;
        mContext = recyclerView.getContext();
        mAdapter = new SpecialRecyclerAdapter();
        mAdapter.setNotifyOnChange(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(mAdapter);
    }

    public boolean isInitialized() {
        Log.d(getClass().getSimpleName(), "isInitialized");
        return mInitialized;
    }

    public synchronized void reInit(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(mAdapter);
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
        Runnable onModify = new Runnable() {
            @Override
            public void run() {
                new Handler(mContext.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.onDataSetChanged();
                                //mAdapter.notifyDataSetChanged();
                            }
                        });
            }
        };

        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                module.init(onChange, onModify);
            }
        }
    }

    public synchronized void update() {
        Log.d(getClass().getSimpleName(), "update");
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                if (module.isInitialized())
                    module.update();
            }
        }
        refreshItems();
    }

    private synchronized void refreshItems() {
        Log.d(getClass().getSimpleName(), "refreshItems");
        ArrayList<SpecialItem> items = new ArrayList<>();
        synchronized (mModules) {
            for (SpecialModule module : mModules) {
                if (module.isInitialized())
                    Collections.addAll(items, module.getItems());
                else items.add(module.getLoadingItem());
            }
        }
        mAdapter.clearItems();
        mAdapter.addAllItems(items);
        mAdapter.onDataSetChanged();
        //mAdapter.notifyDataSetChanged();
    }
}
