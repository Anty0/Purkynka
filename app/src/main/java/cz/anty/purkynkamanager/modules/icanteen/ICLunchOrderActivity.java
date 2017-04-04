package cz.anty.purkynkamanager.modules.icanteen;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.items.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.service.ServiceManager;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

public class ICLunchOrderActivity extends AppCompatActivity {

    private boolean mShowOldLunches = false;
    private boolean mShowNewLunches = true;
    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private RecyclerInflater.RecyclerManager recyclerManager;
    private RecyclerView recyclerView;
    private OnceRunThread refreshThread;
    private ICService.ICBinder binder = null;

    private final RecyclerItemClickListener.ClickListener onItemClickListener =
            new RecyclerItemClickListener.SimpleClickListener() {
                @Override
                public void onClick(View view, int position) {
                    MultilineItem item = adapter.getItem(position);
                    final MonthLunchDay lunch = item instanceof MonthLunchDay ? (MonthLunchDay) item : null;
                    if (lunch == null) return;

                    final ScrollView mainScrollView = new ScrollView(ICLunchOrderActivity.this);

                    final RadioGroup radioGroup = new RadioGroup(ICLunchOrderActivity.this);
                    radioGroup.setOrientation(LinearLayout.VERTICAL);
                    mainScrollView.addView(radioGroup);

                    RadioButton radioButtonNoLunch = new AppCompatRadioButton(ICLunchOrderActivity.this);
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

                        RadioButton radioButtonLunch = new AppCompatRadioButton(ICLunchOrderActivity.this);
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
                            case UNKNOWN:
                                radioButtonLunch.setEnabled(false);
                                break;
                        }

                        MonthLunch.BurzaState burzaState = monthLunch.getBurzaState();
                        if (burzaState != null) {
                            Button button = new AppCompatButton(ICLunchOrderActivity.this);
                            button.setText(burzaState.toCharSequence(ICLunchOrderActivity.this));
                            button.setTag(monthLunch);

                            buttons.add(button);

                            radioGroup.addView(button);
                        }
                    }
                    if (radioButtonNoLunch.getTag() == null
                            && radioButtonNoLunch.getId() != toCheck)
                        radioButtonNoLunch.setEnabled(false);
                    radioGroup.check(toCheck);

                    final CharSequence creditText = Utils.getFormattedText(ICLunchOrderActivity
                            .this, R.string.text_credit, binder == null ? "?" : binder.getCreditString());

                    final TextView creditTextView = new TextView(ICLunchOrderActivity.this);
                    creditTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout
                            .LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    creditTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                    creditTextView.setTextSize(16f);
                    Utils.setPadding(creditTextView, 0, 10, 0, 0);

                    final Spannable spannable = new SpannableString(creditText);
                    spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 8, creditText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, creditText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    creditTextView.setText(spannable, TextView.BufferType.SPANNABLE);
                    radioGroup.addView(creditTextView);

