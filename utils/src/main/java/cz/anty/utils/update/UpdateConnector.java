package cz.anty.utils.update;

import org.jsoup.Jsoup;

import java.io.IOException;

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

    public static Integer latestVersionCode() throws IOException {
        return Integer.parseInt(Jsoup.connect(DEFAULT_URL + LATEST_VERSION_CODE_URL_ADD)
                .execute().body().replaceAll("\n", ""));
    }

    public static String latestVersionName() throws IOException {
        return Jsoup.connect(DEFAULT_URL + LATEST_VERSION_NAME_URL_ADD)
                .execute().body().replaceAll("\n", "");
    }
}
