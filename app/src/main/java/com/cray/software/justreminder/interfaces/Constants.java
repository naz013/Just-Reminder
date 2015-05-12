package com.cray.software.justreminder.interfaces;

public class Constants {
    // DB constants
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEXT = "task_text";
    public static final String COLUMN_TYPE = "task_type";
    public static final String COLUMN_NUMBER = "call_number";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_YEAR = "year";
    public static final String COLUMN_HOUR = "hour";
    public static final String COLUMN_MINUTE = "minute";
    public static final String COLUMN_SECONDS = "seconds";
    public static final String COLUMN_REMIND_TIME = "remind_time";
    public static final String COLUMN_REPEAT = "repeat";
    public static final String COLUMN_REMINDERS_COUNT = "reminders_count";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_FEATURE_TIME = "tech_int";
    public static final String COLUMN_DELAY = "tech_lint";
    public static final String COLUMN_TECH_VAR = "tech_var";
    public static final String COLUMN_WEEKDAYS = "tech_lvar";
    public static final String COLUMN_IS_DONE = "done";
    public static final String COLUMN_EXPORT_TO_CALENDAR = "export_calendar";
    public static final String COLUMN_CUSTOM_MELODY = "custom_melody";
    public static final String COLUMN_CUSTOM_RADIUS = "custom_radius";
    public static final String COLUMN_ARCHIVED = "archived";
    public static final String COLUMN_DATE_TIME = "var";
    public static final String COLUMN_CATEGORY = "var2";
    public static final String COLUMN_LED_COLOR = "int";
    public static final String COLUMN_SYNC_CODE = "int2";

    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COLOR = "color";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_ENCRYPTED = "tech";
    public static final String COLUMN_FONT_STYLE = "font_style";
    public static final String COLUMN_FONT_COLOR = "font_color";
    public static final String COLUMN_FONT_SIZE = "font_size";
    public static final String COLUMN_FONT_UNDERLINED = "font_underlined";
    public static final String COLUMN_LINK_ID = "font_crossed";

    public static final String COLUMN_REMINDER_ID = "reminder_id";
    public static final String COLUMN_DELETE_URI = "delete_id";
    public static final String COLUMN_EVENT_TECH = "event_tech";
    public static final String COLUMN_EVENT_VAR = "event_var";
    public static final String COLUMN_EVENT_ID = "event_id";

    public static final String DRIVE_USER_NONE = "none";
    public static final String TASKS_ALL = "all";
    public static final String TASKS_NEED_ACTION = "needsAction";
    public static final String TASKS_COMPLETE = "completed";

