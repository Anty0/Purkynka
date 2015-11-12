package cz.anty.purkynkamanager;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;

import cz.anty.purkynkamanager.utils.other.AppDataManager;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.service.ServiceManager;
import cz.anty.purkynkamanager.utils.other.thread.OnceRunThread;
import cz.anty.purkynkamanager.utils.other.update.UpdateConnector;

/**
 * Created by anty on 7.9.15.
 *
 * @author anty
 */
@ReportsCrashes(
        buildConfigClass = BuildConfig.class,
        formUri = UpdateConnector.URL_REPORT,
        applicationLogFileLines = 500,
        sendReportsInDevMode = false
)
public class ApplicationBase extends Application {

    public static final Gson GSON = new Gson();
    public static final OnceRunThread WORKER = new OnceRunThread();

    @Override
    public void onCreate() {
        super.onCreate();

        final Thread.UncaughtExceptionHandler defaultHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.d("UExHandler", "Enabling Feedback Module...");
                getSharedPreferences(Constants.SETTINGS_NAME_MAIN,
                        MODE_PRIVATE).edit().putInt(Constants
                                .SETTING_NAME_LATEST_EXCEPTION_CODE,
                        BuildConfig.VERSION_CODE).apply();
                defaultHandler.uncaughtException(thread, ex);
            }
        });

        ACRAConfiguration configuration = new ACRAConfiguration(
                getClass().getAnnotation(ReportsCrashes.class));
        configuration.setCustomReportContent(ReportField.values());
        ACRA.init(this, configuration);

        AnalyticsTrackers.initialize(this);
        Tracker tracker = AnalyticsTrackers.getInstance()
                .get(AnalyticsTrackers.Target.APP);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);

        WORKER.setPowerManager(this);
        AppDataManager.init(this);
        Utils.restoreLocale(this);
        Log.d("STARTED", "DEBUG-MODE: "
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
