package cz.anty.utils.teacher;

import android.os.Build;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anty on 22.6.15.
 *
 * @author anty
 */
public class TeachersListConnector {

    private static final int MAX_TRY = 3;
    private static final String DEFAULT_URL = "http://www.sspbrno.cz/view.php?cisloclanku=2005090605";

    public static Elements getTeachersElements() throws IOException {
        return getTeachersElements(0, null);
    }

    private static Elements getTeachersElements(int depth, IOException last) throws IOException {
        if (depth >= MAX_TRY) throw last;
        try {
            Elements elements = Jsoup.connect(DEFAULT_URL)
                    .get().getElementById("table4").children().get(0).children().select("tr");
            elements.remove(0);

            List<Element> elementsToRemove = new ArrayList<>();
            for (Element element : elements) {
                if (element.text().length() < 3/*!element.select("[colSpan=3]").isEmpty()*/) {
                    elementsToRemove.add(element);
                }
            }
            elements.removeAll(elementsToRemove);

            return elements;
        } catch (IOException e) {
            depth++;
            if (Build.VERSION.SDK_INT >= 19 && last != null) e.addSuppressed(last);
            return getTeachersElements(depth, e);
        }
    }

    public static List<Teacher> parseTeachers(Elements teachersElements) {
        List<Teacher> teachers = new ArrayList<>();
        for (Element element : teachersElements) {
            Elements children = element.children();
            teachers.add(new Teacher(children.get(0).text(), children.get(1).text(), children.get(2).text()));
        }
        return teachers;
    }

    public static List<Teacher> getTeachers() throws IOException {
        return parseTeachers(getTeachersElements());
    }

}
