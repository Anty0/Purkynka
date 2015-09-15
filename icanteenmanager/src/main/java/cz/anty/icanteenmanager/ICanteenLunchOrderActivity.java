package cz.anty.icanteenmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.List;

import cz.anty.utils.Log;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICanteenLunchOrderActivity extends AppCompatActivity {

    private MultilineAdapter adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private ICanteenService.MyBinder binder = null;
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.d("LunchOrderActivity", "onServiceConnected");
            ICanteenLunchOrderActivity.this.binder = (ICanteenService.MyBinder) binder;
            refreshThread.startWorker(new Runnable() {
                @Override
                public void run() {
                    ICanteenLunchOrderActivity.this.binder.setOnMonthChangeListener(new Runnable() {
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
            Log.d("LunchOrderActivity", "onServiceDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("LunchOrderActivity", "onServiceDisconnected", e);
            }
            ICanteenLunchOrderActivity.this.binder.setOnMonthChangeListener(null);
            ICanteenLunchOrderActivity.this.binder = null;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MultilineItem item = adapter.getItem(position);
                final MonthLunchDay lunch = item instanceof MonthLunchDay ? (MonthLunchDay) item : null;
                if (lunch == null) return;

                ScrollView mainScrollView = new ScrollView(ICanteenLunchOrderActivity.this);

                final RadioGroup radioGroup = new RadioGroup(ICanteenLunchOrderActivity.this);
                radioGroup.setOrientation(LinearLayout.VERTICAL);
                mainScrollView.addView(radioGroup);

                RadioButton radioButtonNoLunch = new RadioButton(ICanteenLunchOrderActivity.this);
                radioButtonNoLunch.setTag(null);
                radioButtonNoLunch.setText("No lunch");
                radioButtonNoLunch.setId(R.id.text_view_title);
                radioGroup.addView(radioButtonNoLunch);
                //radioButtonNoLunch.setChecked(true);

                int toCheck = radioButtonNoLunch.getId();

                MonthLunch[] lunches = lunch.getLunches();
                for (int i = 0, lunchesLength = lunches.length; i < lunchesLength; i++) {
                    MonthLunch monthLunch = lunches[i];

                    RadioButton radioButtonLunch = new RadioButton(ICanteenLunchOrderActivity.this);
                    radioButtonLunch.setTag(monthLunch);
                    radioButtonLunch.setText(monthLunch.getName());
                    radioButtonLunch.setId(R.id.text_view_title + 1 + i);
                    radioGroup.addView(radioButtonLunch);

                    switch (monthLunch.getState()) {
                        case ORDERED:
                            radioButtonNoLunch.setTag(monthLunch);
                        case DISABLED_ORDERED:
                            radioButtonLunch.setTag(null);
                            toCheck = radioButtonLunch.getId();
                            break;
                        case DISABLED:
                            radioButtonLunch.setEnabled(false);
                            break;
                    }
                }
                if (radioButtonNoLunch.getTag() == null
                        && radioButtonNoLunch.getId() != toCheck)
                    radioButtonNoLunch.setEnabled(false);
                radioGroup.check(toCheck);

                new AlertDialog.Builder(ICanteenLunchOrderActivity.this)
                        .setTitle(MonthLunchDay.DATE_PARSE_FORMAT.format(lunch.getDate()))
                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                        .setView(mainScrollView)
                        .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (binder == null)
                                    Toast.makeText(ICanteenLunchOrderActivity.this, R.string.toast_text_can_not_order_lunch, Toast.LENGTH_LONG).show();
                                else {
                                    MonthLunch monthLunchToOrder = (MonthLunch)
                                            radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()).getTag();
                                    if (monthLunchToOrder != null)
                                        binder.orderMonthLunch(monthLunchToOrder);
                                    update();
                                }
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
                    List<MonthLunchDay> dataList = binder.getMonth();
                    data = dataList.toArray(new MonthLunchDay[dataList.size()]);
                } catch (NullPointerException | InterruptedException e) {
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
        Log.d("LunchOrderActivity", "onStart");
        super.onStart();
        bindService(new Intent(this, ICanteenService.class),
                mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        Log.d("LunchOrderActivity", "onStop");
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
                binder.refreshMonth();
            update();
            return true;
        }
        if (id == R.id.action_start_burza) {
            ICanteenBurzaActivity.startBurzaChecker(this, binder);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
