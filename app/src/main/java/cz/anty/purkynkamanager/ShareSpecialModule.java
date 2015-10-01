package cz.anty.purkynkamanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.utils.Constants;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 1.10.15.
 *
 * @author anty
 */
public class ShareSpecialModule extends SpecialModule {

    private boolean mShowShare = false;
    private boolean hideItem = false;
    private SharedPreferences preferences;

    public ShareSpecialModule(Context context) {
        super(context);
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
    protected void onInitialize() {
        preferences = getContext().getSharedPreferences(
                Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE);
        onUpdate();
    }

    @Override
    protected void onUpdate() {
        mShowShare = preferences.getBoolean(Constants.SETTING_NAME_SHOW_SHARE, true);
    }

    public boolean isShowShare() {
        return mShowShare;
    }

    @Override
    protected SpecialItem[] getItems() {
        if (!isShowShare() || hideItem) {
            return new SpecialItem[0];
        }
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
                        title.setText(R.string.dialog_title_share);
                        text.setText(R.string.dialog_message_share);
                    }

                    @Override
                    public void onClick() {
                        Context context = getContext();
                        context.startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND)
                                        .setType("text/plain")
                                        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                                        .putExtra(Intent.EXTRA_TEXT, context.getString(R.string.text_extra_text_share)),
                                null));

                        preferences.edit().putBoolean(Constants.SETTING_NAME_SHOW_SHARE, false).apply();
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
                        return Constants.SPECIAL_ITEM_PRIORITY_SHARE;
                    }
                }
        };
    }

    @Override
    protected int getModuleNameResId() {
        return R.string.dialog_title_share;
    }
}
