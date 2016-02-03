package cz.anty.purkynkamanager.utils.other;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Locale;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.list.widget.WidgetProvider;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThreadWithSpinner;
import cz.anty.purkynkamanager.utils.other.update.UpdateConnector;

/**
 * Created by anty on 17.10.15.
 *
 * @author anty
 */
public class Utils {

    private static final String LOG_TAG = "Utils";

    public static void generateChangeLogDialog(final Activity activity, @Nullable final Runnable onExit) {
        new OnceRunThreadWithSpinner(activity)
                .startWorker(new Runnable() {
                    @Override
                    public void run() {
                        CharSequence changeLog;
                        try {
                            changeLog = UpdateConnector.getLatestChangeLog();
                        } catch (IOException e) {
                            Log.d(LOG_TAG, "generateChangeLogDialog", e);
                            changeLog = activity.getText(R.string.text_change_log);
                        }

                        final CharSequence finalChangeLog = changeLog;
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(activity, R.style.AppTheme_Dialog)
                                        .setTitle(R.string.activity_title_first_start_change_log)
                                        .setMessage(finalChangeLog)
                                        .setPositiveButton(R.string.but_back,
                                                onExit == null ? null :
                                                        new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                onExit.run();
                                                            }
                                                        })
                                        .setIcon(R.mipmap.ic_launcher)
                                        .setCancelable(false)
                                        .show();
                            }
                        });
                    }
                }, activity.getText(R.string.wait_text_loading));
    }

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

    public static void restoreLocale(Context context) {
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


    public static void generateFirstStartDialog
            (final Context context, Intent widgetStartIntent, Class<? extends WidgetProvider>
                    widgetClass, @StyleRes int dialogThemeRes, CharSequence dialogTitle,
             CharSequence dialogMessage, @DrawableRes int iconRes, final Runnable onSuccessfulExit) {
        View widget = null;
        try {
            RemoteViews remoteViews = WidgetProvider.getContent(context,
                    new int[0], widgetStartIntent, widgetClass,
                    WidgetProvider.ContentType.FULL_CONTENT);

            if (remoteViews != null)
                widget = remoteViews.apply(context, null);
        } catch (Exception e) {
            Log.d(LOG_TAG, "generateFirstStartDialog", e);
            widget = null;
        }

        new AlertDialog.Builder(context, dialogThemeRes)
                .setTitle(dialogTitle)
                .setMessage(dialogMessage)
                .setIcon(iconRes)
                .setView(widget)
                .setPositiveButton(R.string.but_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        onSuccessfulExit.run();

                        AppWidgetHost widgetHost = new AppWidgetHost(context,
                                Constants.WIDGET_HOST_ID);
                        int appWidgetId = widgetHost.allocateAppWidgetId();
                        widgetHost.deleteHost();

                        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
                        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        context.startActivity(pickIntent);
                    }
                })
                .setNegativeButton(R.string.but_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSuccessfulExit.run();
                    }
                })
                .setNeutralButton(R.string.but_later, null)
                .setCancelable(false)
                .show();
    }


    public static void threadSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Log.d(LOG_TAG, "threadSleep", e);
            Thread.currentThread().interrupt();
        }
    }

    public static void threadSleep(long milis, int nanos) {
        try {
            Thread.sleep(milis, nanos);
        } catch (InterruptedException e) {
            Log.d(LOG_TAG, "threadSleep", e);
            Thread.currentThread().interrupt();
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

    /*@SuppressWarnings("deprecation")
    public static int getColor(Context context, @ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= 23)
            return context.getColor(colorRes);
        else return context.getResources().getColor(colorRes);
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, @DrawableRes int drawableRes) {
        if (Build.VERSION.SDK_INT >= 21)
            return context.getDrawable(drawableRes);
        else return context.getResources().getDrawable(drawableRes);
    }*/

    /*public static Bitmap getNotificationBitmap(Context context, int imageResourceId) {
        if (Build.VERSION.SDK_INT <= 10)
            return BitmapFactory.decodeResource(context
                    .getResources(), imageResourceId);

        Drawable image = getDrawable(context, imageResourceId);
        image.setColorFilter(new LightingColorFilter(Color.TRANSPARENT, Color.WHITE));
        Bitmap  bitmap = Bitmap.createBitmap(image.getMinimumWidth(),
                image.getMinimumHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        image.draw(canvas);
        return bitmap;
    }*/

    public static CharSequence getFormattedText(Context context, @StringRes int stringId,
                                                Object... args) {
        return getFormattedText(context.getString(stringId), args);
    }

    public static CharSequence getFormattedText(String text, Object... args) {
        return Html.fromHtml(String.format(text, args));
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo activeNetInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        WifiInfo wifiInfo = ((WifiManager) context
                .getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();

        return activeNetInfo != null && activeNetInfo.isConnected()
                && (!context.getSharedPreferences(Constants.SETTINGS_NAME_MAIN, Context.MODE_PRIVATE)
                .getBoolean(Constants.SETTING_NAME_USE_ONLY_WIFI, false) ||
                (wifiInfo != null && wifiInfo.getSSID() != null && !"<unknown ssid>".equals(wifiInfo.getSSID())));
    }
}
