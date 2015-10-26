package cz.anty.purkynkamanager.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.RecyclerInflater;
import cz.anty.purkynkamanager.utils.other.list.recyclerView.specialAdapter.SpecialModuleManager;

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
        if (moduleManager == null)
            moduleManager = SpecialModuleManager.getInstance(this, false);
        moduleManager.bindRecyclerManager(RecyclerInflater
                .inflateToActivity(this).inflate());

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
