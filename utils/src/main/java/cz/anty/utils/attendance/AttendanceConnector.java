package cz.anty.utils.attendance;

import android.os.Build;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cz.anty.utils.Constants;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class AttendanceConnector {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    //private static final String DEFAULT_LOGIN = "rodic";
    private static final String DEFAULT_URL = "http://www2.sspbrno.cz/main.asp";
    //private static final String LOGIN_FIELD = "LOGIN";
    //private static final String PASS_FIELD = "PASSWORD";
    //private static final String SUBMIT = "prihlas";
    //private static final String SUBMIT_VALUE = "Přihlásit";
    private static final String ATT_URL_ADD = "DRUH";
    private static final String ATT_URL_ADD_VALUE = "7";
    private static final String SEARCH = "KL_SLOVO";
    private static final String SEARCH_SUBMIT = "ODESLI";
    private static final String SEARCH_SUBMIT_VALUE = "Vyhledat";
    private static final String PAGE = "OD";

    //private final Map<String, String> loginCookies;

    /*public AttendanceConnector() throws IOException {
        //this.loginCookies = login(0, null, DEFAULT_LOGIN, DEFAULT_LOGIN);
    }

    private synchronized Map<String, String> login(int depth, IOException last, String username, String password) throws IOException {
        if (depth >= MAX_TRY) throw last;
        try {
            return Jsoup
                    .connect(DEFAULT_URL)
                    .data(SUP_URL_ADD, SUP_URL_ADD_VALUE, LOGIN_FIELD, username,
                            PASS_FIELD, password, SUBMIT, SUBMIT_VALUE)
                    .method(Connection.Method.POST)
                    .execute().cookies();
        } catch (IOException e) {
            depth++;
            return login(depth, e, username, password);
        }
    }*/

    public synchronized Elements getSupElements(String search, int page) throws IOException {
        return getSupElements(0, null, search, page);
    }

    /*private synchronized String encodeString(String toEncode) {
        return toEncode.replace("ň", "%F2").replace("Ň", "%D2");//Š
    }*/

    private synchronized Elements getSupElements(int depth, IOException last, String search, int page) throws IOException {
        if (depth >= Constants.MAX_TRY) throw last;
        try {
            //URLConnection url = new URL(DEFAULT_URL + "?" + SEARCH + "=" + search + "&" + SEARCH_SUBMIT + "=" + SEARCH_SUBMIT_VALUE
            //        + "&" + ATT_URL_ADD + "=" + ATT_URL_ADD_VALUE + "&" + PAGE + "=" + Integer.toString(page)).openConnection();
            String urlStr = DEFAULT_URL + "?" + SEARCH + "=" + URLEncoder.encode(search, "Windows-1250") + "&" + SEARCH_SUBMIT + "=" + SEARCH_SUBMIT_VALUE
                    + "&" + ATT_URL_ADD + "=" + ATT_URL_ADD_VALUE + "&" + PAGE + "=" + Integer.toString(page);
            //Log.d("AttendanceConnector", "getSupElements URLEncoder cp1252: " + URLEncoder.encode(search, "cp1252"));
            //Log.d("AttendanceConnector", "getSupElements URLEncoder us-ascii: " + URLEncoder.encode(search, "us-ascii"));
            //Log.d("AttendanceConnector", "getSupElements URLEncoder UTF8: " + URLEncoder.encode(search, "UTF8"));
            //Log.d("AttendanceConnector", "getSupElements URLEncoder Windows-1250: " + URLEncoder.encode(search, "Windows-1250"));
            //Log.d("AttendanceConnector", "getSupElements encodeString: " + encodeString(search));
            //URL url= new URL(urlStr);
            //URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            Document doc = Jsoup
                    .connect(urlStr)//uri.toASCIIString()
                            //.data(SEARCH, search, SEARCH_SUBMIT, SEARCH_SUBMIT_VALUE,
                            //        ATT_URL_ADD, ATT_URL_ADD_VALUE, PAGE, Integer.toString(page))
                            //.method(Connection.Method.POST)
                            //.postDataCharset(Build.VERSION.SDK_INT >= 19 ? StandardCharsets.US_ASCII.name() : "US-ASCII")
                            //.cookies(loginCookies)
                    .get();

            //Document doc = Jsoup.parse(url.getInputStream(), StandardCharsets.US_ASCII.name(), DEFAULT_URL);

            //System.out.println(doc);
            //System.out.println(urlStr);//uri.toASCIIString()
            //System.out.println("=============================");

            //elements.remove(0);
            return doc.select("table[3]").select("tr[bgcolor=#FFFFFF]");
            //System.out.println("Mark: " + mark);
            //System.out.println("Text: " + mark.text());
        } catch (IOException e) {
            depth++;
            if (Build.VERSION.SDK_INT >= 19 && last != null) e.addSuppressed(last);
            return getSupElements(depth, e, search, page);
        }
    }
}
