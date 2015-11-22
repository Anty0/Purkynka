package cz.anty.purkynkamanager.utils.other.icanteen;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.Lunches;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class ICManager {

    private final String username, password;
    private ICConnector connector = null;

    public ICManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void validate(String username, String password) throws IOException {
        if (!new ICConnector(username, password).isLoggedIn())
            throw new WrongLoginDataException();
    }

    public synchronized void connect() throws IOException {
        if (!isConnected()) connector = new ICConnector(username, password);
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
        String urlAdd = lunch.getOrderUrlAdd();
        if (urlAdd == null) throw new NullPointerException("urlAdd is null");
        connector.orderLunch(urlAdd);
    }

    public synchronized void toBurzaMonthLunch(MonthLunch lunch) throws IOException {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        String urlAdd = lunch.getToBurzaUrlAdd();
        if (urlAdd == null) throw new NullPointerException("urlAdd is null");
        connector.orderLunch(urlAdd);
    }

    public synchronized List<MonthLunchDay> getMonth() throws IOException {
        return getMonth(0);
    }

    private synchronized List<MonthLunchDay> getMonth(int depth) throws IOException {
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
        connector.orderLunch(lunch.getOrderUrlAdd());
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

    public double getCredit() {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        return connector.getCredit();
    }
}
