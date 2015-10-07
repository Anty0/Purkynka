package cz.anty.purkynkamanager.update;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.io.IOException;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.list.recyclerView.specialAdapter.MultilineSpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class UpdateSpecialModule extends SpecialModule {

    private final SpecialItem[] mItems;
    private boolean updateAvailable = false;

    public UpdateSpecialModule(Context context) {
        super(context);
        mItems = new SpecialItem[]{
                new UpdateSpecialItem(context)
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
        try {
            UpdateReceiver.checkUpdate(getContext());
        } catch (IOException | NumberFormatException e) {
            Log.d(getClass().getSimpleName(), "onInitialize", e);
        }
        updateAvailable = UpdateReceiver
                .isUpdateAvailable(getContext());
        return true;
    }

    @Override
    protected void onUpdate() {
        boolean last = updateAvailable;
        onInitialize();
        if (last != updateAvailable)
            notifyItemsModified();
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

        public UpdateSpecialItem(Context context) {
            super(context);
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
            textData.append(String.format(context.getString(R.string
                    .notify_text_update_old), BuildConfig.VERSION_NAME))
                    .append("\n")
                    .append(String.format(context.getString(R.string.notify_text_update_new),
                            UpdateReceiver.getLatestName(context)));
            if (isShowDescription())
                textData.append("\n\n").append(context.getString(R.string.dialog_message_update_alert));

            return textData;
        }

        @Override
        public void onClick() {
            Context context = getContext();
            context.startActivity(new Intent(context, UpdateActivity.class)
                    .putExtra(UpdateActivity.EXTRA_SKIP_DIALOG, true));
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
