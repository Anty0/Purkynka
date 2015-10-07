package cz.anty.utils;

/**
 * Created by anty on 26.7.15.
 *
 * @author anty
 */
public class Constants {

    public static final int MAX_TRY = 3;

    public static final int NOTIFICATION_ID_SAS_MANAGER_SERVICE = 10;
    public static final int NOTIFICATION_ID_TEACHERS_ATTENDANCE = 20;
    public static final int NOTIFICATION_ID_TIMETABLE_LESSON = 80;
    public static final int NOTIFICATION_ID_UPDATE = 30;
    public static final int NOTIFICATION_ID_TRACKING = 40;
    public static final int NOTIFICATION_ID_I_CANTEEN_BURZA = 50;
    public static final int NOTIFICATION_ID_I_CANTEEN_BURZA_RESULT = 60;
    public static final int NOTIFICATION_ID_I_CANTEEN_LOGIN_EXCEPTION = 70;
    public static final int NOTIFICATION_ID_I_CANTEEN_MONTH = 80;

    public static final int WAIT_TIME_ON_BIND = 100;
    public static final int WAIT_TIME_FIRST_REPEAT = 10;
    public static final long WAIT_TIME_TEACHERS_ATTENDANCE = 1000 * 60 * 15;
    public static final long WAIT_TIME_SAS_MARKS_REFRESH = 1000 * 60 * 5;
    public static final int WAIT_TIME_WIFI_LOGIN = 1500;

    public static final long REPEAT_TIME_UPDATE = 1000 * 60 * 60 * 5;
    public static final long REPEAT_TIME_SAS_MARKS_UPDATE = 1000 * 60 * 15;
    //public static final long REPEAT_TIME_TEACHERS_ATTENDANCE = 1000 * 60;
    public static final long REPEAT_TIME_TRACKING_ATTENDANCE = 1000 * 60 * 5;
    public static final long REPEAT_TIME_IC_LUNCHES_UPDATE = 1000 * 60 * 90;

    //-------------------------------------------------------------
    public static final int SPECIAL_ITEM_PRIORITY_NEW_UPDATE = 100;
    public static final int SPECIAL_ITEM_PRIORITY_SHARE = 95;
    //-------------------------------------------------------------
    public static final int SPECIAL_ITEM_PRIORITY_SAS_LOGIN = 86;
    public static final int SPECIAL_ITEM_PRIORITY_WIFI_LOGIN = 85;
    public static final int SPECIAL_ITEM_PRIORITY_IC_LOGIN = 84;
    public static final int SPECIAL_ITEM_PRIORITY_ADD_TIMETABLE = 83;
    //-------------------------------------------------------------
    public static final int SPECIAL_ITEM_PRIORITY_IC_NEW_LUNCHES = 80;
    public static final int SPECIAL_ITEM_PRIORITY_TIMETABLE = 75;
    public static final int SPECIAL_ITEM_PRIORITY_SAS_BAD_LESSON = 70;
    public static final int SPECIAL_ITEM_PRIORITY_TRACKING = 65;
    //-------------------------------------------------------------
    public static final int SPECIAL_ITEM_PRIORITY_LOADING_ITEM = 50;
    //-------------------------------------------------------------

    public static final String SETTINGS_NAME_ATTENDANCE = "AttendanceData";
    public static final String SETTING_NAME_DISPLAY_TEACHERS_ATTENDANCE_WARNINGS = "DISPLAY_TEACHERS_WARNING";
    public static final String SETTING_NAME_DISPLAY_TRACKING_ATTENDANCE_WARNINGS = "DISPLAY_TRACKING_WARNING";
    public static final String SETTING_NAME_TRACKING_MANS_SAVE = "TRACKING_MANS";
    public static final String SETTING_NAME_MANS_SAVE_VERSION = "MANS_SAVE_VERSION";
    public static final String SETTING_NAME_ADD_LAST_UPDATE = " LAST_UPDATE";


