package cz.anty.purkynkamanager.utils.other.attendance.man;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.anty.purkynkamanager.utils.other.attendance.AttendanceConnector;

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
        Elements manData = man.select("td");
        try {
            return new Man(manData.get(0).text(), manData.get(1).text(),
                    AttendanceConnector.DATE_FORMAT.parse(manData.get(2).text()),
                    Man.IsInSchoolState.parseIsInSchoolState(manData.get(3).text()));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Parameter error: invalid date " + manData.get(2).text(), e);
        }
    }

}
