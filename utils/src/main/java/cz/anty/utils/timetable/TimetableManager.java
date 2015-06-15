package cz.anty.utils.timetable;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class TimetableManager {

    private final Context context;
    private final List<Timetable> timetables = new ArrayList<>();

    public TimetableManager(Context context) {
        this.context = context;
        String[] timetablesNames = Timetables.getTimetablesNames(context);
        for (String timetableName : timetablesNames) {
            timetables.add(Timetable.loadTimetable(context, timetableName));
        }
    }

    public synchronized void addTimetable(String timetableName) {
        timetables.add(new Timetable(context, timetableName));
    }

    public synchronized void removeTimetable(Timetable timetable) {
        timetables.remove(timetable);
        Timetables.removeTimetablesName(context, timetable.getName());
    }

    public synchronized Timetable[] getTimetables() {
        return timetables.toArray(new Timetable[timetables.size()]);
    }
}
