package cz.anty.utils.update;

import android.content.Context;
import android.os.Environment;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import cz.anty.utils.Log;
import cz.anty.utils.thread.ProgressReporter;

/**
 * Created by anty on 25.6.15.
 *
 * @author anty
 */
public class UpdateConnector {

    private static final String DEFAULT_URL = "http://anty.crush-team.cz/purkynkamanager/";

    private static final String LATEST_VERSION_CODE_URL_ADD = "latestVersionCode";
    private static final String LATEST_VERSION_NAME_URL_ADD = "latestVersionName";
    private static final String LATEST_APK_URL_ADD = "latest.apk";

    private static final String LATEST_TERMS_URL_ADD = "latestTerms";
    private static final String LATEST_TERMS_VERSION_CODE_URL_ADD = "latestTermsVersionCode";

    private static final String LATEST_CHANGE_LOG_URL_ADD = "latestChangeLog";

    public static Integer getLatestVersionCode() throws IOException, NumberFormatException {
        Integer toReturn = Integer.parseInt(Jsoup.connect(DEFAULT_URL + LATEST_VERSION_CODE_URL_ADD)
                .execute().body().trim());
        Log.d(UpdateConnector.class.getSimpleName(), "getLatestVersionCode versionCode: " + toReturn);
        return toReturn;
    }

    public static String getLatestVersionName() throws IOException {
        String toReturn = Jsoup.connect(DEFAULT_URL + LATEST_VERSION_NAME_URL_ADD)
                .execute().body().replace("\n", "");
        Log.d(UpdateConnector.class.getSimpleName(), "getLatestVersionName versionName: " + toReturn);
        return toReturn;
    }

    public static Integer getLatestTermsVersionCode() throws IOException, NumberFormatException {
        Integer toReturn = Integer.parseInt(Jsoup.connect(DEFAULT_URL + LATEST_TERMS_VERSION_CODE_URL_ADD)
                .execute().body().trim());
        Log.d(UpdateConnector.class.getSimpleName(), "getLatestTermsVersionCode versionCode: " + toReturn);
        return toReturn;
    }

    public static String getLatestTerms(String languageShortcut) throws IOException {
        String toReturn = Jsoup.connect(DEFAULT_URL + LATEST_TERMS_URL_ADD
                + languageShortcut.toUpperCase(Locale.ENGLISH))
                .execute().body().trim();
        Log.d(UpdateConnector.class.getSimpleName(), "getLatestTerms terms: " + toReturn);
        return toReturn;
    }

    public static String getLatestChangeLog(String languageShortcut) throws IOException {
        String toReturn = Jsoup.connect(DEFAULT_URL + LATEST_CHANGE_LOG_URL_ADD
                + languageShortcut.toUpperCase(Locale.ENGLISH))
                .execute().body().trim();
        Log.d(UpdateConnector.class.getSimpleName(), "getLatestChangeLog changeLog: " + toReturn);
        return toReturn;
    }

    public static String downloadUpdate(Context context, ProgressReporter reporter, String filename) throws IOException, InterruptedException {
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            URL url = new URL(DEFAULT_URL + LATEST_APK_URL_ADD);
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
