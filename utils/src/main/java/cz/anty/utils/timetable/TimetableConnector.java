package cz.anty.utils.timetable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.Constants;
import cz.anty.utils.Log;
import cz.anty.utils.WrongLoginDataException;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class TimetableConnector {

    private static final String DEFAULT_URL_NAV_BAR = "http://www.sspbrno.cz/rozvrh/frames/navbar.htm";
    private static final String DEFAULT_URL_TIMETABLE = "http://www.sspbrno.cz/rozvrh/%1$s/%2$s/%2$s%3$s.htm";//5

    public static String[] getClasses() throws IOException {
        return getClasses(getNavBar());
    }

    private static String[] getClasses(Document navBar) throws IOException {
        String toFind = "var classes = [\"";
        String text = navBar.head().toString();
        int index = text.indexOf(toFind) + toFind.length();
        return text.substring(index, text.indexOf("\"]", index)).split("\",\"");
    }

    private static String getType(Document navBar) {
        return navBar.select("select[name=type]").get(0).child(0).attr("value");
    }

    private static String getWeek(Document navBar) {
        return navBar.select("select[name=week]").get(0).child(0).attr("value");
    }

    private static int getLessonIndex(String[] classes, String name) throws WrongLoginDataException {
        if (name == null) throw new WrongLoginDataException();
        name = name.toLowerCase(Locale.ENGLISH);
        List<Integer> possibleSolutions = new ArrayList<>();
        for (int i = 0; i < classes.length; i++) {
            String lesson = classes[i].toLowerCase(Locale.ENGLISH);
            if (lesson.equals(name)) {
                possibleSolutions.clear();
                possibleSolutions.add(i);
                break;
            }
            if (name.contains(lesson)) {
                possibleSolutions.add(i);
            }
        }
        if (possibleSolutions.size() != 1) throw new WrongLoginDataException();
        return possibleSolutions.get(0) + 1;
    }

    public static void tryLoadTimetable(Timetable timetable) throws IOException {
        Document navBar = getNavBar();

        String lessonIndex = Integer.toString(getLessonIndex(
                getClasses(navBar), timetable.getName()));
        StringBuilder index = new StringBuilder();
        for (int i = lessonIndex.length(); i < 5; i++) {
            index.append('0');
        }
        index.append(lessonIndex);

        String url = String.format(DEFAULT_URL_TIMETABLE,
                getWeek(navBar), getType(navBar), index.toString());
        //Log.i(null, url);
        Elements elements = Jsoup.connect(url).get().select("td[rowspan=6][colspan=2]");
        //Log.d(null, elements.toString());

        for (int i = 0, elementsSize = elements.size(); i < elementsSize; i++) {
            Elements elements1 = elements.get(i).select("td").not("[rowspan=6]");

            if (elements1.get(0).text().equals("")) continue;

            timetable.setLesson(new Lesson("", elements1.get(0).text(),
                            elements1.get(2).text(), elements1.get(1).text()),
                    i / Timetable.MAX_LESSONS, i % Timetable.MAX_LESSONS);
        }
    }

    private static Document getNavBar() throws IOException {
        IOException lastException = null;
        for (int i = 0; i < Constants.MAX_TRY; i++) {
            try {
                return Jsoup.connect(DEFAULT_URL_NAV_BAR)
                        .followRedirects(false).get();
            } catch (IOException e) {
                Log.d(TimetableConnector.class.getSimpleName(), "getNavBar", e);
                lastException = e;
            }
        }
        throw lastException;
    }
}
