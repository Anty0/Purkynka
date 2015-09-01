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
            refreshThread.waitToWorkerStop();
            ICanteenBurzaActivity.this.binder.setOnBurzaChangeListener(null);
            ICanteenBurzaActivity.this.binder = null;
        }

    };

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
                final BurzaLunch lunch = item instanceof BurzaLunch ? (BurzaLunch) item : null;
                if (lunch == null) return;

                new AlertDialog.Builder(ICanteenBurzaActivity.this)
                        .setTitle(lunch.getName())
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage(lunch.getLunchNumber()
                                + "\n" + BurzaLunch.DATE_FORMAT.format(lunch.getDate())
                                + "\n" + lunch.getName())
                        .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (binder == null)
                                    Toast.makeText(ICanteenBurzaActivity.this, R.string.toast_text_can_not_order_burza_lunch, Toast.LENGTH_LONG).show();
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
                    data = dataList.toArray(new BurzaLunch[dataList.size()]);
                } catch (NullPointerException e) {
                    data = new MultilineItem[]{new TextMultilineItem(getString(R.string.exception_title_sas_manager_binder_null),
                            getString(R.string.exception_message_sas_manager_binder_null))};
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
            startBurzaChecker();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startBurzaChecker() {
        LinearLayout mainLinearLayout = new LinearLayout(this);
        mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mainLinearLayout.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);

        TextView dateTextView = new TextView(this);
        dateTextView.setText(R.string.text_view_text_date_to_watch);
        mainLinearLayout.addView(dateTextView);

        final DatePicker datePicker = new DatePicker(this);
        if (Build.VERSION.SDK_INT >= 11)
            datePicker.setMinDate(System.currentTimeMillis());
        mainLinearLayout.addView(datePicker);

        final TextView dateWrongTextView = new TextView(this);
        dateWrongTextView.setTextColor(Color.RED);
        dateWrongTextView.setText("You still have got lunch on this day.");//TODO to strings
        dateWrongTextView.setVisibility(View.GONE);
        mainLinearLayout.addView(dateWrongTextView);

        View.OnClickListener datePickerOnClickListener =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (binder != null) {
                            Calendar calendar = Calendar.getInstance();
                            for (MonthLunchDay monthLunchDay : binder.getMonth()) {
                                calendar.setTime(monthLunchDay.getDate());
                                if (calendar.get(Calendar.DAY_OF_MONTH) == datePicker.getDayOfMonth()
                                        && calendar.get(Calendar.MONTH) == datePicker.getMonth()
                                        && calendar.get(Calendar.YEAR) == datePicker.getYear()) {
                                    if (monthLunchDay.getOrderedLunch() == null)
                                        dateWrongTextView.setVisibility(View.GONE);
                                    else dateWrongTextView.setVisibility(View.VISIBLE);
                                    break;
                                }
                            }
                        } else dateWrongTextView.setVisibility(View.GONE);
                    }
                };
        datePicker.setOnClickListener(datePickerOnClickListener);
        datePickerOnClickListener.onClick(null);

        TextView lunchNumberTextView = new TextView(this);
        lunchNumberTextView.setText(R.string.text_view_text_numbers_to_watch);
        mainLinearLayout.addView(lunchNumberTextView);

        final CheckBox lunchCheckBox1 = new CheckBox(this);
        lunchCheckBox1.setText(BurzaLunch.LunchNumber.LUNCH_1.toString());
        lunchCheckBox1.setChecked(true);
        mainLinearLayout.addView(lunchCheckBox1);

        final CheckBox lunchCheckBox2 = new CheckBox(this);
        lunchCheckBox2.setText(BurzaLunch.LunchNumber.LUNCH_2.toString());
        lunchCheckBox2.setChecked(true);
        mainLinearLayout.addView(lunchCheckBox2);

        final CheckBox lunchCheckBox3 = new CheckBox(this);
        lunchCheckBox3.setText(BurzaLunch.LunchNumber.LUNCH_3.toString());
        lunchCheckBox3.setChecked(true);
        mainLinearLayout.addView(lunchCheckBox3);

        new AlertDialog.Builder(ICanteenBurzaActivity.this)
                .setTitle(R.string.notify_title_select_to_watch)
                .setView(mainLinearLayout)
                .setIcon(R.mipmap.ic_launcher)
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
                                Toast.makeText(ICanteenBurzaActivity.this, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
                            else
                                binder.startBurzaChecker(
                                        new BurzaLunchSelector(lunchNumbers.toArray(new BurzaLunch.LunchNumber[lunchNumbers.size()]),
                                                BurzaLunch.DATE_FORMAT.parse(format.format(datePicker.getDayOfMonth()) + "."
                                                        + format.format(datePicker.getMonth()) + "." + datePicker.getYear()))
                                );
                        } catch (ParseException e) {
                            Toast.makeText(ICanteenBurzaActivity.this, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.but_cancel, null)
                .setCancelable(true)
                .show();
    }
}
