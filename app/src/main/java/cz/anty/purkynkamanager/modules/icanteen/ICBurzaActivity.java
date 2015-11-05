package cz.anty.purkynkamanager.modules.icanteen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.list.items.MultilineItem;
import cz.anty.purkynkamanager.utils.other.list.items.TextMultilineItem;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.service.ServiceManager;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

public class ICBurzaActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ICBurzaActivity";

    private MultilineRecyclerAdapter<MultilineItem> adapter;
    private RecyclerInflater.RecyclerManager recyclerManager;
    private OnceRunThread refreshThread;
    private ICService.ICBinder binder = null;
    private ServiceManager.BinderConnection<ICService.ICBinder> binderConnection
            = new ServiceManager.BinderConnection<ICService.ICBinder>() {
        @Override
        public void onBinderConnected(ICService.ICBinder ICBinder) {
            Log.d(LOG_TAG, "onBinderConnected");
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
            Log.d(LOG_TAG, "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, "onBinderDisconnected", e);
            }
            binder.setOnBurzaChangeListener(null);
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
        recyclerManager = RecyclerInflater.inflateToActivity(this).inflate().setAdapter(adapter)
                .setItemTouchListener(new RecyclerItemClickListener.SimpleClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        MultilineItem item = adapter.getItem(position);

                        final BurzaLunch lunch = item instanceof BurzaLunch
                                ? (BurzaLunch) item : null;
                        if (lunch == null) return;

                        new AlertDialog.Builder(ICBurzaActivity.this, R.style.AppTheme_Dialog_IC)
                                .setTitle(lunch.getName())
                                .setIcon(R.mipmap.ic_launcher_ic)
                                .setMessage(lunch.getLunchNumber()
                                        + "\n" + BurzaLunch.DATE_FORMAT.format(lunch.getDate())
                                        + "\n" + lunch.getName())
                                .setPositiveButton(R.string.but_order, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (binder == null)
                                            Toast.makeText(ICBurzaActivity.this, R.string.toast_text_can_not_order_lunch, Toast.LENGTH_LONG).show();
                                        else binder.orderLunch(lunch);
                                    }
                                })
                                .setNegativeButton(R.string.but_cancel, null)
                                .setCancelable(true)
                                .show();
                    }
                }).setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        update();
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
                recyclerManager.setRefreshing(true);
                MultilineItem[] data;
                try {
                    data = binder.getBurza();
                } catch (NullPointerException | InterruptedException e) {
                    data = new MultilineItem[]{new TextMultilineItem(getText(R.string.exception_title_sas_manager_binder_null),
                            getText(R.string.exception_message_sas_manager_binder_null)).setTag("EXCEPTION")};
                }

                final MultilineItem[] finalData = data;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clearItems();
                        adapter.addAllItems(finalData);
                    }
                });
                recyclerManager.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_refresh, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
