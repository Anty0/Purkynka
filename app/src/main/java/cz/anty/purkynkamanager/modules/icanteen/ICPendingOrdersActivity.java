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

import java.util.List;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.LunchOrderRequest;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.MultilineRecyclerAdapter;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerItemClickListener;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.base.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;

/**
 * Created by anty on 05.11.2015.
 *
 * @author anty
 */
public class ICPendingOrdersActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ICPendingOrdersActivity";

    private MultilineRecyclerAdapter<LunchOrderRequest> mAdapter;
    private RecyclerInflater.RecyclerManager mRecyclerManager;
    private OnceRunThread refreshThread;

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

        mAdapter = new MultilineRecyclerAdapter<>();
        mRecyclerManager = RecyclerInflater.inflateToActivity(this).inflate()
                .setAdapter(mAdapter).setItemTouchListener(
                        new RecyclerItemClickListener.SimpleClickListener() {
                            @Override
                            public void onClick(View view, int position) {
                                final LunchOrderRequest item = mAdapter.getItem(position);
                                new AlertDialog.Builder(ICPendingOrdersActivity.this, R.style.AppTheme_Dialog_IC)
                                        .setTitle(item.getTitle(ICPendingOrdersActivity.this, -1))
                                        .setMessage(R.string.dialog_message_icanteen_cancel_order)
                                        .setPositiveButton(R.string.but_yes,
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        ICService.ICBinder binder = ICSplashActivity.serviceManager.getBinder();
                                                        if (binder != null)
                                                            binder.removeOrderRequest(item);
                                                        update();
                                                    }
                                                })
                                        .setNegativeButton(R.string.but_no, null)
                                        .setIcon(R.mipmap.ic_launcher_ic_no_border)
                                        .setCancelable(true)
                                        .show();
                            }
                        })
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        processOrders();
                    }
                });

        update();
    }

    private void update() {
        refreshThread.startWorker(new Runnable() {
            @Override
            public void run() {
                try {
                    mRecyclerManager.setRefreshing(true);
                    ICService.ICBinder binder = ICSplashActivity.serviceManager.getBinder();
                    final LunchOrderRequest[] requests;
                    if (binder != null) {
                        binder.waitToWorkerStop();
                        List<LunchOrderRequest> requestList = binder.getOrderRequests();
                        requests = requestList.toArray(new LunchOrderRequest[requestList.size()]);
                    } else requests = new LunchOrderRequest[0];
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clearItems();
                            mAdapter.addAllItems(requests);
                        }
                    });
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "update", e);
                } finally {
                    mRecyclerManager.setRefreshing(false);
                }
            }
        });
    }

    private void processOrders() {
        mRecyclerManager.setRefreshing(true);
        ICService.ICBinder binder = ICSplashActivity.serviceManager.getBinder();
        if (binder != null) {
            binder.processOrders();
            update();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_retry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_retry) {
            processOrders();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}