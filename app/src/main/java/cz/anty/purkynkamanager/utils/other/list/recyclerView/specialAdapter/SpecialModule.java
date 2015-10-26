package cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

/**
 * Created by anty on 30.9.15.
 *
 * @author anty
 */
public abstract class SpecialModule {

    private static final String LOG_TAG = "SpecialModule";

    private final Context mContext;
    private final boolean mShowDescription;
    private final OnceRunThread worker;

    private boolean mInitializeStarted = false;
    private boolean mInitialized = false;
    private SpecialModuleManager mModuleManager = null;

    public SpecialModule(Context context) {
        Log.d(LOG_TAG, "<init>");
        mContext = context;
        mShowDescription = mContext.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        worker = new OnceRunThread(context);
    }

    public boolean isShowDescription() {
        Log.d(LOG_TAG, "isShowDescription");
        return mShowDescription;
    }

    public synchronized final boolean isInitialized() {
        Log.d(LOG_TAG, "isInitialized");
        return mInitialized;
    }

    protected abstract boolean isInitOnThread();

    protected abstract boolean isUpdateOnThread();

    final synchronized void init(final SharedPreferences preferences,
                                 SpecialModuleManager moduleManager) {
        Log.d(LOG_TAG, "init");
        if (mInitializeStarted || mInitialized) {
            throw new IllegalStateException(LOG_TAG + " is still initialized.");
        }
        mInitializeStarted = true;
        mModuleManager = moduleManager;

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
        Log.d(LOG_TAG, "update");
        if (!mInitialized) {
            throw new IllegalStateException(LOG_TAG + " is not initialized.");
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
        Log.d(LOG_TAG, "getLoadingItem");
        return new SpecialItem() {
            TextView title, text;

            @Override
            public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
                LayoutInflater.from(parent.getContext()).inflate(R.layout
                        .base_list_item_multi_line_loading, parent);
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
        Log.d(LOG_TAG, "notifyInitializeCompleted");
        if (mInitialized)
            throw new IllegalStateException(LOG_TAG + " is still initialized.");
        mInitialized = true;
        notifyItemsChanged();
    }

    protected synchronized final void notifyItemsChanged() {
        Log.d(LOG_TAG, "notifyItemsChanged");
        if (isInitialized() && mModuleManager != null)
            new Handler(mContext.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            mModuleManager.refreshItems();
                        }
                    });
    }

    /*protected synchronized final void notifyItemRemoved(final SpecialItem item) {
        Log.d(LOG_TAG, "notifyItemsChanged");
        if (isInitialized() && mModuleManager != null)
            new Handler(mContext.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            mModuleManager.removeItem(item);
                        }
                    });
    }

    protected synchronized final void notifyItemAdded(final SpecialItem item) {
        Log.d(LOG_TAG, "notifyItemsChanged");
        if (isInitialized() && mModuleManager != null)
            new Handler(mContext.getMainLooper())
                    .post(new Runnable() {
                        @Override
                        public void run() {
                            mModuleManager.addItem(item);
                        }
                    });
    }*/
}
