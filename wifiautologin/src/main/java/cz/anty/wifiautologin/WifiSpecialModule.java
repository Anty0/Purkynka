package cz.anty.wifiautologin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 2.10.15.
 *
 * @author anty
 */
public class WifiSpecialModule extends SpecialModule {

    private final WifiSpecialItem[] mItems;
    private boolean showItem = false;

    public WifiSpecialModule(Context context) {
        super(context);
        mItems = new WifiSpecialItem[]{
                new WifiSpecialItem()
        };
    }

    @Override
    protected boolean isInitOnThread() {
        return false;
    }

    @Override
    protected boolean isUpdateOnThread() {
        return false;
    }

    @Override
    protected boolean onInitialize(SharedPreferences preferences) {
        mItems[0].setEnabled(preferences.getBoolean(Constants
                .SETTING_NAME_ITEM_WIFI_LOGIN, true));

        showItem = !AppDataManager.isLoggedIn
                (AppDataManager.Type.WIFI);
        return true;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        boolean last = showItem;
        onInitialize(preferences);
        if (last != showItem)
            notifyItemsChanged();
        //notifyItemsModified();
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_WIFI_LOGIN,
                mItems[0].isEnabled());
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
    }

    private boolean isShowItem() {
        return showItem;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.app_name_wifi);
    }

    private class WifiSpecialItem extends MultilineSpecialItem {

        public WifiSpecialItem() {
            super(WifiSpecialModule.this);
        }

        @Nullable
        @Override
        public CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_wifi_login);
        }

        @Nullable
        @Override
        public CharSequence getText() {
            if (isShowDescription())
                return getContext().getText(R.string.app_description_wifi);
            return null;
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(), WifiLoginActivity.class));
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && isShowItem();
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_WIFI_LOGIN;
        }
    }
}
