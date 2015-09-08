package cz.anty.utils.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Locale;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.R;

public class AboutActivity extends AppCompatActivity {

    public static void generateLanguageChangeDialog(final Context context, @Nullable final Runnable onChange) {
        final RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(LinearLayout.VERTICAL);

        RadioButton radioButtonEnglish = new RadioButton(context);
        radioButtonEnglish.setTag("en");
        radioButtonEnglish.setText(R.string.radio_button_text_english);
        radioButtonEnglish.setId(R.id.txtTitle);
        radioGroup.addView(radioButtonEnglish);
        //radioButtonEnglish.setChecked(context.getString(R.string.language).equals("EN"));

        RadioButton radioButtonCzech = new RadioButton(context);
        radioButtonCzech.setTag("cs");
        radioButtonCzech.setText(R.string.radio_button_text_czech);
        radioButtonCzech.setId(R.id.txtTitle2);
        radioGroup.addView(radioButtonCzech);
        //radioButtonCzech.setChecked(context.getString(R.string.language).equals("CS"));

        radioGroup.check(context.getString(R.string.language).equals("CS") ? R.id.txtTitle2 : R.id.txtTitle);

        new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
                .setTitle(R.string.dialog_title_select_language)
                .setView(radioGroup)
                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setLocale(context, (String) radioGroup
                                .findViewById(radioGroup.getCheckedRadioButtonId())
                                .getTag());
                        if (onChange != null) onChange.run();
                    }
                })
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setNeutralButton(R.string.but_cancel, null)
                .show();
    }

    public static void setLocale(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, res.getDisplayMetrics());

        context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_LANGUAGE, lang)
                .apply();
    }

    public static void setLocale(Context context) {
        String lang = context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_LANGUAGE, null);
        if (lang != null) {
            Locale myLocale = new Locale(lang);
            Resources res = context.getResources();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SharedPreferences preferences = getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE);
        ((CheckBox) findViewById(R.id.check_box_debug)).setChecked(AppDataManager.isDebugMode());
        ((CheckBox) findViewById(R.id.check_box_show_description)).setChecked(
                preferences.getBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION, true));
        ((CheckBox) findViewById(R.id.check_box_first_start)).setChecked(
                preferences.getInt(Constants.SETTING_NAME_FIRST_START, -1) == -1);
    }

    public void onCheckBoxDebugClick(View view) {
        AppDataManager.setDebugMode(((CheckBox) findViewById(R.id.check_box_debug)).isChecked());
    }

    public void onCheckBoxShowDescriptionClick(View view) {
        getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE).edit()
                .putBoolean(Constants.SETTING_NAME_SHOW_DESCRIPTION,
                        ((CheckBox) findViewById(R.id.check_box_show_description))
                                .isChecked())
                .apply();
    }

    public void onCheckBoxFirstStartClick(View view) {
        if (((CheckBox) findViewById(R.id.check_box_first_start)).isChecked()) {
            getSharedPreferences(Constants.SETTINGS_NAME_MAIN, MODE_PRIVATE).edit()
                    .putInt(Constants.SETTING_NAME_FIRST_START, -1).apply();
            return;
        }
        ((CheckBox) findViewById(R.id.check_box_first_start)).setChecked(true);
    }

    public void onChangeLanguageClick(View view) {
        generateLanguageChangeDialog(this, new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(AboutActivity.this, AboutActivity.class));
                Toast.makeText(AboutActivity.this, R.string
                        .toast_text_restart_app_to_change_language, Toast.LENGTH_LONG).show();
            }
        });
    }
}
