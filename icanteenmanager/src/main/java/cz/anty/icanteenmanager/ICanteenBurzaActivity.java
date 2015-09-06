package cz.anty.icanteenmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICanteenBurzaActivity extends AppCompatActivity {

    private MultilineAdapter adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private ICanteenService.MyBinder binder = null;
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            if (AppDataManager.isDebugMode(ICanteenBurzaActivity.this))
                Log.d("ICanteenBurzaActivity", "onServiceConnected");
            ICanteenBurzaActivity.this.binder = (ICanteenService.MyBinder) binder;
            refreshThread.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICanteenBurzaActivity.this.binder.setOnBurzaChangeListener(new Runnable() {
                        @Override
                        public void run() {
                            update();
                        }
                    });
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            update();
                        }
                    });
                }
            });
        }

        public void onServiceDisconnected(ComponentName className) {
            if (AppDataManager.isDebugMode(ICanteenBurzaActivity.this))
                Log.d("ICanteenBurzaActivity", "onServiceDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("ICanteenBurzaActivity", "onServiceDisconnected", e);
            }
            ICanteenBurzaActivity.this.binder.setOnBurzaChangeListener(null);
            ICanteenBurzaActivity.this.binder = null;
        }

    };

    static void startBurzaChecker(final Context context, final ICanteenService.MyBinder binder) {
        if (AppDataManager.isDebugMode(context))
            Log.d("ICanteenBurzaActivity", "startBurzaChecker");

        ScrollView mainScrollView = new ScrollView(context);

        LinearLayout mainLinearLayout = new LinearLayout(context);
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        //mainLinearLayout.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        mainScrollView.addView(mainLinearLayout);

        TextView dateTextView = new TextView(context);
        dateTextView.setText(R.string.text_view_text_date_to_watch);
        mainLinearLayout.addView(dateTextView);

        final DatePicker datePicker = new DatePicker(context);
        if (Build.VERSION.SDK_INT >= 11) {
            datePicker.setCalendarViewShown(false);
            datePicker.setSpinnersShown(true);
        }
        mainLinearLayout.addView(datePicker);

        final TextView dateWrongTextView = new TextView(context);
        dateWrongTextView.setTextColor(Color.RED);
        dateWrongTextView.setText(R.string.text_view_text_you_still_have_got_lunch);
        dateWrongTextView.setVisibility(View.GONE);
        mainLinearLayout.addView(dateWrongTextView);

        TextView lunchNumberTextView = new TextView(context);
        lunchNumberTextView.setText(R.string.text_view_text_numbers_to_watch);
        mainLinearLayout.addView(lunchNumberTextView);

        final CheckBox lunchCheckBox1 = new CheckBox(context);
        lunchCheckBox1.setText(BurzaLunch.LunchNumber.LUNCH_1.toString());
        lunchCheckBox1.setChecked(true);
        mainLinearLayout.addView(lunchCheckBox1);

        final CheckBox lunchCheckBox2 = new CheckBox(context);
        lunchCheckBox2.setText(BurzaLunch.LunchNumber.LUNCH_2.toString());
        lunchCheckBox2.setChecked(true);
        mainLinearLayout.addView(lunchCheckBox2);

        final CheckBox lunchCheckBox3 = new CheckBox(context);
        lunchCheckBox3.setText(BurzaLunch.LunchNumber.LUNCH_3.toString());
        lunchCheckBox3.setChecked(true);
        mainLinearLayout.addView(lunchCheckBox3);

        DatePicker.OnDateChangedListener datePickerOnDateChangedListener =
                new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        dateWrongTextView.setVisibility(View.GONE);
                        lunchCheckBox1.setText(BurzaLunch.LunchNumber.LUNCH_1.toString());
                        lunchCheckBox2.setText(BurzaLunch.LunchNumber.LUNCH_2.toString());
                        lunchCheckBox3.setText(BurzaLunch.LunchNumber.LUNCH_3.toString());
                        lunchCheckBox1.setVisibility(View.VISIBLE);
                        lunchCheckBox2.setVisibility(View.VISIBLE);
                        lunchCheckBox3.setVisibility(View.VISIBLE);

                        if (binder != null) {
                            Calendar calendar = Calendar.getInstance();
                            try {
                                List<MonthLunchDay> monthLunchDays = binder.getMonth();
                                if (monthLunchDays == null) monthLunchDays = new ArrayList<>();
                                for (MonthLunchDay monthLunchDay : monthLunchDays) {
                                    calendar.setTime(monthLunchDay.getDate());
                                    if (calendar.get(Calendar.DAY_OF_MONTH) == dayOfMonth
                                            && calendar.get(Calendar.MONTH) == monthOfYear
                                            && calendar.get(Calendar.YEAR) == year) {
                                        if (monthLunchDay.getOrderedLunch() != null)
                                            dateWrongTextView.setVisibility(View.VISIBLE);

                                        MonthLunch[] monthLunches = monthLunchDay.getLunches();

                                        switch (monthLunches.length) {
                                            case 3:
                                                lunchCheckBox3.setText(BurzaLunch.LunchNumber.LUNCH_3.toString()
                                                        + " - " + monthLunches[2].getName());
                                            case 2:
                                                lunchCheckBox2.setText(BurzaLunch.LunchNumber.LUNCH_2.toString()
                                                        + " - " + monthLunches[1].getName());
                                            case 1:
                                                lunchCheckBox1.setText(BurzaLunch.LunchNumber.LUNCH_1.toString()
                                                        + " - " + monthLunches[0].getName());
                                        }

                                        switch (monthLunches.length) {
                                            case 0:
                                                lunchCheckBox1.setVisibility(View.GONE);
                                            case 1:
                                                lunchCheckBox2.setVisibility(View.GONE);
                                            case 2:
                                                lunchCheckBox3.setVisibility(View.GONE);
                                        }

                                        break;
                                    }
                                }
                            } catch (InterruptedException e) {
                                Log.d("ICanteenBurzaActivity", "startBurzaChecker datePickerOnDateChangedListener:", e);
                            }
                        }
                    }
                };
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), datePickerOnDateChangedListener);
        datePickerOnDateChangedListener.onDateChanged(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

        new AlertDialog.Builder(context)
                .setTitle(R.string.notify_title_select_to_watch)
                .setView(mainScrollView)
                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                .setPositiveButton(R.string.but_start, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<BurzaLunch.LunchNumber> lunchNumbers = new ArrayList<>();
                        if (lunchCheckBox1.isChecked())
                            lunchNumbers.add(BurzaLunch.LunchNumber.LUNCH_1);
                        if (lunchCheckBox2.isChecked())
                            lunchNumbers.add(BurzaLunch.LunchNumber.LUNCH_2);
                        if (lunchCheckBox3.isChecked())
                            lunchNumbers.add(BurzaLunch.LunchNumber.LUNCH_3);

                        DecimalFormat format = new DecimalFormat("##");
                        try {
                            if (binder == null)
                                Toast.makeText(context, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
                            else
                                binder.startBurzaChecker(
                                        new BurzaLunchSelector(lunchNumbers.toArray(new BurzaLunch.LunchNumber[lunchNumbers.size()]),
                                                BurzaLunch.DATE_FORMAT.parse(format.format(datePicker.getDayOfMonth()) + "."
                                                        + format.format(datePicker.getMonth()) + "." + datePicker.getYear()))
                                );
                        } catch (ParseException e) {
                            Toast.makeText(context, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.but_cancel, null)
                .setCancelable(true)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter(this, R.layout.text_multi_line_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MultilineItem item = adapter.getItem(position);

                if (item instanceof TextMultilineItem &&
                        ((TextMultilineItem) item).getTag() == null) {
                    startBurzaChecker(ICanteenBurzaActivity.this, binder);
                    return;
                }

                final BurzaLunch lunch = item instanceof BurzaLunch
                        ? (BurzaLunch) item : null;
                if (lunch == null) return;

                new AlertDialog.Builder(ICanteenBurzaActivity.this)
                        .setTitle(lunch.getName())
                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                        .setMessage(lunch.getLunchNumber()
                                + "\n" + BurzaLunch.DATE_FORMAT.format(lunch.getDate())
                                + "\n" + lunch.getName())
                        .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (binder == null)
                                    Toast.makeText(ICanteenBurzaActivity.this, R.string.toast_text_can_not_order_lunch, Toast.LENGTH_LONG).show();
                                else binder.orderBurzaLunch(lunch);
                            }
                        })
                        .setNegativeButton(R.string.but_cancel, null)
                        .setCancelable(true)
                        .show();
            }
        });
    }

    private void update() {
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                MultilineItem[] data;
                try {
                    List<BurzaLunch> dataList = binder.getBurza();
                    data = dataList.toArray(new MultilineItem[dataList.size() + 1]);
                    data[data.length - 1] = new TextMultilineItem(getString(
                            R.string.menu_item_text_start_burza_checking), null);
                } catch (NullPointerException | InterruptedException e) {
                    data = new MultilineItem[]{new TextMultilineItem(getString(R.string.exception_title_sas_manager_binder_null),
                            getString(R.string.exception_message_sas_manager_binder_null)).setTag("EXCEPTION")};
                }

                adapter.setNotifyOnChange(false);
                adapter.clear();
                for (MultilineItem item : data) {
                    adapter.add(item);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }, getString(R.string.wait_text_loading));
    }

    @Override
    protected void onStart() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenBurzaActivity", "onStart");
        super.onStart();
        bindService(new Intent(this, ICanteenService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (AppDataManager.isDebugMode(this)) Log.d("ICanteenBurzaActivity", "onStop");
        unbindService(mConnection);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_burza, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            if (binder != null)
                binder.refreshBurza();
            update();
            return true;
        }
        if (id == R.id.action_start_burza) {
            startBurzaChecker(this, binder);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
