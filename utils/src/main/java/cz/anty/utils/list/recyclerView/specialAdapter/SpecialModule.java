package cz.anty.utils.list.recyclerView.specialAdapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.utils.Constants;
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

    public SpecialModule(Context context) {
        mContext = context;
        mShowDescription = mContext.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true);
        worker = new OnceRunThread(context);
    }

    public synchronized boolean isShowDescription() {
        return mShowDescription;
    }

    public synchronized final boolean isInitialized() {
        return mInitialized;
    }

    protected abstract boolean isInitOnThread();

    protected abstract boolean isUpdateOnThread();

    final synchronized void init(Runnable onChange) {
        if (mInitializeStarted) {
            throw new IllegalStateException(getClass()
                    .getSimpleName() + " is still initialized.");
        }
        mInitializeStarted = true;
        mOnChange = onChange;

        Runnable initRunnable = new Runnable() {
            @Override
            public void run() {
                onInitialize();
                mInitialized = true;
                notifyItemsChanged();
            }
        };

        if (isInitOnThread()) {
            worker.startWorker(initRunnable);
        }
        initRunnable.run();
    }

    protected abstract void onInitialize();

    final synchronized void update() {
        Runnable updateRunnable = new Runnable() {
            @Override
            public void run() {
                onUpdate();
                notifyItemsChanged();
            }
        };

        if (isUpdateOnThread()) {
            worker.startWorker(updateRunnable);
        }
        updateRunnable.run();
    }

    protected abstract void onUpdate();

    protected abstract SpecialItem[] getItems();

    protected SpecialItem getLoadingItem() {
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
                title.setText(getModuleNameResId());
                text.setText(R.string.wait_text_loading);
            }

            @Override
            public void onClick() {

            }

            @Override
            public void onLongClick() {

            }

            @Override
            public void onHideClick() {

            }

            @Override
            public boolean isShowHideButton() {
                return false;
            }

            @Override
            public int getPriority() {
                return 75;
            }
        };
    }

    @StringRes
    protected abstract int getModuleNameResId();

    public Context getContext() {
        return mContext;
    }

    public synchronized final void notifyItemsChanged() {
        if (isInitialized() && mOnChange != null)
            mOnChange.run();
    }
}
