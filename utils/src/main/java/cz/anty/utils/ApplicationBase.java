package cz.anty.utils;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import cz.anty.utils.settings.AboutActivity;
import cz.anty.utils.thread.OnceRunThread;

/**
 * Created by anty on 7.9.15.
 *
 * @author anty
 */
@ReportsCrashes(
        formUri = "http://anty.crush-team.cz/purkynkamanager/report.php",
        disableSSLCertValidation = true
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

        AppDataManager.init(this);
        AboutActivity.restoreLocale(this);
        Log.d("START", "DEBUG-MODE: " + AppDataManager.isDebugMode());
    }
}
