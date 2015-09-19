package cz.anty.utils.attendance.man;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.anty.utils.AppDataManager;
import cz.anty.utils.Constants;
import cz.anty.utils.R;

/**
 * Created by anty on 27.7.15.
 *
 * @author anty
 */
public class TrackingMansManager {

    private static final int MANS_SAVE_VERSION = 1;
    //private static final String MAIN_SPLIT_VALUE = ":;MM;:";
    //private static final String MAN_SPLIT_VALUE = ":;M;:";

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
            if (context == null) return this;
            else txtData = context
                    .getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                    .getString(Constants.SETTING_NAME_TRACKING_MANS_SAVE, "");

        if (context != null) {
            if (context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE)
                    .getInt(Constants.SETTING_NAME_MANS_SAVE_VERSION, -1) != MANS_SAVE_VERSION) {
                apply();
                return this;
            }
        }

        //String[] data = txtData.split(MAIN_SPLIT_VALUE);
        if (txtData.equals("")) return this;
        synchronized (mans) {
            mans.clear();
            mans.addAll(Arrays.asList(new Gson()
                    .fromJson(txtData, Man[].class)));
            /*mans.clear();
            for (String manData : data) {
                String[] manDataS = manData.split(MAN_SPLIT_VALUE);
                try {
                    Log.d("TrackingMansManager", "reload txtData: " + txtData
                                    + "\ndata: " + Arrays.toString(data)
                                    + "\nmanDataS: " + Arrays.toString(manDataS));
                    mans.add(new Man(manDataS[0], manDataS[1],
                            AttendanceConnector.DATE_FORMAT.parse(manDataS[2]),
                            Man.IsInSchoolState.parseIsInSchoolState(manDataS[3])));
                } catch (ParseException e) {
                    Log.d("TrackingMansManager", "reload data: " + txtData, e);
                }
            }*/
        }
        return this;
    }

    public TrackingMansManager apply() {
        if (context != null)
            context.getSharedPreferences(Constants.SETTINGS_NAME_ATTENDANCE, Context.MODE_PRIVATE).edit()
                    .putString(Constants.SETTING_NAME_TRACKING_MANS_SAVE, toString())
                    .putInt(Constants.SETTING_NAME_MANS_SAVE_VERSION, MANS_SAVE_VERSION).apply();
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

    public int indexOf(Man man) {
        synchronized (mans) {
            return mans.indexOf(man);
        }
    }

    public void processMan(@NonNull final Man man, @Nullable final Runnable onChange) {
        if (context == null) return;

        if (contains(man)) {
            new AlertDialog.Builder(context)
                    .setTitle(man.getName())
                            //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                    .setMessage(context.getString(R.string.dialog_text_attendance_stop_tracking)
                            .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                    .setPositiveButton(R.string.but_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            remove(man).apply();
                            if (onChange != null) onChange.run();
                        }
                    })
                    .setNegativeButton(R.string.but_no, null)
                    .setCancelable(true)
                    .show();
        } else {
            String name = man.getName();
            if (name.contains("Kuchyňka") && name.contains("Jiří")) return;

            new AlertDialog.Builder(context)
                    .setTitle(man.getName())
                            //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                    .setMessage(context.getString(R.string.dialog_text_attendance_tracking)
                            .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                    .setPositiveButton(R.string.but_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (AppDataManager.isLoggedIn(AppDataManager.Type.SAS)) {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.dialog_title_terms_warning)
                                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                                        .setMessage(context.getString(R.string.dialog_text_terms_attendance_tracking)
                                                .replace(Constants.STRINGS_CONST_NAME, man.getName()))
                                        .setPositiveButton(R.string.but_accept, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                add(man).apply();
                                                if (onChange != null) onChange.run();
                                            }
                                        })
                                        .setNegativeButton(R.string.but_cancel, null)
                                        .setCancelable(true)
                                        .show();
                            } else {
                                new AlertDialog.Builder(context)
                                        .setTitle(R.string.dialog_title_terms_warning)
                                                //.setIcon(R.mipmap.ic_launcher) // TODO: 2.9.15 use icon A
                                        .setMessage(R.string.dialog_text_attendance_can_not_start_tracking)
                                        .setPositiveButton(R.string.but_cancel, null)
                                        .setCancelable(true)
                                        .show();
                            }
                        }
                    })
                    .setNegativeButton(R.string.but_no, null)
                    .setCancelable(true)
                    .show();
        }
    }

    @Override
    public String toString() {
        //StringBuilder data = new StringBuilder();
        synchronized (mans) {
            return new Gson().toJson(mans.toArray(new Man[mans.size()]));
            /*if (!mans.isEmpty()) {
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
            }*/
        }
        //return data.toString();
    }
}
