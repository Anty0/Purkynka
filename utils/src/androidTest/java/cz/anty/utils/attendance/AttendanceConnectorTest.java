package cz.anty.utils.attendance;

import junit.framework.TestCase;

import cz.anty.utils.attendance.man.Man;
import cz.anty.utils.attendance.man.Mans;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class AttendanceConnectorTest extends TestCase {

    public void testGetSupElements() throws Exception {
        for (Man man : Mans.parseMans(new AttendanceConnector().getSupElements("Kuch", 1))) {
            System.out.println(man);
        }
    }
}