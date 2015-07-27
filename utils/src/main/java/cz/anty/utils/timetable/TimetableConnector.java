package cz.anty.utils.timetable;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.WrongLoginDataException;

/**
 * Created by anty on 18.6.15.
 *
 * @author anty
 */
public class TimetableConnector {

    private static final String DEFAULT_URL_START = "http://www.sspbrno.cz/rozvrh/38/c/c000";
    private static final String DEFAULT_URL_END = ".htm";
    private static final String[] LESSONS_NAMES = new String[]{
            "E1", "E4", "E2",
            "I2", "I3", "I4", "I1",
            "K2", "K3", "K1",
            "L2A", "L2B", "L4L", "L3A", "L4M", "L4CR", "L3B", "L1A", "L1B",
            "M2", "M4", "M1",
            "P4", "R3",
            "S2A", "S2B", "S2C", "S3A", "S3B", "S3C", "S4A", "S4B", "S4C", "S1A", "S1B", "S1C",
            "V2A", "V2B", "V2C", "V2D", "V3A", "V3B", "V3D", "V4B", "V4C", "V4DS", "V1A", "V1B", "V1C", "V1D",
            "Z2", "Z3", "Z4K", "Z1", "R2"/*,"dozor"*/};

    private static int getLessonIndex(String name) throws WrongLoginDataException {
        if (name == null) throw new WrongLoginDataException();
        name = name.toLowerCase(Locale.ENGLISH);
        List<Integer> possibleSolutions = new ArrayList<>();
        for (int i = 0; i < LESSONS_NAMES.length; i++) {
            String lesson = LESSONS_NAMES[i].toLowerCase(Locale.ENGLISH);
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
        int lessonIndex = getLessonIndex(timetable.getName());
        String url = DEFAULT_URL_START + (lessonIndex < 10 ? "0" + lessonIndex : lessonIndex) + DEFAULT_URL_END;
        //Log.i(null, url);
        Elements elements = Jsoup.connect(url).get().select("td[rowspan=6][colspan=2]");
        //Log.d(null, elements.toString());

        for (int i = 0, elementsSize = elements.size(); i < elementsSize; i++) {
            Elements elements1 = elements.get(i).select("td").not("[rowspan=6]");/*font*/

            if (elements1.get(0).text().equals("")) continue;

            timetable.setLesson(new Lesson("", elements1.get(0).text(),
                            elements1.get(2).text(), elements1.get(1).text()),
                    i / Timetable.MAX_LESSONS, i % Timetable.MAX_LESSONS);

            /*for (int j = 0, elements1Size = elements1.size(); j < elements1Size; j++) {
                Element element = elements1.get(j);
                Log.d(null, element.text().equals("") ? "FREE" : element.text());
                //Log.d(null, element1.toString());
            }*/

            //Log.d(null, "----------------------------");
        }
    }
}
