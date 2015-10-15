package cz.anty.purkynkamanager.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.icanteen.ICSplashActivity;
import cz.anty.purkynkamanager.utils.AppDataManager;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 5.10.15.
 *
 * @author anty
 */
public class ICSpecialModule extends SpecialModule {

    private final MultilineSpecialItem[] mItems;

    public ICSpecialModule(Context context) {
        super(context);
        mItems = new MultilineSpecialItem[]{
                new ICLoginSpecialItem(),
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
        mItems[0].setEnabled(preferences
                .getBoolean(Constants.SETTING_NAME_ITEM_IC_LOGIN, true));

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
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_IC_LOGIN,
                mItems[0].isEnabled());
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
}
