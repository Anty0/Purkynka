package cz.anty.purkynkamanager.utils.firststart.pages;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.wifi.WifiLoginActivity;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.FirstStartPage;

/**
 * Created by anty on 29.8.15.
 *
 * @author anty
 */
@Deprecated
public class WifiFirstStartPage extends FirstStartPage {

    private final LinearLayout linearLayout;

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

    public static boolean available() {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.WIFI);
    }

    @Override
    public boolean showThisPage() {
        return available();
    }

    @Override
    public CharSequence getTitle() {
        return getContext().getText(R.string.activity_title_wifi_login);
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_W;
    }

    @Override
    public View getView(ViewGroup rootView) {
        return linearLayout;
    }

    @Override
    public boolean doFinish() {
        WifiLoginActivity.save(
                ((EditText) linearLayout.findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) linearLayout.findViewById(R.id.edit_password)).getText().toString());

        AppDataManager.setWifiAutoLogin(((CheckBox) linearLayout.findViewById(R.id.auto_login_checkbox)).isChecked());
        AppDataManager.setWifiWaitLogin(((CheckBox) linearLayout.findViewById(R.id.wait_login_checkbox)).isChecked());
        return true;
    }
}