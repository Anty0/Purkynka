package cz.anty.wifiautologin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.FirstStartPage;

/**
 * Created by anty on 29.8.15.
 *
 * @author anty
 */
public class WifiFirstStartPage extends FirstStartPage {

    private LinearLayout linearLayout;

    public WifiFirstStartPage(Context context) {
        super(context);
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.activity_wifi_login, linearLayout);
        inflater.inflate(R.layout.activity_wifi_settings, linearLayout);

        ((EditText) linearLayout.findViewById(R.id.edit_username)).
                setText(AppDataManager.getUsername(AppDataManager.Type.WIFI));
        ((EditText) linearLayout.findViewById(R.id.edit_password))
                .setText(AppDataManager.getPassword(AppDataManager.Type.WIFI));

        linearLayout.findViewById(R.id.but_login).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.but_save).setVisibility(View.GONE);

        ((CheckBox) linearLayout.findViewById(R.id.auto_login_checkbox))
                .setChecked(AppDataManager.isWifiAutoLogin());
        ((CheckBox) linearLayout.findViewById(R.id.wait_login_checkbox))
                .setChecked(AppDataManager.isWifiWaitLogin());
    }

    @Override
    public boolean showThisPage() {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.WIFI);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.app_name_wifi);
    }

    @Override
    public int getButSkipVisibility() {
        return View.VISIBLE;
    }

    @Override
    public int getButNextVisibility() {
        return View.VISIBLE;
    }

    @Override
    public String getButSkipText() {
        return getContext().getString(R.string.but_skip);
    }

    @Override
    public String getButNextText() {
        return getContext().getString(R.string.but_next);
    }

    @Override
    public View getView(ViewGroup rootView) {
        return linearLayout;
    }

    @Override
    public boolean doSkip() {
        return true;
    }

    @Override
    public boolean doFinish() {
        WifiLoginActivity.save(getContext(),
                ((EditText) linearLayout.findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) linearLayout.findViewById(R.id.edit_password)).getText().toString());

        AppDataManager.setWifiAutoLogin(((CheckBox) linearLayout.findViewById(R.id.auto_login_checkbox)).isChecked());
        AppDataManager.setWifiWaitLogin(((CheckBox) linearLayout.findViewById(R.id.wait_login_checkbox)).isChecked());
        return true;
    }
}