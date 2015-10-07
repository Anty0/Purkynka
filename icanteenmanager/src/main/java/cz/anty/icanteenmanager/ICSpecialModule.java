package cz.anty.icanteenmanager;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 5.10.15.
 *
 * @author anty
 */
public class ICSpecialModule extends SpecialModule {

    private final SpecialItem[] mItems;

    public ICSpecialModule(Context context) {
        super(context);
        mItems = new SpecialItem[]{
                new ICLoginSpecialItem(context),
                new ICNewLunchesSpecialItem(context)
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
    protected boolean onInitialize() {
        ICSplashActivity.initService(getContext(), getWorker(),
                new Runnable() {
                    @Override
                    public void run() {
                        notifyInitializeCompleted();
                    }
                });
        return false;
    }

    @Override
    protected void onUpdate() {
        ICSplashActivity.initService(getContext(), getWorker(),
                new Runnable() {
                    @Override
                    public void run() {
                        notifyItemsModified();
                    }
                });
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

        public ICLoginSpecialItem(Context context) {
            super(context);
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

        public ICNewLunchesSpecialItem(Context context) {
            super(context);
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
        public void onHideClick() {
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
