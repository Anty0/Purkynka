package cz.anty.purkynkamanager;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testSASConnector() throws Exception {
        //SASConnector connector = new SASConnector("xkuchy4", "Ep8Rt4sk");
        //android.util.Log.d("TEST", "LOGGED_IN? " + connector.isLoggedIn());
        //android.util.Log.d("TEST", "MARKS_ELEMENTS: " + connector.getMarksElements(MarksManager.Semester.AUTO).toString());
    }
}