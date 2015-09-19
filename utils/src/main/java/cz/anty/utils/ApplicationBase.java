package cz.anty.utils;

import android.app.Application;
import android.util.Log;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import cz.anty.utils.settings.AboutActivity;

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

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        AboutActivity.restoreLocale(this);
        AppDataManager.init(this);
        Log.d("START", "DEBUG-MODE: " + AppDataManager.isDebugMode());
    }
}
