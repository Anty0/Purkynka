package cz.anty.purkynkamanager.utils.other.icanteen;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Map;

import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.Utils;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
class ICConnector {

    private static final String LOG_TAG = "ICConnector";
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
    private long lastRefresh = 0;
    private long lastOrder = 0;

    ICConnector(String username, String password) throws IOException {
        this.loginCookies = login(0, null, username, password);
    }

    private synchronized Map<String, String> login(int depth, IOException last, String username, String password) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            Map<String, String> cookies = Jsoup
                    .connect(LOGIN_URL)
                    .data(LOGIN_FIELD, username, PASS_FIELD, password, CHECKBOX_SAVE, CHECKBOX_SAVE_VALUE,
                            TERMINAL, TERMINAL_VALUE, TYPE, TYPE_VALUE, TARGET_URL, TARGET_URL_VALUE)
                    .method(Connection.Method.POST)
                    .execute().cookies();

            long actual = System.currentTimeMillis();
            lastRefresh = actual;
            lastOrder = actual;
            return cookies;
        } catch (IOException e) {
            depth++;
            return login(depth, e, username, password);
        }
    }

    public synchronized boolean isLoggedIn() throws IOException {
        return isLoggedIn(getPage(MAIN_URL, 0, null));
    }

    private synchronized boolean isLoggedIn(Document page) {
        return page.select("div.login_menu").isEmpty();
    }

    public synchronized void orderLunch(String urlAdd) throws IOException {
        long actual = System.currentTimeMillis();
        long last = lastRefresh > lastOrder ? lastRefresh : lastOrder;
        if (actual - last < Constants.WAIT_TIME_IC_CONNECTION)
            Utils.threadSleep(last + Constants.WAIT_TIME_IC_CONNECTION - actual);

        Connection.Response response = Jsoup.connect(ORDER_URL_START + urlAdd.replace("&amp;", "&"))
                .cookies(loginCookies).execute();

        lastOrder = System.currentTimeMillis();
        Log.v(LOG_TAG, "orderLunch response: " + response.body());
        if (response.body().contains("\"error\":true"))
            throw new IOException("Server error received");
    }

    public synchronized Elements getMonthElements() throws IOException {
        Document monthPage = getPage(MONTH_URL, 0, null);
        if (!isLoggedIn(monthPage))
            throw new IllegalStateException("iCanteen Connector is not logged in");

        //if (AppDataManager.isDebugMode(null))
        //System.out.println("ICConnector getMonthElements startElements:\n" + monthPage);
        //Log.v("ICConnector", "getMonthElements startElements:\n" + monthPage);

        /*if (!isLoggedIn(monthPage))
            throw new WrongLoginDataException();*/

        Elements toReturn = monthPage
                .select("div#mainContext")
                .select("table")
                .select("form[name=objednatJidlo-]");
        Log.v(LOG_TAG, "getMonthElements finalElements: " + toReturn);
        return toReturn;
    }


    public synchronized Elements getBurzaElements() throws IOException {
        Document burzaPage = getPage(BURZA_URL, 0, null);
        if (!isLoggedIn(burzaPage))
            throw new IllegalStateException("iCanteen Connector is not logged in");

        //if (AppDataManager.isDebugMode(null))
        //System.out.println("ICConnector getBurzaElements startElements:\n" + burzaPage);
        //Log.v("ICConnector", "getBurzaElements startElements:\n" + burzaPage);

        if (!isLoggedIn(burzaPage))
            throw new WrongLoginDataException();

        Elements toReturn = burzaPage
                .select("div#mainContext")
                .select("table")
                .select("tr");
        toReturn.remove(0);
        Log.v(LOG_TAG, "getBurzaElements finalElements: " + toReturn);
        return toReturn;
    }


    private synchronized Document getPage(String url, int depth, IOException last) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            long actual = System.currentTimeMillis();
            if (actual - lastOrder < Constants.WAIT_TIME_IC_CONNECTION)
                Utils.threadSleep(lastOrder + Constants.WAIT_TIME_IC_CONNECTION - actual);

            Document document = Jsoup
                    .connect(url)
                    .cookies(loginCookies).get();

            lastRefresh = System.currentTimeMillis();
            return document;
        } catch (IOException e) {
            depth++;
            return getPage(url, depth, e);
        }
    }
}