    //Shared Prefs Constants
    public static final String APP_UI_PREFERENCES_THEME = "theme_color";
    public static final String APP_UI_PREFERENCES_DRIVE_USER = "ggl_user";
    public static final String APP_UI_PREFERENCES_SCREEN = "screen";
    public static final String APP_UI_PREFERENCES_USE_DARK_THEME = "dark_theme";
    public static final String APP_UI_PREFERENCES_MAP_TYPE = "map_type";
    public static final String APP_UI_PREFERENCES_LOCATION_RADIUS = "radius";
    public static final String APP_UI_PREFERENCES_TRACKING_NOTIFICATION = "tracking_notification";
    public static final String APP_UI_PREFERENCES_VIBRATION_STATUS = "vibration_status";
    public static final String APP_UI_PREFERENCES_SOUND_STATUS = "sound_status";
    public static final String APP_UI_PREFERENCES_WAKE_STATUS = "wake_status";
    public static final String APP_UI_PREFERENCES_INFINITE_SOUND = "infinite_sound";
    public static final String APP_UI_PREFERENCES_CHECKED_LAYOUT = "checked_layout";
    public static final String APP_UI_PREFERENCES_SILENT_SMS = "silent_sms";
    public static final String APP_UI_PREFERENCES_USE_CONTACTS = "use_contacts";
    public static final String APP_UI_PREFERENCES_START_DAY = "start_day";
    public static final String APP_UI_PREFERENCES_DAYS_TO_BIRTHDAY = "days_to";
    public static final String APP_UI_PREFERENCES_DELAY_TIME = "delay_time";
    public static final String APP_UI_PREFERENCES_EVENT_DURATION = "event_duration";
    public static final String APP_UI_PREFERENCES_EXPORT_TO_CALENDAR = "export_to_calendar";
    public static final String APP_UI_PREFERENCES_CALENDAR_NAME = "calendar_name";
    public static final String APP_UI_PREFERENCES_CALENDAR_ID = "calendar_id";
    public static final String APP_UI_PREFERENCES_AUTO_CHECK_BIRTHDAYS = "birthdays_auto_scan";
    public static final String APP_UI_PREFERENCES_LED_STATUS = "led_status";
    public static final String APP_UI_PREFERENCES_LED_COLOR = "led_color";
    public static final String APP_UI_PREFERENCES_MARKER_STYLE = "marker_style";
    public static final String APP_UI_PREFERENCES_INFINITE_VIBRATION = "infinite_vibration";
    public static final String APP_UI_PREFERENCES_AUTO_BACKUP = "auto_backup";
    public static final String APP_UI_PREFERENCES_SMART_FOLD = "smart_fold";
    public static final String APP_UI_PREFERENCES_NOTIFICATION_REPEAT = "notification_repeat";
    public static final String APP_UI_PREFERENCES_NOTIFICATION_REPEAT_INTERVAL = "notification_repeat_interval";
    public static final String APP_UI_PREFERENCES_WEAR_NOTIFICATION = "wear_notification";
    public static final String APP_UI_PREFERENCES_WIDGET_BIRTHDAYS = "widget_birthdays";
    public static final String APP_UI_PREFERENCES_NOTIFICATION_REMOVE = "notification_remove";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_COLOR = "birthday_color";
    public static final String APP_UI_PREFERENCES_CURRENT_COLOR = "current_color";
    public static final String APP_UI_PREFERENCES_STATUS_BAR_ICON = "status_icon";
    public static final String APP_UI_PREFERENCES_STATUS_BAR_NOTIFICATION = "status_notification";
    public static final String APP_UI_PREFERENCES_TRACK_TIME = "tracking_time";
    public static final String APP_UI_PREFERENCES_TRACK_DISTANCE = "tracking_distance";
    public static final String APP_UI_PREFERENCES_NOTE_ENCRYPT = "note_encrypt";
    public static final String APP_UI_PREFERENCES_TEXT_SIZE= "text_size";
    public static final String APP_UI_PREFERENCES_QUICK_NOTE_REMINDER = "quick_note_reminder";
    public static final String APP_UI_PREFERENCES_QUICK_NOTE_REMINDER_TIME = "quick_note_reminder_time";
    public static final String APP_UI_PREFERENCES_SYNC_NOTES = "sync_notes";
    public static final String APP_UI_PREFERENCES_ANIMATIONS = "animation";
    public static final String APP_UI_PREFERENCES_DELETE_NOTE_FILE = "delete_note_file";
    public static final String APP_UI_PREFERENCES_AUTO_LANGUAGE = "auto_language";
    public static final String APP_UI_PREFERENCES_VOICE_LANGUAGE = "voice_language";
    public static final String APP_UI_PREFERENCES_TIME_MORNING = "time_morning";
    public static final String APP_UI_PREFERENCES_TIME_DAY = "time_day";
    public static final String APP_UI_PREFERENCES_TIME_EVENING = "time_evening";
    public static final String APP_UI_PREFERENCES_TIME_NIGHT = "time_night";
    public static final String APP_UI_PREFERENCES_EXPORT_TO_STOCK = "export_to_stock";
    public static final String APP_UI_PREFERENCES_APPLICATION_AUTO_LAUNCH = "application_auto_launch";
    public static final String APP_UI_PREFERENCES_HIDE_TRANSLATION_MENU = "translation_menu";
    public static final String APP_UI_PREFERENCES_LIST_ORDER = "list_ordering";
    public static final String APP_UI_PREFERENCES_NOTES_ORDER = "notes_ordering";
    public static final String APP_UI_PREFERENCES_TASKS_ORDER = "tasks_ordering";
    public static final String APP_UI_PREFERENCES_REMINDERS_IN_CALENDAR = "reminders_in_calendar";
    public static final String APP_UI_PREFERENCES_REMINDERS_COLOR = "reminders_color";
    public static final String APP_UI_PREFERENCES_IS_24_TIME_FORMAT = "24_hour_format";
    public static final String APP_UI_PREFERENCES_UNLOCK_DEVICE = "unlock_device";
    public static final String APP_UI_PREFERENCES_WIDGET_DISTANCE = "widget_distance";
    public static final String APP_UI_PREFERENCES_CALENDAR_FEATURE_TASKS = "feature_tasks";
    public static final String APP_UI_PREFERENCES_MISSED_CALL_REMINDER = "missed_call_reminder";
    public static final String APP_UI_PREFERENCES_MISSED_CALL_TIME = "missed_call_time";
    public static final String APP_UI_PREFERENCES_VOLUME = "reminder_volume";
    public static final String APP_UI_PREFERENCES_QUICK_SMS = "quick_sms";
    public static final String APP_UI_PREFERENCES_FOLLOW_REMINDER = "follow_reminder";
    public static final String APP_UI_PREFERENCES_LAST_LIST = "last_list";
    public static final String APP_UI_PREFERENCES_WEARABLE = "wearable";
    public static final String APP_UI_PREFERENCES_TTS = "tts_enabled";
    public static final String APP_UI_PREFERENCES_TTS_LOCALE = "tts_locale";
    public static final String APP_UI_PREFERENCES_LAST_USED_REMINDER = "last_reminder";
    public static final String APP_UI_PREFERENCES_EXTENDED_BUTTON = "extended_button";
    public static final String APP_UI_PREFERENCES_ITEM_PREVIEW = "item_preview";
    public static final String APP_UI_PREFERENCES_THANKS_SHOWN = "thanks_shown";
    public static final String APP_UI_PREFERENCES_LAST_CALENDAR_VIEW = "last_calendar_view";