                    AlertDialog.Builder dialogBuilder = new AlertDialog
                            .Builder(ICLunchOrderActivity.this, R.style.AppTheme_Dialog_IC)
                            .setTitle(lunch.getTitle(ICLunchOrderActivity.this, MultilineItem.NO_POSITION))
                            .setIcon(R.mipmap.ic_launcher_ic_no_border)
                            .setView(mainScrollView)
                            .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MonthLunch monthLunchToOrder = (MonthLunch)
                                            radioGroup.findViewById(radioGroup
                                                    .getCheckedRadioButtonId()).getTag();
                                    if (binder == null || (monthLunchToOrder != null && monthLunchToOrder
                                            .getOrderUrlAdd() == null))
                                        Toast.makeText(ICLunchOrderActivity.this, R.string.toast_text_can_not_order_lunch,
                                                Toast.LENGTH_LONG).show();
                                    else {
                                        if (monthLunchToOrder != null)
                                            binder.orderLunch(monthLunchToOrder);
                                        update();
                                    }
                                }
                            })
                            .setNegativeButton(R.string.but_cancel, null)
                            .setCancelable(true);
                    if (lunch.isDisabled()) {
                        dialogBuilder.setNeutralButton(R.string.but_remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (binder != null) {
                                    binder.removeDisabledLunch(lunch);
                                    return;
                                }
                                Toast.makeText(ICLunchOrderActivity.this, R.string.toast_text_can_not_remove_lunch,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    final AlertDialog dialog = dialogBuilder.create();

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

                    dialog.show();
                }
            };

    private ServiceManager.BinderConnection<ICService.ICBinder> binderConnection
            = new ServiceManager.BinderConnection<ICService.ICBinder>() {
        @Override
        public void onBinderConnected(ICService.ICBinder ICBinder) {
            Log.d("LunchOrderActivity", "onBinderConnected");
            binder = ICBinder;
            binder.setOnMonthChangeListener(new Runnable() {
                @Override
                public void run() {
                    update();
                }
            });
            update();
        }

        @Override
        public void onBinderDisconnected() {
            Log.d("LunchOrderActivity", "onBinderDisconnected");
            /*try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d("LunchOrderActivity", "onBinderDisconnected", e);
            }*/
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
            refreshThread = new OnceRunThread(this);

        adapter = new MultilineRecyclerAdapter<>();
        recyclerManager = RecyclerInflater.inflateToActivity(this).inflate()
                .setAdapter(adapter).setItemTouchListener(onItemClickListener)
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (binder != null)
                            binder.refreshMonth();
                        else Toast.makeText(ICLunchOrderActivity.this,
                                R.string.toast_text_can_not_refresh_lunches,
                                Toast.LENGTH_LONG).show();
                        update();
                    }
                });
        recyclerView = recyclerManager.getRecyclerView();

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
        /*if (refreshThread.isWorkerRunning() &&
                (binder != null && !binder.isWorkerRunning())) return;*/
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                recyclerManager.setRefreshing(true);
                MultilineItem[] data;
                try {
                    if (mShowNewLunches && mShowOldLunches)
                        data = binder.getFullMonth();
                    else if (mShowNewLunches)
                        data = binder.getNewMonth();
                    else if (mShowOldLunches)
                        data = binder.getOldMonth();
                    else data = new MultilineItem[0];
                    if (data == null) throw new NullPointerException();
                } catch (NullPointerException | InterruptedException e) {
                    data = new MultilineItem[]{new TextMultilineItem(
                            getText(R.string.exception_title_sas_manager_binder_null),
                            getText(R.string.exception_message_sas_manager_binder_null))};
                }


                int position = -1;
                Calendar actualCalendar = Calendar.getInstance();
                Calendar lunchCalendar = Calendar.getInstance();
                Calendar lunchTmpCalendar = Calendar.getInstance();
                lunchTmpCalendar.setTimeInMillis(Long.MAX_VALUE);
                for (int i = 0; i < data.length; i++) {
                    MultilineItem item = data[i];
                    if (!(item instanceof MonthLunchDay)) continue;
                    lunchCalendar.setTime(((MonthLunchDay) item).getDate());
                    int yearDiff = lunchCalendar.get(Calendar.YEAR) - actualCalendar.get(Calendar.YEAR);
                    int dayDiff = lunchCalendar.get(Calendar.DAY_OF_YEAR) - actualCalendar.get(Calendar.DAY_OF_YEAR);
                        /*Log.d(getClass().getSimpleName(), "update position: " + i
                                + " yearDiff: " + yearDiff + " dayDiff: " + dayDiff);*/

                    if (yearDiff == 0 && dayDiff == 0) {
                        position = i;
                        //Log.d(getClass().getSimpleName(), "update newUsedItem: " + i);
                        break;
                    }

                        /*Log.d(getClass().getSimpleName(), "update position: " + i
                                + " time: " + time + " tmpTime: " + tmpTime);*/
                    if (lunchCalendar.getTimeInMillis() < lunchTmpCalendar.getTimeInMillis() && (yearDiff > 0 || (yearDiff >= 0 && dayDiff >= 0))) {
                        lunchTmpCalendar.setTime(lunchCalendar.getTime());
                        position = i;
                        //Log.d(getClass().getSimpleName(), "update newTmpItem: " + position);
                    }
                }


                final MultilineItem[] finalData = data;
                final int finalPosition = position;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(finalData);

                        if (finalPosition != -1)
                            recyclerView.scrollToPosition(finalPosition);
                    }
                });

                AppDataManager.setICNewMonthLunches(false);
                if (binder != null && binder.isWorkerRunning()) update();
                recyclerManager.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lunch_order, menu);
        menu.findItem(R.id.action_show_new).setChecked(mShowNewLunches);
        menu.findItem(R.id.action_show_old).setChecked(mShowOldLunches);
        menu.findItem(R.id.action_clear_history).setVisible(mShowOldLunches);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_show_old) {
            mShowOldLunches = !mShowOldLunches;
            invalidateOptionsMenu();
            update();
            return true;
        }
        if (id == R.id.action_show_new) {
            mShowNewLunches = !mShowNewLunches;
            invalidateOptionsMenu();
            update();
            return true;
        }
        if (id == R.id.action_refresh) {
            if (binder != null)
                binder.refreshMonth();
            else Toast.makeText(this,
                    R.string.toast_text_can_not_refresh_lunches,
                    Toast.LENGTH_LONG).show();
            update();
            return true;
        }
        if (id == R.id.action_clear_history) {
            if (binder != null)
                binder.removeAllDisabledLunches();
            else Toast.makeText(this,
                    R.string.toast_text_can_not_remove_lunch,
                    Toast.LENGTH_LONG).show();
            update();
            return true;
        }
        // TODO: 12.11.2015 add button show credit and add logic to show warning about no credit to order lunch

        return super.onOptionsItemSelected(item);
    }
}
