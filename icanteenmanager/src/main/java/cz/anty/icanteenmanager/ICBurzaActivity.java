package cz.anty.icanteenmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.Log;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunchSelector;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICBurzaActivity extends AppCompatActivity {

    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private ICService.ICBinder binder = null;
    private ServiceManager.BinderConnection<ICService.ICBinder> binderConnection
            = new ServiceManager.BinderConnection<ICService.ICBinder>() {
        @Override
        public void onBinderConnected(ICService.ICBinder ICBinder) {
            Log.d("ICBurzaActivity", "onBinderConnected");
            binder = ICBinder;
            refreshThread.startWorker(new Runnable() {
                @Override
                public void run() {
                    binder.setOnBurzaChangeListener(new Runnable() {
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

        @Override
        public void onBinderDisconnected() {
            Log.d("ICBurzaActivity", "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("ICBurzaActivity", "onBinderDisconnected", e);
            }
            binder.setOnBurzaChangeListener(null);
            binder = null;

        }
    };

    static void startBurzaChecker(final Context context, final ICService.ICBinder binder) {
        Log.d("ICBurzaActivity", "startBurzaChecker");

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
                                                lunchCheckBox3.setText(String.format("%1$s - %2$s",
                                                        BurzaLunch.LunchNumber.LUNCH_3.toString(), monthLunches[2].getName()));
                                            case 2:
                                                lunchCheckBox2.setText(String.format("%1$s - %2$s",
                                                        BurzaLunch.LunchNumber.LUNCH_2.toString(), monthLunches[1].getName()));
                                            case 1:
                                                lunchCheckBox1.setText(String.format("%1$s - %2$s",
                                                        BurzaLunch.LunchNumber.LUNCH_1.toString(), monthLunches[0].getName()));
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
                                Log.d("ICBurzaActivity", "startBurzaChecker datePickerOnDateChangedListener:", e);
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

                        try {
                            if (binder == null)
                                Toast.makeText(context, R.string.toast_text_can_not_start_burza_checker, Toast.LENGTH_LONG).show();
                            else {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
                                binder.startBurzaChecker(
                                        new BurzaLunchSelector(lunchNumbers.toArray(new BurzaLunch.LunchNumber[lunchNumbers.size()]),
                                                dateFormat.parse(datePicker.getDayOfMonth() + "."
                                                        + datePicker.getMonth() + "." + datePicker.getYear()))
                                );
                            }
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

        if (ICSplashActivity.serviceManager == null
                || !ICSplashActivity.serviceManager.isConnected()) {
            startActivity(new Intent(this, ICSplashActivity.class));
            finish();
            return;
        }

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        adapter = new MultilineRecyclerAdapter<>();
        RecyclerAdapter.inflateToActivity(this, null, adapter,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        MultilineItem item = adapter.getItem(position);

                        if (item instanceof TextMultilineItem &&
                                ((TextMultilineItem) item).getTag() == null) {
                            startBurzaChecker(ICBurzaActivity.this, binder);
                            return;
                        }

                        final BurzaLunch lunch = item instanceof BurzaLunch
                                ? (BurzaLunch) item : null;
                        if (lunch == null) return;

                        new AlertDialog.Builder(ICBurzaActivity.this)
                                .setTitle(lunch.getName())
                                        //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                                .setMessage(lunch.getLunchNumber()
                                        + "\n" + BurzaLunch.DATE_FORMAT.format(lunch.getDate())
                                        + "\n" + lunch.getName())
                                .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (binder == null)
                                            Toast.makeText(ICBurzaActivity.this, R.string.toast_text_can_not_order_lunch, Toast.LENGTH_LONG).show();
                                        else binder.orderBurzaLunch(lunch);
                                    }
                                })
                                .setNegativeButton(R.string.but_cancel, null)
                                .setCancelable(true)
                                .show();
                    }
                });

        if (ICSplashActivity.serviceManager != null) {
            ICSplashActivity.serviceManager
                    .addBinderConnection(binderConnection);
        }
    }

    @Override
    protected void onDestroy() {
        if (ICSplashActivity.serviceManager != null) {
            ICSplashActivity.serviceManager
                    .removeBinderConnection(binderConnection);
        }
        super.onDestroy();
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

                final MultilineItem[] finalData = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(finalData);
                    }
                });
            }
        }, getString(R.string.wait_text_loading));
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
