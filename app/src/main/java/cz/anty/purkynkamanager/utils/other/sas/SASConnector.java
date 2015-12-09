package cz.anty.purkynkamanager.utils.other.sas;

import android.os.Build;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

import cz.anty.purkynkamanager.BuildConfig;
import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.sas.mark.MarksManager;

/**
 * Created by anty on 7.6.15.
 *
 * @author anty
 */
class SASConnector {

    private static final String LOG_TAG = "SASConnector";
    private static final String SCHOOL_URL = "http://www.sspbrno.cz";
    private static final String DEFAULT_MAIN_URL = "http://isas.sspbrno.cz";
    private static final String LOGIN_URL_ADD = "/prihlasit.php";
    private static final String LOGIN_FIELD = "login-isas-username";
    private static final String PASS_FIELD = "login-isas-password";
    private static final String SUBMIT = "login-isas-send";
    private static final String SUBMIT_VALUE = "isas-send";
    private static final String MARKS_URL_ADD = "/prubezna-klasifikace.php";
    private static final String SEMESTER = "pololeti";
    private static final String SHORT_BY = "zobraz";
    private static final String SHORT_BY_DATE = "datum";
    private final String MAIN_URL;
    //private static final String SHORT_BY_LESSONS = "predmety";
    //private static final String SHORT_BY_SCORE = "hodnoceni";
    private final Map<String, String> loginCookies;
    private final boolean forced;

    SASConnector(String username, String password, boolean forceMode) throws IOException {
        this.forced = forceMode;
        String mainUrl;
        try {
            mainUrl = Jsoup
                    .connect(SCHOOL_URL)
                    .userAgent(getUserAgent())
                    .followRedirects(false)
                    .get().select("#table1")
                    .select("a").get(0).attr("href");
        } catch (Throwable t) {
            Log.d(LOG_TAG, "<init>", t);
            mainUrl = DEFAULT_MAIN_URL;
        }
        MAIN_URL = mainUrl;

        this.loginCookies = login(0, null, username, password);
    }

    private synchronized Map<String, String> login(int depth, IOException last, String username, String password) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        Log.d(LOG_TAG, "login");
        try {
            return Jsoup
                    .connect(MAIN_URL + LOGIN_URL_ADD)
                    .userAgent(getUserAgent())
                    .data(LOGIN_FIELD, username, PASS_FIELD, password, SUBMIT, SUBMIT_VALUE)
                    .followRedirects(false)
                    .timeout(Constants.CONNECTION_TIMEOUT_SAS)
                    .method(Connection.Method.POST)
                            //.validateTLSCertificates(false)
                    .execute().cookies();
        } catch (IOException e) {
            depth++;
            return login(depth, e, username, password);
        }
    }

    public synchronized Elements getMarksElements(MarksManager.Semester semester) throws IOException {
        Document marksPage = getMarksPage(0, null, semester);
        if (!isLoggedIn(marksPage))
            throw new IllegalStateException("SAS Connector is not logged in");

        return marksPage.select("table.isas-tabulka")
                .select("tr")
                .not("tr.zahlavi");
    }

    public synchronized boolean isLoggedIn() throws IOException {
        return isLoggedIn(getMarksPage(0, null, MarksManager.Semester.AUTO));
    }

    private synchronized boolean isLoggedIn(Document marksPage) {
        return marksPage.select("div.isas-varovani").isEmpty() && marksPage.select("form.isas-form")
                .isEmpty() && !marksPage.select("#isas-menu").isEmpty();
    }

    private synchronized Document getMarksPage(int depth, IOException last, MarksManager.Semester semester) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(MAIN_URL + MARKS_URL_ADD)
                    .userAgent(getUserAgent())
                    .data(SEMESTER, semester.getValue().toString(), SHORT_BY, SHORT_BY_DATE)
                    .followRedirects(false)
                    .timeout(Constants.CONNECTION_TIMEOUT_SAS)
                    .method(Connection.Method.GET)
                            //.validateTLSCertificates(false)
                    .cookies(loginCookies).get();
        } catch (IOException e) {
            depth++;
            return getMarksPage(depth, e, semester);
        }
    }

    public boolean isInForceMode() {
        return forced;
    }

    private String getUserAgent() {
        return "Purkynka/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + "; Linux; rv:"
                + BuildConfig.VERSION_CODE + "; forced:" + forced + " cz-cs) Gecko/20100101 Firefox/42.0";
    }
}