    // birthdays reminder notification constants
    public static final String APP_UI_PREFERENCES_USE_GLOBAL = "use_global";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_VIBRATION_STATUS = "birthday_vibration_status";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_SOUND_STATUS = "birthday_sound_status";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_WAKE_STATUS = "birthday_wake_status";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_INFINITE_SOUND = "birthday_infinite_sound";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_SILENT_SMS = "birthday_silent_sms";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_LED_STATUS = "birthday_led_status";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_LED_COLOR = "birthday_led_color";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_INFINITE_VIBRATION = "birthday_infinite_vibration";
    public final static String BIRTHDAY_CUSTOM_SOUND = "birthday_custom_sound";
    public final static String BIRTHDAY_CUSTOM_SOUND_FILE = "birthday_sound_file";

    public final static String BIRTHDAY_INTENT_ID = "birthday_intent_id";

    public static final String APP_UI_PREFERENCES_REMINDER_TYPE = "rem_type";
    public static final String APP_UI_PREFERENCES_REMINDER_TEXT = "rem_text";
    public static final String APP_UI_PREFERENCES_REMINDER_NUMBER = "rem_number";

    public static final String APP_UI_PREFERENCES_CONTACTS_IMPORT_DIALOG = "contacts_imported";

    public static final String APP_UI_PREFERENCES_RATE_SHOW = "show_rate";
    public static final String APP_UI_PREFERENCES_APP_RUNS_COUNT = "app_runs";

    public static final String APP_UI_PREFERENCES_BIRTHDAY_REMINDER_HOUR = "reminder_hour";
    public static final String APP_UI_PREFERENCES_BIRTHDAY_REMINDER_MINUTE = "reminder_minute";

    public static final String APP_UI_PREFERENCES_IS_CREATE_SHOWN = "create_showcase";
    public static final String APP_UI_PREFERENCES_IS_CALENDAR_SHOWN = "calendar_showcase";
    public static final String APP_UI_PREFERENCES_IS_LIST_SHOWN = "list_showcase";

    public static final String MAP_TYPE_NORMAL = "normal";
    public static final String MAP_TYPE_SATELLITE = "satellite";
    public static final String MAP_TYPE_HYBRID = "hybrid";
    public static final String MAP_TYPE_TERRAIN = "terrain";

