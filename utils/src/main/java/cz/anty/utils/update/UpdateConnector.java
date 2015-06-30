package cz.anty.utils.update;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Locale;

import cz.anty.utils.R;

/**
 * Created by anty on 25.6.15.
 *
 * @author anty
 */
public class UpdateConnector {

    private static final String DEFAULT_URL = "http://student.sspbrno.cz/~kuchynka.jiri/";
    private static final String LATEST_VERSION_CODE_URL_ADD = "latestVersionCode";
    private static final String LATEST_VERSION_NAME_URL_ADD = "latestVersionName";
    private static final String LATEST_APK_URL_ADD = "latest.apk";
    private static final String LATEST_TERMS_URL_ADD = "latestTerms";

    public static Integer getLatestVersionCode() throws IOException {
        return Integer.parseInt(Jsoup.connect(DEFAULT_URL + LATEST_VERSION_CODE_URL_ADD)
                .execute().body().replaceAll("\n", ""));
    }

    public static String getLatestVersionName() throws IOException {
        return Jsoup.connect(DEFAULT_URL + LATEST_VERSION_NAME_URL_ADD)
                .execute().body().replaceAll("\n", "");
    }

    public static String getLatestTerms(String languageShortcut) throws IOException {
        String terms = Jsoup.connect(DEFAULT_URL + LATEST_TERMS_URL_ADD
                + languageShortcut.toUpperCase(Locale.ENGLISH)).execute().body();
        if (terms.charAt(terms.length() - 1) == '\n') {
            terms = terms.substring(0, terms.length() - 1);
            /*char[] chars = terms.toCharArray();
            chars[chars.length - 1] = ' ';
            terms = new String(chars);*/
        }
        return terms;
    }

    public static long downloadUpdate(Context context, String filename) {
        DownloadManager.Request request = new DownloadManager
                .Request(Uri.parse(DEFAULT_URL + LATEST_APK_URL_ADD));
        request.setTitle(context.getString(R.string.downloading_update));
        request.setDescription(context.getString(R.string.please_wait));

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        } else {
            request.setShowRunningNotification(false);
        }
        request.setVisibleInDownloadsUi(false);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //manager.query(new DownloadManager.Query().setFilterById())
        return manager.enqueue(request);
    }
}
