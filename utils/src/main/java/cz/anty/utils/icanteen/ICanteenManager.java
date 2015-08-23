package cz.anty.utils.icanteen;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import cz.anty.utils.Constants;
import cz.anty.utils.WrongLoginDataException;
import cz.anty.utils.icanteen.lunch.BurzaLunch;
import cz.anty.utils.icanteen.lunch.Lunches;
import cz.anty.utils.icanteen.lunch.MonthLunch;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class ICanteenManager {

    private final String username, password;
    private ICanteenConnector connector = null;

    public ICanteenManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void validate(String username, String password) throws IOException {
        if (!new ICanteenConnector(username, password).isLoggedIn())
            throw new WrongLoginDataException();
    }

    public synchronized void connect() throws IOException {
        if (!isConnected()) connector = new ICanteenConnector(username, password);
    }

    public synchronized void disconnect() {
        if (isConnected()) connector = null;
    }

    public synchronized boolean isConnected() {
        return connector != null;
    }

    public synchronized boolean isLoggedIn() throws IOException {
        return isConnected() && connector.isLoggedIn();
    }

    public synchronized void orderMonthLunch(MonthLunch lunch) throws IOException {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        //TODO CREATE
        connector.orderMonthLunch(null);
    }

    public synchronized List<MonthLunch> getMonth() throws IOException {
        return getMonth(0);
    }

    private synchronized List<MonthLunch> getMonth(int depth) throws IOException {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        if (depth >= Constants.MAX_TRY) throw new WrongLoginDataException();
        Elements elements;
        try {
            elements = connector.getMonthElements();
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                disconnect();
                connect();
                depth++;
                return getMonth(depth);
            }
            throw e;
        }
        return Lunches.parseMonthLunches(elements);
    }

    public synchronized void orderBurzaLunch(BurzaLunch lunch) throws IOException {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        connector.orderBurzaLunch(lunch.getOrderUrlAdd());
    }

    public synchronized List<BurzaLunch> getBurza() throws IOException {
        return getBurza(0);
    }

    private synchronized List<BurzaLunch> getBurza(int depth) throws IOException {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        if (depth >= Constants.MAX_TRY) throw new WrongLoginDataException();
        Elements elements;
        try {
            elements = connector.getBurzaElements();
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                disconnect();
                connect();
                depth++;
                return getBurza(depth);
            }
            throw e;
        }
        return Lunches.parseBurzaLunches(elements);
    }
}
