package cz.anty.purkynkamanager.utils.other.wifi;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import cz.anty.purkynkamanager.R;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;

/**
 * Created by anty on 11.6.15.
 *
 * @author anty
 */
public class WifiLogin {

    public static final String WIFI_NAME = "ISSWF";
    private static final String LOG_TAG = "WifiLogin";
    private static final String TEST_URL = "http://www.sspbrno.cz/";
    //private static final String LOGIN_URL = "http://wifi.sspbrno.cz/login.html";
    //private static final String LOGOUT_URL = "http://wifi.sspbrno.cz/logout.html";
    private static final String LOGIN_FIELD = "username";
    private static final String PASS_FIELD = "password";
    //private static final String SUBMIT = "Submit";
    //private static final String SUBMIT_VALUE = "Submit";

    private static void showToast(final Context context, Handler threadHandler,
                                  final CharSequence text, boolean showToasts) {
        if (showToasts) threadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean tryLogin(Context context, String username, String password,
                                   Handler threadHandler, String wifiName,
                                   boolean showToasts) throws IOException {
        boolean showFailNotification = false;
        try {
            String loginUrl = getLoginUrl();
            if (loginUrl == null || !loginUrl
                    .contains("wifi.sspbrno.cz/login.html"))
                return false;

            showToast(context, threadHandler, Utils.getFormattedText(context, R.string
                    .toast_text_logging_to_wifi, wifiName), showToasts);
            showFailNotification = true;
            sendLoginRequest(loginUrl, username, password);
            showToast(context, threadHandler, Utils.getFormattedText(context, R.string
                    .toast_text_logged_in_wifi, wifiName), showToasts);
            return true;
        } catch (Throwable t) {
            showToast(context, threadHandler, Utils.getFormattedText(context, R.string
                            .toast_text_failed_logging_to_wifi, wifiName),
                    showToasts && showFailNotification);
            throw t;
        }
    }

    private static String getLoginUrl() throws IOException {
        IOException exception = null;
        for (int i = 0; i < Constants.MAX_TRY; i++) {
            try {
                String body = Jsoup.connect(TEST_URL)
                        .validateTLSCertificates(false)
                        .execute().body();
                Log.d(LOG_TAG, "getLoginUrl body: " + body);
                final String toFind = "<META http-equiv=\"refresh\" content=\"1; URL=";
                String url;
                int index = body.indexOf(toFind);
                if (index != -1) {
                    index += toFind.length();
                    url = body.substring(index, body.indexOf("\">", index));
                    Log.d(LOG_TAG, "getLoginUrl loginUrlBefore: " + url);
                    index = url.indexOf("?");
                    if (index != -1)
                        url = url.substring(0, index);
                    Log.d(LOG_TAG, "getLoginUrl loginUrl: " + url);
                    return url;
                }
                return null;
            } catch (IOException e) {
                if (e.getCause() == null && exception != null)
                    e.initCause(exception);
                exception = e;
                Log.d(LOG_TAG, "getLoginUrl", exception);
            }
        }
        throw exception;
    }

    private static void sendLoginRequest(String url, String username, String password) throws IOException {
        IOException exception = null;
        for (int i = 0; i < Constants.MAX_TRY; i++) {
            try {
                Jsoup.connect(url)
                        .data("buttonClicked", "4", "err_flag", "0", "err_msg", "", "info_flag", "0", "info_msg", "",
                                "redirect_url", "", LOGIN_FIELD, username, PASS_FIELD, password)
                        .method(Connection.Method.POST)
                        .validateTLSCertificates(false)
                        .execute();
                return;
            } catch (IOException e) {
                if (e.getCause() == null && exception != null)
                    e.initCause(exception);
                exception = e;
                Log.d(LOG_TAG, "sendLoginRequest", exception);
            }
        }
        throw exception;
    }

}
