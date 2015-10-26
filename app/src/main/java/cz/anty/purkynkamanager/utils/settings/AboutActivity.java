package cz.anty.purkynkamanager.utils.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.list.listView.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.update.UpdateReceiver;

public class AboutActivity extends AppCompatActivity {

    private long lastClick = 0;
    private int clicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerInflater.inflateToActivity(this).useSwipeRefresh(false).inflate()
                .setAdapter(new TextMultilineItem(getText(R.string.list_item_text_installed_app_version), BuildConfig.VERSION_NAME),
                        new TextMultilineItem(getText(R.string.list_item_text_available_app_version), UpdateReceiver.getLatestName(this)),
                        new TextMultilineItem(getText(R.string.list_item_text_about_developer), getText(R.string.text_about)))
                .setItemTouchListener(new RecyclerItemClickListener.SimpleClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        if (position == 0 && System.currentTimeMillis() - lastClick < 1000)
                            clicks++;
                        else clicks = 0;
                        lastClick = System.currentTimeMillis();

                        if (clicks > 10) {
                            final SharedPreferences preferences = getSharedPreferences
                                    (Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE);
                            if (!preferences.getBoolean(Constants.SETTING_NAME_ITEM_UNLOCKED_BONUS, false)) {
                                new AlertDialog.Builder(AboutActivity.this, R.style.AppTheme_Dialog)
                                        .setTitle(R.string.dialog_title_bonus_unlocked)
                                        .setIcon(R.mipmap.ic_launcher_t)
                                        .setMessage(R.string.dialog_text_bonus_unlocked)
                                        .setPositiveButton(R.string.but_accept,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        preferences.edit().putBoolean(Constants
                                                                .SETTING_NAME_ITEM_UNLOCKED_BONUS, true)
                                                                .apply();
                                                    }
                                                })
                                        .setNegativeButton(R.string.but_cancel, null)
                                        .setCancelable(false)
                                        .show();
                            }
                        }
                    }
                });
    }
}
