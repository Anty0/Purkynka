package cz.anty.purkynkamanager.utils.other.sas;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

import cz.anty.purkynkamanager.utils.other.Constants;
import cz.anty.purkynkamanager.utils.other.WrongLoginDataException;
import cz.anty.purkynkamanager.utils.other.sas.mark.Mark;
import cz.anty.purkynkamanager.utils.other.sas.mark.Marks;
import cz.anty.purkynkamanager.utils.other.sas.mark.MarksManager;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
public class SASManager {

    private final String username, password;
    private SASConnector connector = null;

    public SASManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void validate(String username, String password) throws IOException {
        if (!new SASConnector(username, password, true).isLoggedIn())
            throw new WrongLoginDataException();
    }

    public synchronized void connect(boolean forceMode) throws IOException {
        if (!isConnected()) connector = new SASConnector(username, password, forceMode);
    }

    public synchronized boolean disconnect() {
        if (isConnected()) {
            boolean toReturn = connector.isInForceMode();
            connector = null;
            return toReturn;
        }
        return false;
    }

    public synchronized boolean isConnected() {
        return connector != null;
    }

    public synchronized boolean isLoggedIn() throws IOException {
        return isConnected() && connector.isLoggedIn();
    }

    public synchronized List<Mark> getMarks(MarksManager.Semester semester) throws IOException {
        return getMarks(semester, 0);
    }

    private synchronized List<Mark> getMarks(MarksManager.Semester semester, int depth) throws IOException {
        if (!isConnected()) throw new IllegalStateException("Manager is disconnected");
        if (depth >= Constants.MAX_TRY) throw new WrongLoginDataException();
        Elements marksElements;
        try {
            marksElements = connector.getMarksElements(semester);
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                connect(disconnect());
                depth++;
                return getMarks(semester, depth);
            }
            throw e;
        }
        return Marks.parseMarks(marksElements);
    }
}
