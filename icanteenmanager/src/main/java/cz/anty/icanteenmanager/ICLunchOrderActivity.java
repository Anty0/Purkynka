package cz.anty.icanteenmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.anty.utils.Log;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.listItem.MultilineAdapter;
import cz.anty.utils.listItem.MultilineItem;
import cz.anty.utils.listItem.TextMultilineItem;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICLunchOrderActivity extends AppCompatActivity {

    private MultilineAdapter<MultilineItem> adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private ICService.ICanteenBinder binder = null;
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MultilineItem item = adapter.getItem(position);
            final MonthLunchDay lunch = item instanceof MonthLunchDay ? (MonthLunchDay) item : null;
            if (lunch == null) return;

            ScrollView mainScrollView = new ScrollView(ICLunchOrderActivity.this);

            final RadioGroup radioGroup = new RadioGroup(ICLunchOrderActivity.this);
            radioGroup.setOrientation(LinearLayout.VERTICAL);
            mainScrollView.addView(radioGroup);

            RadioButton radioButtonNoLunch = new RadioButton(ICLunchOrderActivity.this);
            radioButtonNoLunch.setTag(null);
            radioButtonNoLunch.setText(R.string.radio_button_text_no_lunch);
            radioButtonNoLunch.setId(R.id.text_view_title);
            radioGroup.addView(radioButtonNoLunch);
            //radioButtonNoLunch.setChecked(true);

            int toCheck = radioButtonNoLunch.getId();

            List<Button> buttons = new ArrayList<>();

            MonthLunch[] lunches = lunch.getLunches();
            for (int i = 0, lunchesLength = lunches.length; i < lunchesLength; i++) {
                final MonthLunch monthLunch = lunches[i];

                RadioButton radioButtonLunch = new RadioButton(ICLunchOrderActivity.this);
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

                MonthLunch.BurzaState burzaState = monthLunch.getBurzaState();
                if (burzaState != null) {
                    Button button = new Button(ICLunchOrderActivity.this);
                    button.setText(burzaState.toString());
                    button.setTag(monthLunch);

                    buttons.add(button);

                    radioGroup.addView(button);
                }
            }
            if (radioButtonNoLunch.getTag() == null
                    && radioButtonNoLunch.getId() != toCheck)
                radioButtonNoLunch.setEnabled(false);
            radioGroup.check(toCheck);

            final AlertDialog dialog = new AlertDialog.Builder(ICLunchOrderActivity.this)
                    .setTitle(MonthLunchDay.DATE_PARSE_FORMAT.format(lunch.getDate()))
                            //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon iC
                    .setView(mainScrollView)
                    .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MonthLunch monthLunchToOrder = (MonthLunch)
                                    radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()).getTag();
                            if (binder == null || (monthLunchToOrder != null && monthLunchToOrder.getOrderUrlAdd() == null))
                                Toast.makeText(ICLunchOrderActivity.this, R.string.toast_text_can_not_order_lunch, Toast.LENGTH_LONG).show();
                            else {
                                if (monthLunchToOrder != null)
                                    binder.orderMonthLunch(monthLunchToOrder);
                                update();
                            }
                        }
                    })
                    .setNegativeButton(R.string.but_cancel, null)
                    .setCancelable(true)
                    .show();

            for (final Button button : buttons) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.hide();
                        MonthLunch monthLunch = (MonthLunch) button.getTag();
                        if (binder == null || monthLunch.getOrderUrlAdd() == null)
                            Toast.makeText(ICLunchOrderActivity.this, R.string.toast_text_can_not_order_lunch, Toast.LENGTH_LONG).show();
                        else {
                            binder.toBurzaMonthLunch(monthLunch);
                            update();
                        }
                    }
                });
            }
        }
    };
    private ServiceManager.BinderConnection<ICService.ICanteenBinder> binderConnection
            = new ServiceManager.BinderConnection<ICService.ICanteenBinder>() {
        @Override
        public void onBinderConnected(ICService.ICanteenBinder iCanteenBinder) {
            Log.d("LunchOrderActivity", "onBinderConnected");
            binder = iCanteenBinder;
            refreshThread.startWorker(new Runnable() {
                @Override
                public void run() {
                    binder.setOnMonthChangeListener(new Runnable() {
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
            Log.d("LunchOrderActivity", "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("LunchOrderActivity", "onBinderDisconnected", e);
            }
            binder.setOnMonthChangeListener(null);
            binder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ICSplashActivity.serviceManager == null
                || !ICSplashActivity.serviceManager.isConnected()) {
            startActivity(new Intent(this, ICSplashActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_list);

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        ListView listView = (ListView) findViewById(R.id.listView);
        adapter = new MultilineAdapter<>(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);

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
            ICBurzaActivity.startBurzaChecker(this, binder);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
