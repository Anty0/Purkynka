package cz.anty.purkynkamanager.modules.icanteen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;

/**
 * Created by anty on 13.10.15.
 *
 * @author anty
 */
public class ICBurzaCheckerActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ICBurzaCheckerActivity";

    private LinearLayout startLayout, stopLayout;
    private Button stopButton;
    private boolean mRunning = false;
    /*private final Runnable onStateChanged = new Runnable() {
        @Override
        public void run() {
            final boolean running = ICBurzaCheckerService.isRunning();
            final boolean stopping = ICBurzaCheckerService.isStopping();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startLayout.setVisibility(running ? View.GONE : View.VISIBLE);
                    stopLayout.setVisibility(running ? View.VISIBLE : View.GONE);
                    messageTextView.setText(stopping ? R.string.wait_text_stopping
                            : R.string.wait_text_running);
                    stopButton.setEnabled(!stopping);
                    invalidateOptionsMenu();
                    datePickerOnDateChangedListener.onDateChanged(datePicker, datePicker
                            .getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                }
            });
        }
    };*/
    private TextView wrongLunchTextView, messageTextView;
    private DatePicker datePicker;
    private CheckBox lunch1CheckBox, lunch2CheckBox, lunch3CheckBox;
    private final DatePicker.OnDateChangedListener datePickerOnDateChangedListener =
            new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    wrongLunchTextView.setVisibility(View.GONE);
                    lunch1CheckBox.setText(BurzaLunch.LunchNumber.LUNCH_1.toString());
                    lunch2CheckBox.setText(BurzaLunch.LunchNumber.LUNCH_2.toString());
                    lunch3CheckBox.setText(BurzaLunch.LunchNumber.LUNCH_3.toString());
                    lunch1CheckBox.setVisibility(View.VISIBLE);
                    lunch2CheckBox.setVisibility(View.VISIBLE);
                    lunch3CheckBox.setVisibility(View.VISIBLE);

                    if (ICSplashActivity.serviceManager != null &&
                            ICSplashActivity.serviceManager.isConnected()) {
                        ICService.ICBinder binder = ICSplashActivity
                                .serviceManager.getBinder();

                        Calendar calendar = Calendar.getInstance();
                        MonthLunchDay[] monthLunchDays = binder.getFullMonthFast();
                        if (monthLunchDays == null) monthLunchDays = new MonthLunchDay[0];
                        for (MonthLunchDay monthLunchDay : monthLunchDays) {
                            calendar.setTime(monthLunchDay.getDate());
                            if (calendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth
                                    && calendar.get(Calendar.MONTH) == monthOfYear
                                    && calendar.get(Calendar.YEAR) == year) {
                                if (monthLunchDay.getOrderedLunch() != null)
                                    wrongLunchTextView.setVisibility(View.VISIBLE);

                                MonthLunch[] monthLunches = monthLunchDay.getLunches();

                                switch (monthLunches.length) {
                                    case 3:
                                        lunch3CheckBox.setText(Utils.getFormattedText("%1$s - %2$s",
                                                BurzaLunch.LunchNumber.LUNCH_3.toString(),
                                                monthLunches[2].getName()));
                                    case 2:
                                        lunch2CheckBox.setText(Utils.getFormattedText("%1$s - %2$s",
                                                BurzaLunch.LunchNumber.LUNCH_2.toString(),
                                                monthLunches[1].getName()));
                                    case 1:
                                        lunch1CheckBox.setText(Utils.getFormattedText("%1$s - %2$s",
                                                BurzaLunch.LunchNumber.LUNCH_1.toString(),
                                                monthLunches[0].getName()));
                                }

                                switch (monthLunches.length) {
                                    case 0:
                                        lunch1CheckBox.setVisibility(View.GONE);
                                    case 1:
                                        lunch2CheckBox.setVisibility(View.GONE);
                                    case 2:
                                        lunch3CheckBox.setVisibility(View.GONE);
                                }

                                break;
                            }
                        }
                    }
                }
            };
    private final BroadcastReceiver onStateChangedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean running = intent.getBooleanExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_STATE_IS_RUNNING, false);
            final boolean stopping = intent.getBooleanExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_STATE_IS_STOPPING, false);
            mRunning = running;

            startLayout.setVisibility(running ? View.GONE : View.VISIBLE);
            stopLayout.setVisibility(running ? View.VISIBLE : View.GONE);
            messageTextView.setText(stopping ? R.string.wait_text_stopping
                    : R.string.wait_text_running);
            stopButton.setEnabled(!stopping);
            invalidateOptionsMenu();
            datePickerOnDateChangedListener.onDateChanged(datePicker, datePicker
                    .getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ICSplashActivity.serviceManager == null
                || !ICSplashActivity.serviceManager.isConnected()) {
            startActivity(new Intent(this, ICSplashActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_icanteen_burza_watcher);

        startLayout = (LinearLayout) findViewById(R.id.linear_layout_burza_watcher_start);
        wrongLunchTextView = (TextView) findViewById(R.id.text_view_wrong_lunch);
        datePicker = (DatePicker) findViewById(R.id.date_picker_burza_watcher);
        lunch1CheckBox = (CheckBox) findViewById(R.id.check_box_lunch_1);
        lunch2CheckBox = (CheckBox) findViewById(R.id.check_box_lunch_2);
        lunch3CheckBox = (CheckBox) findViewById(R.id.check_box_lunch_3);

        stopLayout = (LinearLayout) findViewById(R.id.linear_layout_burza_watcher_stop);
        messageTextView = (TextView) findViewById(R.id.message);
        stopButton = (Button) findViewById(R.id.button_stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ICSplashActivity.serviceManager != null &&
                        ICSplashActivity.serviceManager.isConnected()) {
                    ICSplashActivity.serviceManager.getBinder()
                            .stopBurzaChecker();
                }
            }
        });

        datePicker.init(datePicker.getYear(), datePicker.getMonth(),
                datePicker.getDayOfMonth(), datePickerOnDateChangedListener);
        datePickerOnDateChangedListener.onDateChanged(datePicker, datePicker
                .getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

        registerReceiver(onStateChangedListener, new IntentFilter(ICBurzaCheckerService.ACTION_STATE_CHANGED));
        startService(new Intent(this, ICBurzaCheckerService.class)
                .putExtra(ICBurzaCheckerService.EXTRA_BURZA_CHECKER_UPDATE_STATE, true));
        /*ICBurzaCheckerService.setOnStateChangedListener(onStateChanged);
        onStateChanged.run();*/
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(onStateChangedListener);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "onDestroy", e);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mRunning) return false;
        getMenuInflater().inflate(R.menu.menu_burza_watcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start) {
            List<BurzaLunch.LunchNumber> lunchNumbers = new ArrayList<>();
            if (lunch1CheckBox.isChecked())
                lunchNumbers.add(BurzaLunch.LunchNumber.LUNCH_1);
            if (lunch2CheckBox.isChecked())
                lunchNumbers.add(BurzaLunch.LunchNumber.LUNCH_2);
            if (lunch3CheckBox.isChecked())
                lunchNumbers.add(BurzaLunch.LunchNumber.LUNCH_3);

            try {
                if (ICSplashActivity.serviceManager != null &&
                        ICSplashActivity.serviceManager.isConnected()) {
                    ICService.ICBinder binder = ICSplashActivity
                            .serviceManager.getBinder();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
                    Log.d(LOG_TAG, "onOptionsItemSelected month: " + datePicker.getMonth());
                    binder.startBurzaChecker(
                            new BurzaLunchSelector(lunchNumbers.toArray(new BurzaLunch.LunchNumber[lunchNumbers.size()]),
                                    dateFormat.parse(datePicker.getDayOfMonth() + "."
                                            + (datePicker.getMonth() + 1) + "." + datePicker.getYear()))
                    );
                } else throw new Exception();
            } catch (Exception e) {
                Toast.makeText(this, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
