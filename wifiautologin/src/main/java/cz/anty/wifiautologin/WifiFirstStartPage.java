package cz.anty.wifiautologin;

import android.app.Activity;
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
public class WifiFirstStartPage implements FirstStartPage {

    private LinearLayout linearLayout;

    public WifiFirstStartPage() {
    }

    @Override
    public void initialize(Activity activity) {
        linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        activity.getLayoutInflater().inflate(R.layout.activity_wifi_login, linearLayout);
        activity.getLayoutInflater().inflate(R.layout.activity_wifi_settings, linearLayout);

        ((EditText) linearLayout.findViewById(R.id.edit_username)).
                setText(AppDataManager.getUsername(AppDataManager.Type.WIFI, activity));
        ((EditText) linearLayout.findViewById(R.id.edit_password))
                .setText(AppDataManager.getPassword(AppDataManager.Type.WIFI, activity));

        linearLayout.findViewById(R.id.but_login).setVisibility(View.GONE);
        linearLayout.findViewById(R.id.but_save).setVisibility(View.GONE);

        ((CheckBox) linearLayout.findViewById(R.id.auto_login_checkbox))
                .setChecked(AppDataManager.isWifiAutoLogin(activity));
        ((CheckBox) linearLayout.findViewById(R.id.wait_login_checkbox))
                .setChecked(AppDataManager.isWifiWaitLogin(activity));
    }

    @Override
    public boolean showThisPage(Context context) {
        return !AppDataManager.isLoggedIn(AppDataManager.Type.WIFI, context);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.app_name_wifi);
    }

    @Override
    public int getButSkipVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public int getButNextVisibility(Context context) {
        return View.VISIBLE;
    }

    @Override
    public String getButSkipText(Context context) {
        return context.getString(R.string.but_skip);
    }

    @Override
    public String getButNextText(Context context) {
        return context.getString(R.string.but_next);
    }

    @Override
    public View getView(Context context, LayoutInflater layoutInflater, ViewGroup rootView) {
        return linearLayout;
    }

    @Override
    public boolean doSkip(Context context) {
        return true;
    }

    @Override
    public boolean doFinish(Context context) {
        WifiLoginActivity.save(context,
                ((EditText) linearLayout.findViewById(R.id.edit_username)).getText().toString(),
                ((EditText) linearLayout.findViewById(R.id.edit_password)).getText().toString());

        AppDataManager.setWifiAutoLogin(context, ((CheckBox) linearLayout.findViewById(R.id.auto_login_checkbox)).isChecked());
        AppDataManager.setWifiWaitLogin(context, ((CheckBox) linearLayout.findViewById(R.id.wait_login_checkbox)).isChecked());
        return true;
    }
}