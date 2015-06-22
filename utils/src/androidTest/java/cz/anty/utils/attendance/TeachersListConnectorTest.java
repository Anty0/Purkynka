package cz.anty.utils.attendance;

import junit.framework.TestCase;

import cz.anty.utils.teacher.Teacher;
import cz.anty.utils.teacher.TeachersListConnector;

/**
 * Created by anty on 22.6.15.
 *
 * @author anty
 */
public class TeachersListConnectorTest extends TestCase {

    public void testGetTeachersElements() throws Exception {
        StringBuilder builder = new StringBuilder();
        for (Teacher teacher : TeachersListConnector.getTeachers()) {
            builder.append(teacher).append("\n");
        }
        System.out.println(builder);
    }
}