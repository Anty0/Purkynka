package cz.anty.purkynkamanager.utils.sas.mark;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.anty.purkynkamanager.utils.Log;

/**
 * Created by anty on 8.6.15.
 *
 * @author anty
 */
public class Marks {

    public static List<Lesson> toLessons(Elements elementMarks) {
        return toLessons(parseMarks(elementMarks));
    }

    public static List<Lesson> toLessons(List<Mark> marks) {
        List<Lesson> lessons = new ArrayList<>();
        for (Mark mark : marks) {
            Lesson lesson = new Lesson(mark.getLongLesson(), mark.getShortLesson());
            if (lessons.contains(lesson)) {
                lessons.get(lessons.indexOf(lesson)).addMark(mark);
            } else {
                lesson.addMark(mark);
                lessons.add(lesson);
            }
        }
        Collections.sort(lessons, new Comparator<Lesson>() {
            @Override
            public int compare(Lesson lhs, Lesson rhs) {
                return lhs.getShortName().compareTo(rhs.getShortName());
            }
        });
        return lessons;
    }

    public static List<Mark> parseMarks(Elements elementMarks) {
        List<Mark> marks = new ArrayList<>();

        if (!elementMarks.isEmpty()) {
            Elements firstMark = elementMarks.get(0).select("td");
            if (firstMark.isEmpty() || firstMark.get(0).text().toLowerCase().contains("žádné"))
                return marks;
        }

        for (Element mark : elementMarks) {
            try {
                marks.add(parseMark(mark));
            } catch (IllegalArgumentException e) {
                Log.d("Marks", "parseMarks", e);
            }
        }
        return marks;
    }

    public static Mark parseMark(Element mark) {
        Elements markData = mark.select("td");

        try {
            return new Mark(Mark.DATE_FORMAT.parse(markData.get(0).text()),
                    markData.get(1).text(), markData.get(1).attr("title"), markData.get(2).text(),
                    Double.parseDouble(markData.get(3).text()), markData.get(4).text(),
                    Integer.parseInt(markData.get(5).text()), markData.get(6).text(), markData.get(7).text());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Parameter error: invalid date " + markData.get(0).text(), e);
        }
    }

}
