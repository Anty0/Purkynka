package cz.anty.purkynkamanager.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Locale;

import cz.anty.purkynkamanager.R;

/**
 * Created by anty on 17.10.15.
 *
 * @author anty
 */
public class Utils {

    public static void generateLanguageChangeDialog(final Context context, @Nullable final Runnable onChange) {
        final RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(LinearLayout.VERTICAL);

        RadioButton radioButtonEnglish = new RadioButton(context);
        radioButtonEnglish.setTag("en");
        radioButtonEnglish.setText(R.string.radio_button_text_english);
        radioButtonEnglish.setId(R.id.text_view_title);
        radioGroup.addView(radioButtonEnglish);
        //radioButtonEnglish.setChecked(context.getString(R.string.language).equals("EN"));

        RadioButton radioButtonCzech = new RadioButton(context);
        radioButtonCzech.setTag("cs");
        radioButtonCzech.setText(R.string.radio_button_text_czech);
        radioButtonCzech.setId(R.id.text_view_text);
        radioGroup.addView(radioButtonCzech);
        //radioButtonCzech.setChecked(context.getString(R.string.language).equals("CS"));

        radioGroup.check(context.getString(R.string.language).equals("CS") ? R.id.text_view_text : R.id.text_view_title);

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

    private static void setLocale(Context context, String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, res.getDisplayMetrics());

        context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_LANGUAGE, lang)
                .apply();
    }

    static void restoreLocale(Context context) {
        String lang = context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_LANGUAGE, null);
        if (lang != null) {
            Locale myLocale = new Locale(lang);
            Resources res = context.getResources();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }
    }


    public static void setPadding(View view, int left, int top, int right, int bottom) {
        left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                left, view.getContext().getResources().getDisplayMetrics());
        right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                right, view.getContext().getResources().getDisplayMetrics());
        top = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                top, view.getContext().getResources().getDisplayMetrics());
        bottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                bottom, view.getContext().getResources().getDisplayMetrics());
        view.setPadding(left, top, right, bottom);
    }
}
