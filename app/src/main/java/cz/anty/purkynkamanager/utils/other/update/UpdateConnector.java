package cz.anty.purkynkamanager.utils.other.update;

import android.content.Context;
import android.os.Environment;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.thread.ProgressReporter;

/**
 * Created by anty on 25.6.15.
 *
 * @author anty
 */
public class UpdateConnector {

    private static final String LOG_TAG = "UpdateConnector";

    //private static final String DEFAULT_URL = "http://anty.crush-team.cz/purkynkamanager/";
    private static final String DEFAULT_URL = "http://anty.codetopic.eu/purkynka/";

    public static final String URL_REPORT = DEFAULT_URL + "report.php";
    private static final String URL_FEEDBACK = DEFAULT_URL + "feedback.php";

    private static final String URL_LATEST_VERSION_CODE = DEFAULT_URL + "latestVersionCode";
    private static final String URL_LATEST_VERSION_NAME = DEFAULT_URL + "latestVersionName";
    private static final String URL_LATEST_APK = DEFAULT_URL + "latest.apk";

    private static final String URL_LATEST_TERMS = DEFAULT_URL + "latestTerms";
    private static final String URL_LATEST_TERMS_VERSION_CODE = DEFAULT_URL + "latestTermsVersionCode";

    private static final String URL_LATEST_CHANGE_LOG = DEFAULT_URL + "latestChangeLog";

    public static Integer getLatestVersionCode() throws IOException, NumberFormatException {
        Integer toReturn = Integer.parseInt(Jsoup.connect(URL_LATEST_VERSION_CODE)
                .followRedirects(false).execute().body().trim());
        Log.d(LOG_TAG, "getLatestVersionCode versionCode: " + toReturn);
        return toReturn;
    }

    public static String getLatestVersionName() throws IOException {
        String toReturn = Jsoup.connect(URL_LATEST_VERSION_NAME)
                .followRedirects(false).execute().body().replace("\n", "");
        Log.d(LOG_TAG, "getLatestVersionName versionName: " + toReturn);
        return toReturn;
    }

    public static Integer getLatestTermsVersionCode() throws IOException, NumberFormatException {
        Integer toReturn = Integer.parseInt(Jsoup.connect(URL_LATEST_TERMS_VERSION_CODE)
                .followRedirects(false).execute().body().trim());
        Log.d(LOG_TAG, "getLatestTermsVersionCode versionCode: " + toReturn);
        return toReturn;
    }

    public static String getLatestTerms(String languageShortcut) throws IOException {
        String toReturn = Jsoup.connect(URL_LATEST_TERMS
                + languageShortcut.toUpperCase(Locale.ENGLISH))
                .followRedirects(false).execute().body().trim();
        Log.d(LOG_TAG, "getLatestTerms terms: " + toReturn);
        return toReturn;
    }

    public static String getLatestChangeLog() throws IOException {
        String toReturn = Jsoup.connect(URL_LATEST_CHANGE_LOG)
                .followRedirects(false).execute().body().trim();
        Log.d(LOG_TAG, "getLatestChangeLog changeLog: " + toReturn);
        return toReturn;
    }

    public static void sendFeedback(String title, String text) throws IOException {
        Log.d(LOG_TAG, "sendFeedback title: " + title + " text: " + text);
        Jsoup.connect(URL_FEEDBACK)
                .followRedirects(false)
                .method(Connection.Method.POST)
                .data("APP_VERSION_NAME", BuildConfig.VERSION_NAME,
                        "APP_VERSION_CODE", String.valueOf(BuildConfig
                                .VERSION_CODE), "NAME", title, "TEXT", text)
                .execute();
    }

    public static String downloadUpdate(Context context, ProgressReporter reporter, String filename) throws IOException, InterruptedException {
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            URL url = new URL(URL_LATEST_APK);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setInstanceFollowRedirects(false);
            c.setDoOutput(true);
            c.connect();

            String PATH = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    + File.separator;
            File file = new File(PATH);
            file.mkdirs();
            File outputFile = new File(file, filename);
            if (outputFile.exists()) outputFile.delete();
            fos = new FileOutputStream(outputFile);

            is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len;
            int completedLen = 0;
            int max = c.getContentLength();
            reporter.setMaxProgress(max != -1 ? max : Integer.MAX_VALUE);
            //Log.d("UpdateConnector", "TotalLen: " + is.available());
            Thread currentThread = Thread.currentThread();
            while ((len = is.read(buffer)) != -1
                    && !currentThread.isInterrupted()) {
                fos.write(buffer, 0, len);
                completedLen += len;
                reporter.reportProgress(completedLen);
                //Log.d("UpdateConnector", "CompletedLen: " + completedLen);
            }

            if (Thread.interrupted())
                throw new InterruptedException();

            return PATH + filename;
        } finally {
            if (fos != null) fos.close();
            if (is != null) is.close();
        }

        /*File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + filename);
        if (file.exists()) file.delete();

        DownloadManager.Request request = new DownloadManager
                .Request(Uri.parse(DEFAULT_URL + LATEST_APK_URL_ADD));
        request.setTitle(context.getString(R.string.downloading_update));
        request.setDescription(context.getString(R.string.please_wait));

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= 11) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        } else {
            //noinspection deprecation
            request.setShowRunningNotification(false);
        }
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //manager.query(new DownloadManager.Query().setFilterById())
        return manager.enqueue(request);*/
    }
}
