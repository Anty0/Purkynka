package cz.anty.utils.attendance.man;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.anty.utils.attendance.AttendanceConnector;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Mans {

    public static List<Man> parseMans(Elements elementMans) {
        List<Man> mans = new ArrayList<>();

        /*if (!elementMans.isEmpty()) {
            Elements firstMan = elementMans.get(0).select("td");
            if (firstMan.isEmpty() || firstMan.get(0).text().toLowerCase().contains("žádné")) return mans;
        }*/

        for (Element man : elementMans) {
            mans.add(parseMan(man));
        }
        return mans;
    }

    public static Man parseMan(Element man) {
        Man.Builder builder = new Man.Builder();

        Elements manData = man.select("td");
        for (int i = 0; i < manData.size(); i++) {
            String manInfoText = manData.get(i).text();
            switch (i) {
                case 0:
                    //name
                    builder.setName(manInfoText);
                    break;
                case 1:
                    //class
                    builder.setClassString(manInfoText);
                    break;
                case 2:
                    //date
                    try {
                        builder.setLastEnter(AttendanceConnector.DATE_FORMAT.parse(manInfoText));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Parameter error: invalid date " + manInfoText, e);
                    }
                    break;
                case 3:
                    //inSchool
                    builder.setInSchool(!manInfoText.toLowerCase(Locale.getDefault()).contains("ne"));
                    break;
                default:
                    //unknown parameter
                    throw new IllegalArgumentException("Parameter error: " + manInfoText);
            }
        }

        return builder.get();
    }

}
