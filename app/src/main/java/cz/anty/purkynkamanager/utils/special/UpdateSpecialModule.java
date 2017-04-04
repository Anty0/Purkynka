package cz.anty.purkynkamanager.utils.special;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.io.IOException;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModule;
import cz.anty.purkynkamanager.utils.update.UpdateActivity;
import cz.anty.purkynkamanager.utils.update.UpdateReceiver;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class UpdateSpecialModule extends SpecialModule {

    private static final String LOG_TAG = "UpdateSpecialModule";

    private final UpdateSpecialItem[] mItems;
    private boolean updateAvailable = false;

    public UpdateSpecialModule(Context context) {
        super(context);
        mItems = new UpdateSpecialItem[]{
                new UpdateSpecialItem()
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
        mItems[0].setEnabled(preferences
                .getBoolean(Constants.SETTING_NAME_ITEM_UPDATE, true));

        try {
            UpdateReceiver.checkUpdate(getContext());
        } catch (IOException | NumberFormatException e) {
            Log.d(LOG_TAG, "onInitialize", e);
        }
        updateAvailable = UpdateReceiver
                .isUpdateAvailable(getContext());
        return true;
    }

    @Override
    protected void onUpdate(SharedPreferences preferences) {
        boolean last = updateAvailable;
        onInitialize(preferences);
        if (last != updateAvailable)
            notifyItemsChanged();
        //notifyItemsModified();
    }

    @Override
    protected void onSaveState(SharedPreferences.Editor preferences) {
        preferences.putBoolean(Constants.SETTING_NAME_ITEM_UPDATE,
                !isUpdateAvailable() || mItems[0].isEnabled());
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    @Override
    protected SpecialItem[] getItems() {
        return mItems;
    }

    @Override
    protected CharSequence getModuleName() {
        return getContext().getText(R.string.app_name_updater);
    }

    private class UpdateSpecialItem extends MultilineSpecialItem {

        public UpdateSpecialItem() {
            super(UpdateSpecialModule.this);
        }

        @Nullable
        @Override
        protected Integer getImageId() {
            return R.mipmap.ic_launcher_no_border;
        }

        @Nullable
        @Override
        protected CharSequence getTitle() {
            return getContext().getText(R.string.notify_title_update_available);
        }

        @Nullable
        @Override
        protected CharSequence getText() {
            Context context = getContext();
            StringBuilder textData = new StringBuilder();
            textData.append(Utils.getFormattedText(context, R.string
                    .notify_text_update_old, BuildConfig.VERSION_NAME))
                    .append("\n")
                    .append(Utils.getFormattedText(context, R.string.notify_text_update_new,
                            UpdateReceiver.getLatestName(context)));
            if (isShowDescription())
                textData.append("\n\n").append(context.getString(R.string
                        .dialog_message_update_alert));

            return textData;
        }

        @Override
        public void onClick() {
            Context context = getContext();
            context.startActivity(new Intent(context, UpdateActivity.class));
        }

        @Override
        public boolean isShowHideButton() {
            return false;
        }

        @Override
        public boolean isVisible() {
            return super.isVisible() && isUpdateAvailable();
        }

        @Override
        public int getPriority() {
            return Constants.SPECIAL_ITEM_PRIORITY_NEW_UPDATE;
        }
    }
}
