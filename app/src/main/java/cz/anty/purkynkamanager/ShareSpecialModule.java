package cz.anty.purkynkamanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import cz.anty.utils.Constants;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class ShareSpecialModule extends SpecialModule {

    private final SpecialItem[] mItems;
    private boolean mShowShare = false;
    private SharedPreferences preferences;

    public ShareSpecialModule(Context context) {
        super(context);
        mItems = new SpecialItem[]{
                new ShareSpecialItem(context)
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
    protected boolean onInitialize() {
        preferences = getContext().getSharedPreferences(
                Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        onUpdate();
        return true;
    }

    @Override
    protected void onUpdate() {
        boolean last = mShowShare;
        mShowShare = preferences.getBoolean(Constants.SETTING_NAME_SHOW_SHARE, true);
        if (last != mShowShare)
            notifyItemsModified();
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

        public ShareSpecialItem(Context context) {
            super(context);
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

            preferences.edit().putBoolean(Constants.SETTING_NAME_SHOW_SHARE, false).apply();
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
