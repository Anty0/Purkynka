package cz.anty.purkynkamanager.utils.other.icanteen.lunch;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.anty.purkynkamanager.utils.other.Log;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.burza.BurzaLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunch;
import cz.anty.purkynkamanager.utils.other.icanteen.lunch.month.MonthLunchDay;

/**
 * Created by anty on 17.8.15.
 *
 * @author anty
 */
public class Lunches {

    public static List<BurzaLunch> parseBurzaLunches(Elements lunches) {
        Log.d("Lunches", "parseBurzaLunches lunches: " + lunches.toString());

        List<BurzaLunch> lunchList = new ArrayList<>();

        for (Element element : lunches) {
            Elements lunchElements = element.children();

            String onClickText = lunchElements.get(5).child(0).attr("onClick");
            int startIndex = onClickText.indexOf("document.location='") + "document.location='".length();
            String urlAdd = onClickText.substring(startIndex, onClickText.indexOf("';", startIndex));

            try {
                lunchList.add(new BurzaLunch(BurzaLunch.LunchNumber.parseLunchNumber(lunchElements.get(0).text()),
                        BurzaLunch.DATE_FORMAT.parse(lunchElements.get(1).text().split("\n")[0]), lunchElements.get(2).text(), lunchElements.get(3).text(),
                        Integer.parseInt(lunchElements.get(4).text()), urlAdd));
            } catch (ParseException e) {
                Log.d("Lunches", "parseBurzaLunches", e);
            }
        }

        return lunchList;
    }

    public static List<MonthLunchDay> parseMonthLunches(Elements lunches) {
        List<MonthLunchDay> lunchList = new ArrayList<>();

        for (Element element : lunches) {
            Elements lunchElements = element.children();

            List<MonthLunch> monthLunches = new ArrayList<>();
            Elements lunchesElements = lunchElements.get(1).select("div.jidelnicekItem");//child(0).children();
            for (Element lunchElement : lunchesElements) {
                String name = lunchElement.child(0).child(1).text().split("\n")[0].trim();

                MonthLunch.State state;
                if (!lunchElement.select("a." + MonthLunch.State.ENABLED).isEmpty()) {
                    state = MonthLunch.State.ENABLED;
                } else if (!lunchElement.select("a." + MonthLunch.State.ORDERED).isEmpty()) {
                    state = MonthLunch.State.ORDERED;
                } else if (!lunchElement.select("a." + MonthLunch.State.DISABLED).isEmpty()) {
                    state = MonthLunch.State.DISABLED;
                } else state = MonthLunch.State.UNKNOWN;

                if (state.equals(MonthLunch.State.DISABLED)
                        && lunchElement.select("a." + MonthLunch.State.DISABLED)
                        .get(0).child(0).text().contains("nelze zrušit")) {
                    state = MonthLunch.State.DISABLED_ORDERED;
                }

                Elements buttons = lunchElement.select("a.btn");
                String toBurzaUrlAdd = null;
                MonthLunch.BurzaState burzaState = null;
                String orderUrlAdd = null;
                switch (buttons.size()) {
                    case 2:
                        String onClickText1 = buttons.get(1).attr("onClick");
                        int startIndex1 = onClickText1.indexOf("'") + "'".length();
                        toBurzaUrlAdd = onClickText1.substring(startIndex1, onClickText1.indexOf("'", startIndex1));
                        burzaState = buttons.get(1).text().contains(MonthLunch.BurzaState.TO_BURZA.toString())
                                ? MonthLunch.BurzaState.TO_BURZA : MonthLunch.BurzaState.FROM_BURZA;
                    case 1:
                        String onClickText = buttons.get(0).attr("onClick");
                        int startIndex = onClickText.indexOf("'") + "'".length();
                        orderUrlAdd = onClickText.substring(startIndex, onClickText.indexOf("'", startIndex));
                        break;
                }

                monthLunches.add(new MonthLunch(name, orderUrlAdd, state, toBurzaUrlAdd, burzaState));
            }

            try {
                lunchList.add(new MonthLunchDay(MonthLunchDay.DATE_PARSE_FORMAT.parse(lunchElements.get(0).attr("id").replace("day-", "")),
                        monthLunches.toArray(new MonthLunch[monthLunches.size()])));
            } catch (ParseException e) {
                Log.d("Lunches", "parseMonthLunches", e);
            }
        }

        return lunchList;
    }
}
