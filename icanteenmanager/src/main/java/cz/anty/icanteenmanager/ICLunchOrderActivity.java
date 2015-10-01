package cz.anty.icanteenmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Log;
import cz.anty.utils.icanteen.lunch.month.MonthLunch;
import cz.anty.utils.icanteen.lunch.month.MonthLunchDay;
import cz.anty.utils.list.listView.MultilineItem;
import cz.anty.utils.list.listView.TextMultilineItem;
import cz.anty.utils.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerAdapter;
import cz.anty.utils.list.recyclerView.RecyclerItemClickListener;
import cz.anty.utils.service.ServiceManager;
import cz.anty.utils.thread.OnceRunThreadWithSpinner;

public class ICLunchOrderActivity extends AppCompatActivity {

    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private OnceRunThreadWithSpinner refreshThread;
    private ICService.ICBinder binder = null;

    private final RecyclerItemClickListener.ClickListener onItemClickListener =
            new RecyclerItemClickListener.ClickListener() {
                @Override
                public void onClick(View view, int position) {
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

                @Override
                public void onLongClick(View view, int position) {

                }
            };

    private ServiceManager.BinderConnection<ICService.ICBinder> binderConnection
            = new ServiceManager.BinderConnection<ICService.ICBinder>() {
        @Override
        public void onBinderConnected(ICService.ICBinder ICBinder) {
            Log.d("LunchOrderActivity", "onBinderConnected");
            binder = ICBinder;
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

        if (refreshThread == null)
            refreshThread = new OnceRunThreadWithSpinner(this);

        adapter = new MultilineRecyclerAdapter<>();
        RecyclerAdapter.inflateToActivity(this, null, adapter, onItemClickListener);

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
                    data = binder.getMonth();
                    if (data == null) throw new NullPointerException();
                } catch (NullPointerException | InterruptedException e) {
                    data = new MultilineItem[]{new TextMultilineItem(getString(R.string.exception_title_sas_manager_binder_null),
                            getString(R.string.exception_message_sas_manager_binder_null))};
                }

                final MultilineItem[] finalData = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(finalData);
                    }
                });

                AppDataManager.setICNewMonthLunches(false);
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
