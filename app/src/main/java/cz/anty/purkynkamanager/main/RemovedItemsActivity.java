package cz.anty.purkynkamanager.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.modules.ICSpecialModule;
import cz.anty.purkynkamanager.modules.SASSpecialModule;
import cz.anty.purkynkamanager.modules.ShareSpecialModule;
import cz.anty.purkynkamanager.modules.TimetableSpecialModule;
import cz.anty.purkynkamanager.modules.TrackingSpecialModule;
import cz.anty.purkynkamanager.modules.UpdateSpecialModule;
import cz.anty.purkynkamanager.modules.WifiSpecialModule;
import cz.anty.purkynkamanager.utils.Log;
import cz.anty.purkynkamanager.utils.list.recyclerView.specialAdapter.SpecialModuleManager;

/**
 * Created by anty on 14.10.15.
 *
 * @author anty
 */
public class RemovedItemsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "RemovedItemsActivity";

    private static SpecialModuleManager moduleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        View emptyView = findViewById(R.id.empty_view);
        if (moduleManager == null)
            moduleManager = new SpecialModuleManager(recyclerView, emptyView, false,
                    new UpdateSpecialModule(this), new ShareSpecialModule(this), new TrackingSpecialModule(this),
                    new SASSpecialModule(this), new ICSpecialModule(this), new TimetableSpecialModule(this),
                    new WifiSpecialModule(this));
        else moduleManager.reInit(recyclerView, emptyView);

        if (!moduleManager.isInitialized())
            moduleManager.init();
    }

    @Override
    protected void onResume() {
        if (moduleManager.isInitialized())
            moduleManager.update();
        super.onResume();
    }

    @Override
    protected void onPause() {
        moduleManager.saveState();
        super.onPause();
    }
}
