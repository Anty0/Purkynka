package cz.anty.utils.icanteen.lunch;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class Lunches {

    public static List<BurzaLunch> parseBurzaLunches(Elements lunches) {
        List<BurzaLunch> lunchList = new ArrayList<>();

        for (Element element : lunches) {
            Elements lunchElements = element.getAllElements();

            String onClickText = lunchElements.get(5).child(0).attr("onClick");
            int startIndex = onClickText.indexOf("document.location='") + "document.location='".length();
            String urlAdd = onClickText.substring(startIndex, onClickText.indexOf(";", startIndex));

            lunchList.add(new BurzaLunch(BurzaLunch.LunchNumber.parseLunchNumber(lunchElements.get(0).text()),
                    lunchElements.get(1).text(), lunchElements.get(2).text(), lunchElements.get(3).text(),
                    Integer.parseInt(lunchElements.get(4).text()), urlAdd));
        }

        return lunchList;
    }

    public static List<MonthLunch> parseMonthLunches(Elements lunches) {
        List<MonthLunch> lunchList = new ArrayList<>();

        for (Element element : lunches) {
            Elements lunchElements = element.getAllElements();
            //TODO CREATE
            lunchList.add(new MonthLunch());
        }

        return lunchList;
    }
}