    public static final String SETTINGS_NAME_TIMETABLE_ATTENDANCE = "ATTENDANCE";
    public static final String SETTING_NAME_ADD_LAST_NOTIFY = " LAST_NOTIFY";

    public static final String SETTINGS_NAME_MAIN = "MainData";
    public static final String SETTING_NAME_LANGUAGE = "LANGUAGE";
    public static final String SETTING_NAME_SHOW_SHARE = "SHOW_SHARE";
    public static final String SETTING_NAME_SHOW_DESCRIPTION = "SHOW_DESCRIPTION";
    public static final String SETTING_NAME_FIRST_START = "FIRST_START";
    public static final String SETTING_NAME_LATEST_CODE = "LATEST_CODE";
    public static final String SETTING_NAME_LATEST_NAME = "LATEST_NAME";
    public static final String SETTING_NAME_LATEST_TERMS_CODE = "LATEST_TERMS_CODE";

    public static final String SETTINGS_NAME_TIMETABLES = "TimetablesData";
    public static final String SETTING_NAME_DISPLAY_LESSON_WARNINGS = "DISPLAY_TRACKING_WARNING";
    public static final String SETTING_NAME_APP_WIDGET = "appwidget_";
    public static final String SETTING_NAME_SIZE = "SIZE";
    public static final String SETTING_NAME_ADD_TIMETABLE_NAME = "NAME_";
    public static final String SETTING_NAME_ADD_TIMETABLE = "TIMETABLE_";
    public static final String SETTING_NAME_ADD_TIMETABLE_SAVE_VERSION = "SAVE_VERSION_";

    public static final String SETTINGS_NAME_TEACHERS = "TeachersData";
    public static final String SETTING_NAME_TEACHERS_SAVE_VERSION = "TEACHERS_SAVE_VERSION";
    public static final String SETTING_NAME_TEACHERS = "TEACHERS";
    public static final String SETTING_NAME_LAST_REFRESH = "LAST_REFRESH";

    public static final String SETTINGS_NAME_MARKS = "MarksData";
    public static final String SETTING_NAME_MARKS_SAVE_VERSION = "MARKS_SAVE_VERSION";
    public static final String SETTING_NAME_ADD_MARKS = "MARKS";

    public static final String SETTINGS_NAME_LUNCHES = "LunchesData";
    public static final String SETTING_NAME_LUNCHES_SAVE_VERSION = "LUNCHES_SAVE_VERSION";
    public static final String SETTING_NAME_MONTH_LUNCHES = "MONTH_LUNCHES";

    public static final String SETTINGS_NAME_SAS = "SASLoginData";
    public static final String SETTING_NAME_LOGIN = "LOGIN";
    public static final String SETTING_NAME_PASSWORD = "PASSWORD";
    public static final String SETTING_NAME_LOGGED_IN = "LOGGED_IN";
    public static final String SETTING_NAME_MARKS_UPDATE = "MARKS_UPDATE";

    public static final String SETTINGS_NAME_WIFI = "WIFILoginData";
    public static final String SETTING_NAME_WAIT_LOGIN = "WAIT_LOGIN";
    public static final String SETTING_NAME_AUTO_LOGIN = "AUTO_LOGIN";

    public static final String SETTINGS_NAME_I_CANTEEN = "iCanteenLoginData";
    public static final String SETTING_NAME_NEW_MONTH_LUNCHES = "NEW_MONTH_LUNCHES";
    public static final String SETTING_NAME_NOTIFY_NEW_MONTH_LUNCHES = "NOTIFY_NEW_MONTH_LUNCHES";

    public static final String SETTINGS_NAME_DEBUG = "DEBUGSettingsData";
    public static final String SETTING_NAME_DATA_SAVE_VERSION = "DATA_SAVE_VERSION";
    public static final String SETTING_NAME_DEBUG_MODE = "DEBUG_MODE";
}
