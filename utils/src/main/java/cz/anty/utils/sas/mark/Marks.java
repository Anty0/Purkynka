package cz.anty.utils.sas.mark;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.anty.utils.sas.SASConnector;

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
            marks.add(parseMark(mark));
        }
        return marks;
    }

    public static Mark parseMark(Element mark) {
        Mark.Builder builder = new Mark.Builder();

        Elements markData = mark.select("td");
        for (int i = 0; i < markData.size(); i++) {
            Element markInfo = markData.get(i);
            String markInfoText = markInfo.text();
            switch (i) {
                case 0:
                    //date
                    try {
                        builder.setDate(SASConnector.DATE_FORMAT.parse(markInfoText));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Parameter error: invalid date " + markInfoText, e);
                    }
                    break;
                case 1:
                    //lesson
                    builder.setShortLesson(markInfoText)
                            .setLongLesson(markInfo.attr("title"));
                    break;
                case 2:
                    //mark
                    builder.setValueToShow(markInfoText);
                    break;
                case 3:
                    //mark value
                    builder.setValue(Double.parseDouble(markInfoText));
                    break;
                case 4:
                    //type
                    builder.setType(markInfoText);
                    break;
                case 5:
                    //weight
                    builder.setWeight(Integer.parseInt(markInfoText));
                    break;
                case 6:
                    //note
                    builder.setNote(markInfoText);
                    break;
                case 7:
                    //teacher
                    builder.setTeacher(markInfoText);
                    break;
                default:
                    //unknown parameter
                    throw new IllegalArgumentException("Parameter error: " + markInfoText);
            }
        }

        return builder.get();
    }

}
