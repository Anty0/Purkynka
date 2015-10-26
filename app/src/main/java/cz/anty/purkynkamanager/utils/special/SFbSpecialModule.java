package cz.anty.purkynkamanager.utils.special;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.main.SendFeedbackActivity;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 18.10.15.
 *
 * @author anty
 */
public class SFbSpecialModule extends SpecialModule {

    private final SFbSpecialItem[] mItems;
    private final SharedPreferences mPreferences;
    private boolean mShowSFb = false;

    public SFbSpecialModule(Context context) {
        super(context);
        mPreferences = context.getSharedPreferences(
                Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        mItems = new SFbSpecialItem[]{
                new SFbSpecialItem()
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
        mShowSFb = mPreferences.getInt(Constants
                .SETTING_NAME_LATEST_EXCEPTION_CODE, -1)
                == BuildConfig.VERSION_CODE;
        return true;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        boolean last = mShowSFb;
        onInitialize(preferences);
        if (last != mShowSFb)
            notifyItemsChanged();
        //notifyItemsModified();
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {

    }

    public boolean isShowSFb() {
        return mShowSFb;
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.activity_title_send_feedback);
    }

    private class SFbSpecialItem extends MultilineSpecialItem {

        public SFbSpecialItem() {
            super(SFbSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.drawable.ic_action_send;
        }

        @Nullable
        @Override
        protected CharSequence getTitle() {
            return getContext().getText(R.string.activity_title_send_feedback);
        }

        @Nullable
        @Override
        protected CharSequence getText() {
            if (isShowDescription())
                return getContext().getText(R.string.app_description_module_feedback);
            return null;
        }

        @Override
        public void onClick() {
            getContext().startActivity(new Intent(getContext(),
                    SendFeedbackActivity.class));
        }

        @Override
        public void onHideClick(boolean hide) {
            if (hide) {
                mPreferences.edit().putInt(Constants
                        .SETTING_NAME_LATEST_EXCEPTION_CODE, -1)
                        .apply();
                onUpdate(null);
            }
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && isShowSFb();
        }

        @Override
        public boolean isShowHideButton() {
            return true;
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_SEND_FEEDBACK;
        }
    }
}
