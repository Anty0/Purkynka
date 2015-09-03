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

    public static Integer getLatestVersionCode() throws IOException, NumberFormatException {
        return Integer.parseInt(Jsoup.connect(DEFAULT_URL + LATEST_VERSION_CODE_URL_ADD)
                .execute().body()/*.replace("\n", "")*/.trim());
    }

    public static String getLatestVersionName() throws IOException {
        return Jsoup.connect(DEFAULT_URL + LATEST_VERSION_NAME_URL_ADD)
                .execute().body()/*.replace("\n", "")*/.trim();
    }

    public static String getLatestTerms(String languageShortcut) throws IOException {
        /*if (terms.charAt(terms.length() - 1) == '\n') {
            terms = terms.substring(0, terms.length() - 1);
            /*char[] chars = terms.toCharArray();
            chars[chars.length - 1] = ' ';
            terms = new String(chars);/
        }*/
        return Jsoup.connect(DEFAULT_URL + LATEST_TERMS_URL_ADD
                + languageShortcut.toUpperCase(Locale.ENGLISH)).execute().body().trim();
    }

    public static String downloadUpdate(Context context, ProgressReporter reporter, String filename) throws IOException {
        FileOutputStream fos = null;
        InputStream is = null;
        try {
            URL url = new URL(DEFAULT_URL + LATEST_APK_URL_ADD);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
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
            reporter.setMaxProgress(is.available());
            //Log.d("UpdateConnector", "TotalLen: " + is.available());
            reporter.startShowingProgress();
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                completedLen += len;
                reporter.reportProgress(completedLen);
                //Log.d("UpdateConnector", "CompletedLen: " + completedLen);
            }
            reporter.stopShowingProgress();

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
