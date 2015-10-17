package cz.anty.purkynkamanager.utils;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import cz.anty.purkynkamanager.AnalyticsTrackers;
import cz.anty.purkynkamanager.utils.service.ServiceManager;
import cz.anty.purkynkamanager.utils.thread.OnceRunThread;
import cz.anty.purkynkamanager.utils.update.UpdateConnector;

/**
 * Created by anty on 7.9.15.
 *
 * @author anty
 */
@ReportsCrashes(
        formUri = UpdateConnector.DEFAULT_URL + "report.php"
)
public class ApplicationBase extends Application {

    public static final OnceRunThread WORKER = new OnceRunThread();

    @Override
    public void onCreate() {
        super.onCreate();
        WORKER.setPowerManager(this);
        ACRA.init(this);

        AnalyticsTrackers.initialize(this);
        Tracker tracker = AnalyticsTrackers.getInstance()
                .get(AnalyticsTrackers.Target.APP);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);

        AppDataManager.init(this);
        Utils.restoreLocale(this);
        Log.d("START", "DEBUG-MODE: "
                + AppDataManager.isDebugMode());
    }

    @Override
    public void onLowMemory() {
        ServiceManager.disconnectAll();
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        ServiceManager.disconnectAll();
        super.onTerminate();
    }
}
