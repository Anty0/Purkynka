package cz.anty.utils.list.recyclerView.specialAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.R;
import cz.anty.utils.thread.OnceRunThread;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public abstract class SpecialModule {

    private final Context mContext;
    private final boolean mShowDescription;
    private final OnceRunThread worker;

    private boolean mInitializeStarted = false;
    private boolean mInitialized = false;
    private Runnable mOnChange = null;
    private SpecialModuleManager.OnRemoveItem mOnRemoveItem = null;

    public SpecialModule(Context context) {
        Log.d(getClass().getSimpleName(), "<init>");
        mContext = context;
        mShowDescription = mContext.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        worker = new OnceRunThread(context);
    }

    public boolean isShowDescription() {
        Log.d(getClass().getSimpleName(), "isShowDescription");
        return mShowDescription;
    }

    public synchronized final boolean isInitialized() {
        Log.d(getClass().getSimpleName(), "isInitialized");
        return mInitialized;
    }

    protected abstract boolean isInitOnThread();

    protected abstract boolean isUpdateOnThread();

    final synchronized void init(final SharedPreferences preferences, Runnable onChange,
                                 SpecialModuleManager.OnRemoveItem onRemoveItem) {
        Log.d(getClass().getSimpleName(), "init");
        if (mInitializeStarted || mInitialized) {
            throw new IllegalStateException(getClass()
                    .getSimpleName() + " is still initialized.");
        }
        mInitializeStarted = true;
        mOnChange = onChange;
        mOnRemoveItem = onRemoveItem;

        Runnable initRunnable = new Runnable() {
            @Override
            public void run() {
                if (onInitialize(preferences))
                    notifyInitializeCompleted();
            }
        };

        if (isInitOnThread()) {
            worker.startWorker(initRunnable);
            return;
        }
        initRunnable.run();
    }

    protected abstract boolean onInitialize(SharedPreferences preferences);

    final synchronized void update(final SharedPreferences preferences) {
        Log.d(getClass().getSimpleName(), "update");
        if (!mInitialized) {
            throw new IllegalStateException(getClass()
                    .getSimpleName() + " is not initialized.");
        }

        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                onUpdate(preferences);
            }
        };

        if (isUpdateOnThread()) {
            worker.startWorker(updateRunnable);
            return;
        }
        updateRunnable.run();
    }

    protected abstract void onUpdate(SharedPreferences preferences);

    protected abstract void onSaveState(SharedPreferences.Editor preferences);

    protected OnceRunThread getWorker() {
        return worker;
    }

    protected abstract SpecialItem[] getItems();

    protected SpecialItem getLoadingItem() {
        Log.d(getClass().getSimpleName(), "getLoadingItem");
        return new SpecialItem() {
            TextView title, text;

            @Override
            public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
                LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .base_loading_multi_line_list_item, parent);
                title = (TextView) parent.findViewById(R.id.text_view_title);
                text = (TextView) parent.findViewById(R.id.text_view_text);
            }

            @Override
            public void onBindViewHolder(int itemPosition) {
                title.setText(getModuleName());
                text.setText(R.string.wait_text_loading);
            }

            @Override
            public int getPriority() {
                return Constants.SPECIAL_ITEM_PRIORITY_LOADING_ITEM;
            }
        };
    }

    @StringRes
    protected abstract CharSequence getModuleName();

    public Context getContext() {
        return mContext;
    }

    protected final void notifyInitializeCompleted() {
        Log.d(getClass().getSimpleName(), "notifyInitializeCompleted");
        if (mInitialized)
            throw new IllegalStateException(getClass()
                    .getSimpleName() + " is still initialized.");
        mInitialized = true;
        notifyItemsChanged();
    }

    protected synchronized final void notifyItemsChanged() {
        Log.d(getClass().getSimpleName(), "notifyItemsChanged");
        if (isInitialized() && mOnChange != null)
            mOnChange.run();
    }

    protected synchronized final void notifyItemRemoved(SpecialItem item) {
        Log.d(getClass().getSimpleName(), "notifyItemsChanged");
        if (isInitialized() && mOnRemoveItem != null)
            mOnRemoveItem.removeItem(item);
    }
}
