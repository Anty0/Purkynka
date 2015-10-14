package cz.anty.purkynkamanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import cz.anty.attendancemanager.TrackingSpecialModule;
import cz.anty.icanteenmanager.ICSpecialModule;
import cz.anty.purkynkamanager.update.UpdateSpecialModule;
import cz.anty.sasmanager.SASSpecialModule;
import cz.anty.timetablemanager.TimetableSpecialModule;
import cz.anty.utils.Log;
import cz.anty.utils.list.recyclerView.specialAdapter.SpecialModuleManager;
import cz.anty.wifiautologin.WifiSpecialModule;

/**
 * Created by anty on 14.10.15.
 *
 * @author anty
 */
public class RemovedItemsActivity extends AppCompatActivity {

    private static SpecialModuleManager moduleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        if (moduleManager == null)
            moduleManager = new SpecialModuleManager(recyclerView, false,
                    new UpdateSpecialModule(this), new ShareSpecialModule(this), new TrackingSpecialModule(this),
                    new SASSpecialModule(this), new ICSpecialModule(this), new TimetableSpecialModule(this),
                    new WifiSpecialModule(this));
        else moduleManager.reInit(recyclerView);

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
