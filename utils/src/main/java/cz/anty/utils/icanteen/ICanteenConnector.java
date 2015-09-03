package cz.anty.utils.icanteen;

import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class ICanteenConnector {

    //public static final SimpleDateFormat DATE_PARSE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    private static final String LOGIN_URL = "http://stravovani.sspbrno.cz:8080/faces/j_spring_security_check";
    private static final String LOGIN_FIELD = "j_username";
    private static final String PASS_FIELD = "j_password";
    private static final String CHECKBOX_SAVE = "_spring_security_remember_me";
    private static final String CHECKBOX_SAVE_VALUE = "true";
    private static final String TERMINAL = "terminal";
    private static final String TERMINAL_VALUE = "false";
    private static final String TYPE = "type";
    private static final String TYPE_VALUE = "web";
    private static final String TARGET_URL = "targetUrl";
    private static final String TARGET_URL_VALUE = "/faces/secured/main.jsp?terminal=false&amp;status=true&amp;printer=false&amp;keyboard=false";

    private static final String BURZA_URL = "http://stravovani.sspbrno.cz:8080/faces/secured/burza.jsp";//?terminal=false&keyboard=false&printer=false";
    private static final String MAIN_URL = "http://stravovani.sspbrno.cz:8080/faces/secured/main.jsp";//?terminal=false&keyboard=false&printer=false";
    private static final String MONTH_URL = "http://stravovani.sspbrno.cz:8080/faces/secured/month.jsp";//?terminal=false&keyboard=false&printer=false";

    private static final String ORDER_URL_START = "http://stravovani.sspbrno.cz:8080/faces/secured/";

    private final Map<String, String> loginCookies;

    public ICanteenConnector(String username, String password) throws IOException {
        this.loginCookies = login(0, null, username, password);
    }

    private synchronized Map<String, String> login(int depth, IOException last, String username, String password) throws IOException {//// TODO: 1.9.15 REPAIR (sometimes NOT WORKING)
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(LOGIN_URL)
                    .data(LOGIN_FIELD, username, PASS_FIELD, password, CHECKBOX_SAVE, CHECKBOX_SAVE_VALUE,
                            TERMINAL, TERMINAL_VALUE, TYPE, TYPE_VALUE, TARGET_URL, TARGET_URL_VALUE)
                    .method(Connection.Method.POST)
                    .execute().cookies();
        } catch (IOException e) {
            depth++;
            return login(depth, e, username, password);
        }
    }

    public synchronized boolean isLoggedIn() throws IOException {
        return isLoggedIn(getMainPage(0, null));
    }

    private synchronized boolean isLoggedIn(Document page) {
        return page.select("div.login_menu").isEmpty();
    }

    public synchronized void orderBurzaLunch(String urlAdd) throws IOException {
        Jsoup.connect(ORDER_URL_START + urlAdd.replace("&amp;", "&"))
                .cookies(loginCookies).execute();
    }

    public synchronized void orderMonthLunch(String urlAdd) throws IOException {
        Jsoup.connect(ORDER_URL_START + urlAdd.replace("&amp;", "&"))
                .cookies(loginCookies).execute();
    }

    public synchronized Elements getMonthElements() throws IOException {
        Document monthPage = getMonthPage(0, null);
        if (!isLoggedIn(monthPage))
            throw new IllegalStateException("iCanteen Connector is not logged in");

        if (AppDataManager.isDebugMode(null))
            Log.v("ICanteenConnector", "getMonthElements startElements: " + monthPage);

        Elements toReturn = monthPage.select("div#mainContext")
                .select("table")
                .select("form.objednatJidlo-");
        if (AppDataManager.isDebugMode(null))
            Log.v("ICanteenConnector", "getMonthElements finalElements: " + toReturn);
        return toReturn;
    }


    public synchronized Elements getBurzaElements() throws IOException {
        Document burzaPage = getBurzaPage(0, null);
        if (!isLoggedIn(burzaPage))
            throw new IllegalStateException("iCanteen Connector is not logged in");

        if (AppDataManager.isDebugMode(null))
            Log.v("ICanteenConnector", "getBurzaElements startElements: " + burzaPage);

        Elements toReturn = burzaPage
                .select("div#mainContext")
                .select("table")
                .select("tr");
        toReturn.remove(0);
        if (AppDataManager.isDebugMode(null))
            Log.v("ICanteenConnector", "getBurzaElements finalElements: " + toReturn);
        return toReturn;
    }


    private synchronized Document getMainPage(int depth, IOException last) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(MAIN_URL)
                    .cookies(loginCookies).get();
        } catch (IOException e) {
            depth++;
            return getMainPage(depth, e);
        }
    }

    private synchronized Document getMonthPage(int depth, IOException last) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(MONTH_URL)
                    .cookies(loginCookies).get();
        } catch (IOException e) {
            depth++;
            return getMonthPage(depth, e);
        }
    }

    private synchronized Document getBurzaPage(int depth, IOException last) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(BURZA_URL)
                    .cookies(loginCookies).get();
        } catch (IOException e) {
            depth++;
            return getBurzaPage(depth, e);
        }
    }
}
