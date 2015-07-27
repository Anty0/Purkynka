package cz.anty.utils.attendance.man;

import android.content.Context;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.attendance.AttendanceConnector;

/**
 * Created by anty on 27.7.15.
 *
 * @author anty
 */
public class TrackingMansManager {

    private static final String MAIN_SPLIT_VALUE = ":;MM;:";
    private static final String MAN_SPLIT_VALUE = ":;M;:";

    private final Context context;
    private final List<Man> mans = new ArrayList<>();

    public TrackingMansManager(Context context) {
        this.context = context;
        reload();
    }

    public TrackingMansManager reload() {
        String[] data = context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                .getString(Constants.SETTING_NAME_TRACKING_MANS_SAVE, "").split(MAIN_SPLIT_VALUE);
        if (data[0].equals("")) return this;
        synchronized (mans) {
            mans.clear();
            for (String manData : data) {
                String[] manDataS = manData.split(MAN_SPLIT_VALUE);
                try {
                    mans.add(new Man.Builder(manDataS[0], manDataS[1], AttendanceConnector.DATE_FORMAT.parse(manDataS[2]), Boolean.parseBoolean(manDataS[3])).get());
                } catch (ParseException e) {
                    if (AppDataManager.isDebugMode(context))
                        Log.d("TrackingMansManager", "load", e);
                }
            }
        }
        return this;
    }

    public TrackingMansManager apply() {
        StringBuilder data = new StringBuilder();
        synchronized (mans) {
            for (Man man : mans) {
                data.append(man.getName())
                        .append(MAN_SPLIT_VALUE)
                        .append(man.getClassString())
                        .append(MAN_SPLIT_VALUE)
                        .append(man.getLastEnterAsString())
                        .append(MAN_SPLIT_VALUE)
                        .append(man.isInSchool());
            }
        }
        context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE).edit()
                .putString(Constants.SETTING_NAME_TRACKING_MANS_SAVE, data.toString()).apply();
        return this;
    }

    public TrackingMansManager add(Man man) {
        synchronized (mans) {
            mans.add(man);
        }
        return this;
    }

    public boolean contains(Man man) {
        synchronized (mans) {
            return mans.contains(man);
        }
    }

    public TrackingMansManager remove(Man man) {
        synchronized (mans) {
            mans.remove(man);
        }
        return this;
    }

    public Man[] get() {
        synchronized (mans) {
            return mans.toArray(new Man[mans.size()]);
        }
    }
}
