package cz.anty.timetablemanager.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import cz.anty.timetablemanager.R;
import cz.anty.utils.Constants;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.timetable.Timetable;
import cz.anty.utils.timetable.TimetableManager;

/**
 * The configuration screen for the {@link TimetableLessonWidget TimetableLessonWidget} AppWidget.
 */
public class TimetableLessonWidgetConfigureActivity extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner mTimetableSpinner;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = TimetableLessonWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            Object item = mTimetableSpinner.getSelectedItem();
            Timetable timetable = item instanceof Timetable ? (Timetable) item : null;

            if (timetable != null) {
                savePref(context, mAppWidgetId, timetable);

                // It is the responsibility of the configuration activity to update the app widget
                TimetableLessonWidget.callUpdate(context);

                // Make sure we pass back the original appWidgetId
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        }
    };
    private MultilineAdapter adapter;
    private TimetableManager timetableManager;

    public TimetableLessonWidgetConfigureActivity() {
        super();
    }

    static void savePref(Context context, int appWidgetId, Timetable timetable) {
        context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, 0).edit()
                .putString(Constants.SETTING_NAME_APP_WIDGET + appWidgetId, timetable.getName()).apply();
    }

    static String loadPref(Context context, int appWidgetId) {
        return context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, 0)
                .getString(Constants.SETTING_NAME_APP_WIDGET + appWidgetId, null);
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        context.getSharedPreferences(Constants.SETTINGS_NAME_TIMETABLES, 0).edit()
                .remove(Constants.SETTING_NAME_APP_WIDGET + appWidgetId).apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.timetable_lesson_widget_configure);
        mTimetableSpinner = (Spinner) findViewById(R.id.spinner_timetable_select);
        findViewById(R.id.button_add_widget).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        timetableManager = new TimetableManager(this);
        adapter = new MultilineAdapter(this);
        mTimetableSpinner.setAdapter(adapter);
        init();
    }

    private void init() {
        adapter.setNotifyOnChange(false);
        adapter.clear();
        for (Timetable timetable : timetableManager.getTimetables()) {
            adapter.add(timetable);
        }
        adapter.notifyDataSetChanged();

        String name = loadPref(this, mAppWidgetId);
        if (name != null)
            mTimetableSpinner.setSelection(timetableManager
                    .getIndexOf(timetableManager.getTimetableByName(name)));
    }
}

