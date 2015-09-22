package cz.anty.utils.wifi;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

import cz.anty.utils.Constants;
import cz.anty.utils.Log;

/**
 * Created by anty on 11.6.15.
 *
 * @author anty
 */
public class WifiLogin {

    public static final String WIFI_NAME = "ISSWF";
    private static final String LOGIN_URL = "http://wifi.sspbrno.cz/login.html";
    //private static final String LOGOUT_URL = "http://wifi.sspbrno.cz/logout.html";
    private static final String LOGIN_FIELD = "username";
    private static final String PASS_FIELD = "password";
    private static final String SUBMIT = "Submit";
    private static final String SUBMIT_VALUE = "Submit";

    public static boolean tryLogin(String username, String password) {
        for (int i = 0; i < Constants.MAX_TRY; i++) {
            try {
                Jsoup.connect(LOGIN_URL)
                        .data("buttonClicked", "4", "err_flag", "0", "err_msg", "", "info_flag", "0", "info_msg", "",
                                "redirect_url", "", LOGIN_FIELD, username, PASS_FIELD, password, SUBMIT, SUBMIT_VALUE)
                        .method(Connection.Method.POST)
                                //.validateTLSCertificates(false)
                        .execute();
                return true;
            } catch (IOException e) {
                Log.d("WifiLogin", "tryLogin", e);
            }
        }
        return false;
        //return !username.equals(password);
    }

}