    public static final String SCREEN_AUTO = "auto";
    public static final String SCREEN_PORTRAIT = "portrait";
    public static final String SCREEN_LANDSCAPE = "landscape";

    public static final String LOG_TAG = "JustRem";
    public static final String ITEM_ID_INTENT = "itemId";
    public static final String WINDOW_INTENT = "window";

    // Contact List Constants
    public final static int REQUEST_CODE_CONTACTS = 101;
    public final static int REQUEST_CODE_THEME = 105;
    public final static int REQUEST_CODE_FONT_STYLE = 106;
    public final static int REQUEST_CODE_SELECTED_MELODY = 115;
    public final static int REQUEST_CODE_SELECTED_RADIUS = 116;
    public final static int REQUEST_CODE_SELECTED_COLOR = 118;
    public final static int REQUEST_CODE_APPLICATION = 117;

    public final static String SELECTED_CONTACT_NUMBER = "selected_number";
    public final static String SELECTED_CONTACT_NAME = "selected_name";
    public final static String SELECTED_CONTACT_ARRAY = "contactNames";
    public final static String SELECTED_FONT_STYLE = "selected_style";
    public final static String SELECTED_MELODY = "selected_melody";
    public final static String SELECTED_RADIUS = "selected_radius";
    public final static String SELECTED_LED_COLOR = "selected_led_color";
    public final static String SELECTED_APPLICATION = "selected_application";

    public final static String CUSTOM_SOUND = "custom_sound";
    public final static String CUSTOM_SOUND_FILE = "sound_file";

    public final static String DIR_SD = "backup";
    public final static String DIR_IMAGE_CASHE = "img";
    public final static String DIR_NOTES_SD = "notes";
    public final static String DIR_GROUP_SD = "groups";
    public final static String DIR_MAIL_SD = "mail_attachments";
    public final static String DIR_SD_DBX_TMP = "tmp_dropbox";
    public final static String DIR_NOTES_SD_DBX_TMP = "tmp_dropbox_notes";
    public final static String DIR_GROUP_SD_DBX_TMP = "tmp_dropbox_groups";
    public final static String DIR_SD_GDRIVE_TMP = "tmp_gdrive";
    public final static String DIR_NOTES_SD_GDRIVE_TMP = "tmp_gdrive_notes";
    public final static String DIR_GROUP_SD_GDRIVE_TMP = "tmp_gdrive_group";

    public static final String FILE_NAME = ".json";
    public static final String FILE_NAME_NOTE = ".note";
    public static final String FILE_NAME_GROUP = ".rgroup";
    public static final String FILE_NAME_IMAGE = ".jpeg";

    public static final String EDIT_ID = "edit_id";
    public static final String EDIT_WIDGET = "edit_widget";

    public static final String DAY_CHECKED = "1";
    public static final String DAY_UNCHECKED = "0";
    public static final String NOTHING_CHECKED = "0000000";
    public static final String ALL_CHECKED = "1111111";

    // Reminder types
    public static final String TYPE_REMINDER = "reminder";
    public static final String TYPE_TIME = "time";
    public static final String TYPE_CALL = "call";
    public static final String TYPE_EMAIL = "e_mail";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_LOCATION_CALL = "location_call";
    public static final String TYPE_LOCATION_MESSAGE = "location_message";
    public static final String TYPE_WEEKDAY = "weekday";
    public static final String TYPE_WEEKDAY_CALL = "weekday_call";
    public static final String TYPE_WEEKDAY_MESSAGE = "weekday_message";
    public static final String TYPE_APPLICATION = "application";
    public static final String TYPE_APPLICATION_BROWSER = "application_browser";
    public static final String TYPE_SKYPE = "skype";
    public static final String TYPE_SKYPE_CHAT = "skype_chat";
    public static final String TYPE_SKYPE_VIDEO = "skype_video";

    //tech constants
    public static final String DEVICE_ID_KEY = "device_key";
    public static final String LICENSE_DIALOG_KEY = "dialog_key";
    public static final String LICENSE_KEY = "license_key";

