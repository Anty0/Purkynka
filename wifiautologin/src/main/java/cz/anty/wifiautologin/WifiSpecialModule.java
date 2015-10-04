package cz.anty.wifiautologin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialItem;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModule;

/**
 * Created by anty on 2.10.15.
 *
 * @author anty
 */
public class WifiSpecialModule extends SpecialModule {

    private boolean showItem = false;

    public WifiSpecialModule(Context context) {
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
        showItem = !AppDataManager.isLoggedIn
                (AppDataManager.Type.WIFI);
    }

    @Override
    protected void onUpdate() {
        if (AppDataManager.isLoggedIn(AppDataManager.Type.WIFI))
            showItem = false;
    }

    @Override
    protected SpecialItem[] getItems() {
        if (!showItem) return new SpecialItem[0];
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
                        title.setText(R.string.activity_title_wifi_login);
                        if (isShowDescription()) {
                            text.setVisibility(View.VISIBLE);
                            text.setText(R.string.app_description_wifi);
                            title.setPadding(1, 1, 1, 1);
                        } else {
                            text.setVisibility(View.GONE);
                            title.setPadding(1, 8, 1, 8);
                        }
                    }

                    @Override
                    public void onClick() {
                        getContext().startActivity(new Intent(getContext(), WifiLoginActivity.class));
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
                        return Constants.SPECIAL_ITEM_PRIORITY_WIFI_LOGIN;
                    }
                }
        };
    }

    @Override
    protected int getModuleNameResId() {
        return R.string.app_name_wifi;
    }
}
