package cz.anty.purkynkamanager.utils.special;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.modules.icanteen.widget.ICTodayLunchWidget;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;

/**
 * Created by anty on 5.10.15.
 *
 * @author anty
 */
public class ICSpecialModule extends SpecialModule {

    private static final String LOG_TAG = "ICSpecialModule";

    private final MultilineSpecialItem[] mItems;

    public ICSpecialModule(Context context) {
        super(context);
        mItems = new MultilineSpecialItem[]{
                new ICLoginSpecialItem(),
                new ICNextLunchSpecialItem(),
                new ICNewLunchesSpecialItem()
        };
    }

    @Override
    protected boolean isInitOnThread() {
        return true;
    }

    @Override
    protected boolean isUpdateOnThread() {
        return true;
    }

    @Override
    protected boolean onInitialize(SharedPreferences preferences) {
        onUpdate(preferences);
        return false;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        mItems[0].setEnabled(preferences.getBoolean(Constants
                .SETTING_NAME_ITEM_IC_LOGIN, true));
        mItems[1].setEnabled(preferences.getBoolean(Constants
                .SETTING_NAME_ITEM_IC_NEXT_LUNCH, true));

        ICSplashActivity.initService(getContext(), getWorker(),
                new Runnable() {
                    @Override
                    public void run() {
                        if (!isInitialized()) {
                            notifyInitializeCompleted();
                            return;
                        }
                        notifyItemsChanged();
                        //notifyItemsModified();
                    }
                });
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_IC_LOGIN, mItems[0].isEnabled())
                .putBoolean(Constants.SETTING_NAME_ITEM_IC_NEXT_LUNCH, mItems[1].isEnabled());
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.app_name_icanteen);
    }

    private class ICLoginSpecialItem extends MultilineSpecialItem {

        public ICLoginSpecialItem() {
            super(ICSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_ic_no_border;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_login_icanteen);
        }

        @Nullable
        @Override
        protected CharSequence getText() {
            if (isShowDescription())
                return getContext().getText(R.string.app_description_icanteen);
            return null;
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), ICSplashActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && !AppDataManager
                    .isLoggedIn(AppDataManager.Type.I_CANTEEN);
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_IC_LOGIN;
        }
    }

    private class ICNewLunchesSpecialItem extends MultilineSpecialItem {

        public ICNewLunchesSpecialItem() {
            super(ICSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_ic_no_border;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.special_item_title_icanteen_new_lunches);
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), ICSplashActivity.class));
        }

        @Override
        public void onHideClick(boolean hide) {
            AppDataManager.setICNewMonthLunches(false);
            notifyItemsChanged();
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN)
                    && AppDataManager.isICNewMonthLunches();
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_IC_NEW_LUNCHES;
        }
    }

    private class ICNextLunchSpecialItem extends MultilineSpecialItem {

        private FrameLayout mFrameLayout;

        public ICNextLunchSpecialItem() {
            super(ICSpecialModule.this);
        }

        @Override
        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
            super.onCreateViewHolder(parent, itemPosition);
            mFrameLayout = new FrameLayout(getContext());
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_ic_no_border;
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.widget_label_next_lunch);
        }

        @Nullable
        @Override
        protected View getContentView(ViewGroup parent) {
            Context context = getContext();
            mFrameLayout.removeAllViews();
            try {
                RemoteViews remoteViews = ICTodayLunchWidget.getContent(context, new int[0],
                        new Intent(), ICTodayLunchWidget.class,
                        WidgetProvider.ContentType.DATA_CONTENT);

                if (remoteViews != null) {
                    mFrameLayout.addView(remoteViews.apply(context, mFrameLayout));
                    return mFrameLayout;
                }
            } catch (Exception e) {
                Log.d(LOG_TAG, "ICNextLunchSpecialItem getContentView", e);
            }
            return super.getContentView(parent);
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), ICSplashActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && AppDataManager.isLoggedIn(AppDataManager.Type.I_CANTEEN);
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_IC_NEXT_LUNCH;
        }
    }
}