    public static final int LOCAL_INT = 120;
    public static final int DROPBOX_INT = 121;
    public static final int GOOGLE_DRIVE_INT = 122;

    public static final int DIR_ID_EXTERNAL = 2;
    public static final int DIR_ID_DATA = 3;

    public static final String LANGUAGE_EN = "en-US";
    public static final String LANGUAGE_RU = "ru-RU";
    public static final String LANGUAGE_UK = "uk-UA";

    public static final String ORDER_DATE_A_Z = "date_az";
    public static final String ORDER_DATE_Z_A = "date_za";
    public static final String ORDER_COMPLETED_A_Z = "completed_az";
    public static final String ORDER_COMPLETED_Z_A = "completed_za";
    public static final String ORDER_DEFAULT = "default";
    public static final String ORDER_NAME_A_Z = "name_az";
    public static final String ORDER_NAME_Z_A = "name_za";
    public static final String ORDER_DATE_WITHOUT_DISABLED_A_Z = "date_az_without";
    public static final String ORDER_DATE_WITHOUT_DISABLED_Z_A = "date_za_without";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_IMAGE = "image";
    public static final String PATH_NOTIFICATION = "/ongoingnotification";
    public static final String PATH_DISMISS = "/dismissnotification";
    public static final int ACTION_REQUEST_GALLERY = 111;
    public static final int ACTION_REQUEST_CAMERA = 112;
    public static final String SELECTED_COLOR = "selected_color";

    public static final int SYNC_NO = 0;
    public static final int SYNC_GTASKS_ONLY = 1;
    public static final int SYNC_MS_EXCHANGE_ONLY = 10;
    public static final int SYNC_ALL = 11;

    public class ColorConstants{
        public static final int COLOR_WHITE= 0xffffffff;
        public static final int COLOR_RED = 0xffff0000;
        public static final int COLOR_GREEN = 0xff008000;
        public static final int COLOR_BLUE = 0xff0000ff;
        public static final int COLOR_ORANGE = 0xffffa500;
        public static final int COLOR_YELLOW = 0xffffff00;
        public static final int COLOR_PINK = 0xffffc0cb;
        public static final int COLOR_GREEN_LIGHT = 0xff00ff00;
        public static final int COLOR_BLUE_LIGHT = 0xff1e90ff;
        public static final int COLOR_PURPLE = 0xff9900FF;
    }

    public class ContactConstants{
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_CONTACT_ID = "contact_id";
        public static final String COLUMN_CONTACT_NAME = "display_name";
        public static final String COLUMN_CONTACT_NUMBER = "phone_number";
        public static final String COLUMN_CONTACT_MAIL = "e_mail";
        public static final String COLUMN_CONTACT_BIRTHDAY = "birthday";
        public static final String COLUMN_CONTACT_PHOTO_ID = "photo_id";
        public static final String COLUMN_CONTACT_DAY = "day";
        public static final String COLUMN_CONTACT_VAR = "var";
        public static final String COLUMN_CONTACT_MONTH = "month";
    }

    public class LocationConstants {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_LOCATION_NAME = "location_name";
        public static final String COLUMN_LOCATION_LATITUDE = "display_name";
        public static final String COLUMN_LOCATION_LONGITUDE = "phone_number";
        public static final String COLUMN_LOCATION_TECH = "tech";
        public static final String COLUMN_LOCATION_TECH1 = "tech1";
        public static final String COLUMN_LOCATION_TECH2 = "tech2";
        public static final String COLUMN_LOCATION_VAR = "var";
        public static final String COLUMN_LOCATION_VAR1 = "var1";
        public static final String COLUMN_LOCATION_VAR2 = "var2";
    }

    public class FilesConstants {
        public static final String FILE_TYPE_LOCAL = "type_local";
        public static final String FILE_TYPE_DROPBOX = "type_dropbox";
        public static final String FILE_TYPE_GDRIVE = "type_gdrive";

        public static final String COLUMN_FILE_NAME = "file_name";
        public static final String COLUMN_FILE_TYPE = "file_type";
        public static final String COLUMN_FILE_LOCATION = "file_location";
        public static final String COLUMN_FILE_LAST_EDIT = "file_last_edit";
    }
}
