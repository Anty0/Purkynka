package cz.anty.icanteenmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import cz.anty.utils.Log;
import cz.anty.utils.icanteen.lunch.burza.BurzaLunch;
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
            Log.d(ICBurzaActivity.this.getClass().getSimpleName(), "onBinderConnected");
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
            Log.d(ICBurzaActivity.this.getClass().getSimpleName(), "onBinderDisconnected");
            try {
                refreshThread.waitToWorkerStop();
            } catch (InterruptedException e) {
                Log.d(ICBurzaActivity.this.getClass().getSimpleName(), "onBinderDisconnected", e);
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
            refreshThread = new OnceRunThreadWithSpinner(this);

        adapter = new MultilineRecyclerAdapter<>();
        RecyclerAdapter.inflateToActivity(this, null, adapter,
                new RecyclerItemClickListener.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        MultilineItem item = adapter.getItem(position);

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
                                        else binder.orderLunch(lunch);
                                    }
                                })
                                .setNegativeButton(R.string.but_cancel, null)
                                .setCancelable(true)
                                .show();
                    }

                    @Override
                    public void onLongClick(View view, int position) {

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
            }
        }, getText(R.string.wait_text_loading));
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

        return super.onOptionsItemSelected(item);
    }
}
