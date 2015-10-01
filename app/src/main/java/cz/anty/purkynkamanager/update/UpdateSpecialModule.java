package cz.anty.purkynkamanager.update;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class UpdateSpecialModule extends SpecialModule {

    private boolean updateAvailable = false;
    private boolean hideItem = false;

    public UpdateSpecialModule(Context context) {
        super(context);
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
    protected void onInitialize() {
        try {
            UpdateReceiver.checkUpdate(getContext());
        } catch (IOException | NumberFormatException e) {
            Log.d(getClass().getSimpleName(), "onInitialize", e);
        }
        updateAvailable = UpdateReceiver
                .isUpdateAvailable(getContext());
    }

    @Override
    protected void onUpdate() {
        onInitialize();
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    @Override
    protected SpecialItem[] getItems() {
        if (isUpdateAvailable())
            return new SpecialItem[]{
                    new SpecialItem() {
                        private TextView title, text;

                        @Override
                        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
                            LayoutInflater.from(getContext()).inflate(R.layout
                                    .base_multiline_text_item, parent);
                            title = (TextView) parent.findViewById(R.id.text_view_title);
                            text = (TextView) parent.findViewById(R.id.text_view_text);
                        }

                        @Override
                        public void onBindViewHolder(int itemPosition) {
                            Context context = getContext();
                            title.setText(R.string.notify_title_update_available);

                            StringBuilder textData = new StringBuilder();
                            textData.append(String.format(context.getString(R.string
                                    .notify_text_update_old), BuildConfig.VERSION_NAME)).append("\n")
                                    .append(String.format(context.getString(R.string.notify_text_update_new),
                                            UpdateReceiver.getLatestName(context)));
                            if (isShowDescription())
                                textData.append("\n\n").append(context.getString(R.string.dialog_message_update_alert));

                            text.setText(textData.toString());
                        }

                        @Override
                        public void onClick() {
                            Context context = getContext();
                            context.startActivity(new Intent(context, UpdateActivity.class)
                                    .putExtra(UpdateActivity.EXTRA_SKIP_DIALOG, true));
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
                            return Constants.SPECIAL_ITEM_PRIORITY_NEW_UPDATE;
                        }
                    }
            };

        if (!hideItem)
            return new SpecialItem[]{
                    new SpecialItem() {
                        private TextView title, text;

                        @Override
                        public void onCreateViewHolder(FrameLayout parent, int itemPosition) {
                            LayoutInflater.from(getContext()).inflate(R.layout
                                    .base_multiline_text_item, parent);
                            title = (TextView) parent.findViewById(R.id.text_view_title);
                            text = (TextView) parent.findViewById(R.id.text_view_text);
                        }

                        @Override
                        public void onBindViewHolder(int itemPosition) {
                            title.setText(R.string.notify_title_no_update);
                            text.setText(R.string.notify_text_no_update);
                        }

                        @Override
                        public void onClick() {

                        }

                        @Override
                        public void onLongClick() {

                        }

                        @Override
                        public void onHideClick() {
                            hideItem = true;
                            notifyItemsChanged();
                        }

                        @Override
                        public boolean isShowHideButton() {
                            return true;
                        }

                        @Override
                        public int getPriority() {
                            return Constants.SPECIAL_ITEM_PRIORITY_NO_UPDATE;
                        }
                    }
            };
        return new SpecialItem[0];
    }

    @Override
    protected int getModuleNameResId() {
        return R.string.app_name_updater;
    }
}
