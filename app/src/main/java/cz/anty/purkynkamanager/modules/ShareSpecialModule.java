package cz.anty.purkynkamanager.modules;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.Constants;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class ShareSpecialModule extends SpecialModule {

    private final ShareSpecialItem[] mItems;
    private final SharedPreferences mPreferences;
    private boolean mShowShare = false;

    public ShareSpecialModule(Context context) {
        super(context);
        mPreferences = context.getSharedPreferences(
                Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        mItems = new ShareSpecialItem[]{
                new ShareSpecialItem()
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
        onUpdate(preferences);
        return true;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        boolean last = mShowShare;
        mShowShare = mPreferences.getBoolean(Constants
                .SETTING_NAME_SHOW_SHARE, true);
        if (last != mShowShare)
            notifyItemsChanged();
        //notifyItemsModified();
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {

    }

    public boolean isShowShare() {
        return mShowShare;
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.dialog_title_share);
    }

    private class ShareSpecialItem extends MultilineSpecialItem {

        public ShareSpecialItem() {
            super(ShareSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.drawable.ic_action_like;
        }

        @Nullable
        @Override
        protected CharSequence getTitle() {
            return getContext().getText(R.string.dialog_title_share);
        }

        @Nullable
        @Override
        protected CharSequence getText() {
            return getContext().getText(R.string.dialog_message_share);
        }

        @Override
        public void onClick() {
            Context context = getContext();
            context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                            .putExtra(Intent.EXTRA_TEXT, context.getText(R.string.text_extra_text_share)),
                    null));

            mPreferences.edit().putBoolean(Constants.SETTING_NAME_SHOW_SHARE, false).apply();
            mShowShare = false;
            notifyItemsChanged();
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && isShowShare();
        }

        @Override
        public boolean isShowHideButton() {
            return false;
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_SHARE;
        }
    }
}
