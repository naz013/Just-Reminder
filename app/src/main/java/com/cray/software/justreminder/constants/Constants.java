package com.cray.software.justreminder.constants;

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
    public static final String COLUMN_VIBRATION = "vibration";
    public static final String COLUMN_AUTO_ACTION = "action_";
    public static final String COLUMN_WAKE_SCREEN = "awake_screen";
    public static final String COLUMN_UNLOCK_DEVICE = "unlock_device";
    public static final String COLUMN_NOTIFICATION_REPEAT = "notification_repeat";
    public static final String COLUMN_VOICE = "voice_notification";
    public static final String COLUMN_REPEAT_LIMIT = "column_extra";
    public static final String COLUMN_EXTRA_1 = "column_extra_1";
    public static final String COLUMN_EXTRA_2 = "column_extra_2";
    public static final String COLUMN_EXTRA_3 = "column_extra_3";
    public static final String COLUMN_EXTRA_4 = "column_extra_4";
    public static final String COLUMN_EXTRA_5 = "column_extra_5";

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

    public final static String BIRTHDAY_INTENT_ID = "birthday_intent_id";

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

    public final static String DIR_SD = "backup";
    public final static String DIR_IMAGE_CACHE = "img";
    public final static String DIR_PREFS = "preferences";
    public final static String DIR_NOTES_SD = "notes";
    public final static String DIR_GROUP_SD = "groups";
    public final static String DIR_BIRTHDAY_SD = "birthdays";
    public final static String DIR_MAIL_SD = "mail_attachments";
    public final static String DIR_SD_DBX_TMP = "tmp_dropbox";
    public final static String DIR_NOTES_SD_DBX_TMP = "tmp_dropbox_notes";
    public final static String DIR_GROUP_SD_DBX_TMP = "tmp_dropbox_groups";
    public final static String DIR_BIRTHDAY_SD_DBX_TMP = "tmp_dropbox_birthdays";
    public final static String DIR_SD_GDRIVE_TMP = "tmp_gdrive";
    public final static String DIR_NOTES_SD_GDRIVE_TMP = "tmp_gdrive_notes";
    public final static String DIR_GROUP_SD_GDRIVE_TMP = "tmp_gdrive_group";
    public final static String DIR_BIRTHDAY_SD_GDRIVE_TMP = "tmp_gdrive_birthdays";
    public final static String DIR_SD_BOX_TMP = "tmp_box";
    public final static String DIR_NOTES_SD_BOX_TMP = "tmp_box_notes";
    public final static String DIR_GROUP_SD_BOX_TMP = "tmp_box_group";
    public final static String DIR_BIRTHDAY_SD_BOX_TMP = "tmp_box_birthdays";

    public static final String FILE_NAME_REMINDER = ".json";
    public static final String FILE_NAME_NOTE = ".note";
    public static final String FILE_NAME_GROUP = ".rgroup";
    public static final String FILE_NAME_BIRTHDAY = ".rbd";
    public static final String FILE_NAME_IMAGE = ".jpeg";

    public static final String EDIT_ID = "edit_id";
    public static final String EDIT_PATH = "edit_path";
    public static final String EDIT_WIDGET = "edit_widget";

    public static final int DAY_CHECKED = 1;
    public static final String DAY_CHECK = "1";
    public static final int DAY_UNCHECKED = 0;
    public static final String NOTHING_CHECKED = "0000000";
    public static final String ALL_CHECKED = "1111111";

    // Reminder types
    public static final String TYPE_REMINDER = "reminder";
    public static final String TYPE_MONTHDAY = "day_of_month";
    public static final String TYPE_MONTHDAY_LAST = "day_of_month_last";
    public static final String TYPE_MONTHDAY_CALL = "day_of_month_call";
    public static final String TYPE_MONTHDAY_CALL_LAST = "day_of_month_call_last";
    public static final String TYPE_MONTHDAY_MESSAGE = "day_of_month_message";
    public static final String TYPE_MONTHDAY_MESSAGE_LAST = "day_of_month_message_last";
    public static final String TYPE_TIME = "time";
    public static final String TYPE_CALL = "call";
    public static final String TYPE_MAIL = "e_mail";
    public static final String TYPE_SHOPPING_LIST = "shopping_list";
    public static final String TYPE_MESSAGE = "message";
    public static final String TYPE_LOCATION = "location";
    public static final String TYPE_LOCATION_OUT = "out_location";
    public static final String TYPE_LOCATION_OUT_CALL = "out_location_call";
    public static final String TYPE_LOCATION_OUT_MESSAGE = "out_location_message";
    public static final String TYPE_LOCATION_CHAIN = "chain_location";
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

    public static final int ACTION_REQUEST_GALLERY = 111;
    public static final int ACTION_REQUEST_CAMERA = 112;
    public static final String SELECTED_COLOR = "selected_color";

    public static final int SYNC_NO = 0;
    public static final int SYNC_GTASKS_ONLY = 1;
    public static final String FILE_PICKED = "file_picked";
    
    public static final String NONE = "none";
    public static final String DEFAULT = "defaut";

    public class ContactConstants{
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_CONTACT_ID = "contact_id";
        public static final String COLUMN_CONTACT_NAME = "display_name";
        public static final String COLUMN_CONTACT_NUMBER = "phone_number";
        public static final String COLUMN_CONTACT_MAIL = "e_mail";
        public static final String COLUMN_CONTACT_BIRTHDAY = "birthday";
        public static final String COLUMN_CONTACT_UUID = "photo_id";
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
