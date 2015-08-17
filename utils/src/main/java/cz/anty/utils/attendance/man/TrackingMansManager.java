package cz.anty.utils.attendance.man;

import android.content.Context;
import android.support.annotation.NonNull;
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

    public TrackingMansManager(@NonNull Context context) {
        this.context = context;
        reload(null);
    }

    public TrackingMansManager(@NonNull String data) {
        this.context = null;
        reload(data);
    }

    public TrackingMansManager reload(String txtData) {
        if (txtData == null)
            if (context == null) txtData = "";
            else txtData = context
                    .getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                    .getString(Constants.SETTING_NAME_TRACKING_MANS_SAVE, "");

        String[] data = txtData.split(MAIN_SPLIT_VALUE);
        if (data[0].equals("")) return this;
        synchronized (mans) {
            mans.clear();
            for (String manData : data) {
                String[] manDataS = manData.split(MAN_SPLIT_VALUE);
                try {
                    mans.add(new Man(manDataS[0], manDataS[1],
                            AttendanceConnector.DATE_FORMAT.parse(manDataS[2]),
                            Man.IsInSchoolState.parseIsInSchoolState(manDataS[3])));
                } catch (ParseException e) {
                    if (AppDataManager.isDebugMode(context))
                        Log.d("TrackingMansManager", "load", e);
                }
            }
        }
        return this;
    }

    public TrackingMansManager apply() {
        if (context != null)
            context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE).edit()
                    .putString(Constants.SETTING_NAME_TRACKING_MANS_SAVE, toString()).apply();
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

    @Override
    public String toString() {
        StringBuilder data = new StringBuilder();
        synchronized (mans) {
            if (!mans.isEmpty()) {
                Man man2 = mans.get(0);
                data.append(man2.getName().replace(MAN_SPLIT_VALUE, "?????"))
                        .append(MAN_SPLIT_VALUE)
                        .append(man2.getClassString().replace(MAN_SPLIT_VALUE, "?????"))
                        .append(MAN_SPLIT_VALUE)
                        .append(man2.getLastEnterAsString().replace(MAN_SPLIT_VALUE, "?????"))
                        .append(MAN_SPLIT_VALUE)
                        .append(man2.isInSchool());
                for (Man man : mans) {
                    if (man == man2) continue;
                    data.append(MAIN_SPLIT_VALUE)
                            .append(man.getName().replace(MAN_SPLIT_VALUE, "?????"))
                            .append(MAN_SPLIT_VALUE)
                            .append(man.getClassString().replace(MAN_SPLIT_VALUE, "?????"))
                            .append(MAN_SPLIT_VALUE)
                            .append(man.getLastEnterAsString().replace(MAN_SPLIT_VALUE, "?????"))
                            .append(MAN_SPLIT_VALUE)
                            .append(man.isInSchool());
                }
            }
        }
        return data.toString();
    }
}
