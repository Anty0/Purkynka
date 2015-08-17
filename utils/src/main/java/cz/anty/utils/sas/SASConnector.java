package cz.anty.utils.sas;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import cz.anty.utils.Constants;
import cz.anty.utils.sas.mark.MarksManager;

/**
 * Created by anty on 7.6.15.
 *
 * @author anty
 */
public class SASConnector {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private static final String LOGIN_URL = "https://www.sspbrno.cz/ISAS/prihlasit.php";
    private static final String LOGIN_FIELD = "login-isas-username";
    private static final String PASS_FIELD = "login-isas-password";
    private static final String SUBMIT = "login-isas-send";
    private static final String SUBMIT_VALUE = "isas-send";

    private static final String MARKS_URL = "https://www.sspbrno.cz/ISAS/prubezna-klasifikace.php";
    private static final String SEMESTER = "pololeti";
    private static final String SHORT_BY = "zobraz";
    private static final String SHORT_BY_DATE = "datum";
    //private static final String SHORT_BY_LESSONS = "predmety";
    //private static final String SHORT_BY_SCORE = "hodnoceni";

    private final Map<String, String> loginCookies;

    public SASConnector(String username, String password) throws IOException {
        this.loginCookies = login(0, null, username, password);
    }

    private synchronized Map<String, String> login(int depth, IOException last, String username, String password) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(LOGIN_URL)
                    .data(LOGIN_FIELD, username, PASS_FIELD, password, SUBMIT, SUBMIT_VALUE)
                    .method(Connection.Method.POST)
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
        return marksPage.select("div.isas-varovani").isEmpty();
    }

    private synchronized Document getMarksPage(int depth, IOException last, MarksManager.Semester semester) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(MARKS_URL)
                    .data(SEMESTER, semester.getValue().toString(), SHORT_BY, SHORT_BY_DATE)
                    .method(Connection.Method.GET)
                    .cookies(loginCookies).get();
        } catch (IOException e) {
            depth++;
            return getMarksPage(depth, e, semester);
        }
    }
}
