package com.cray.software.justreminder.activities;

import android.app.AlarmManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.json.JsonModel;
import com.cray.software.justreminder.json.JsonRecurrence;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.services.EventsCheckAlarm;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import static org.dmfs.rfc5545.recur.RecurrenceRule.Freq;

public class EventsImport extends AppCompatActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private SharedPrefs prefs = new SharedPrefs(EventsImport.this);

    private CheckBox eventsCheck;
    private Spinner eventCalendar;
    private Button syncInterval;

    private ArrayList<CalendarManager.CalendarItem> list;

    public static final String EVENT_KEY = "Events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(EventsImport.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_events_import);

        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.import_events));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        TextView button = (TextView) findViewById(R.id.button);
        button.setOnClickListener(this);

        syncInterval = (Button) findViewById(R.id.syncInterval);
        syncInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogues.selectInterval(EventsImport.this, Prefs.AUTO_CHECK_FOR_EVENTS_INTERVAL, R.string.interval);
            }
        });

        eventsCheck = (CheckBox) findViewById(R.id.eventsCheck);
        CheckBox autoCheck = (CheckBox) findViewById(R.id.autoCheck);
        eventsCheck.setOnCheckedChangeListener(this);
        autoCheck.setOnCheckedChangeListener(this);
        autoCheck.setChecked(prefs.loadBoolean(Prefs.AUTO_CHECK_FOR_EVENTS));

        if (autoCheck.isChecked()) syncInterval.setEnabled(true);
        else syncInterval.setEnabled(false);

        eventCalendar = (Spinner) findViewById(R.id.eventCalendar);
        loadCalendars();
    }

    private void loadCalendars() {
        list = new CalendarManager(this).getCalendarsList();

        if (list == null || list.size() == 0){
            Messages.toast(EventsImport.this, getString(R.string.no_calendars_found));
            finish();
        }

        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getString(R.string.choose_calendar));
        if (list != null && list.size() > 0) {
            for (CalendarManager.CalendarItem item : list) {
                spinnerArray.add(item.getName());
            }
        }

        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        eventCalendar.setAdapter(spinnerArrayAdapter);

        eventCalendar.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                if (Permissions.checkPermission(EventsImport.this, Permissions.READ_CALENDAR,
                        Permissions.WRITE_CALENDAR)) {
                    importEvents();
                } else {
                    Permissions.requestPermission(EventsImport.this, 102,
                            Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR);
                }
                break;
        }
    }

    private void importEvents() {
        if (!eventsCheck.isChecked()) {
            Messages.toast(EventsImport.this, getString(R.string.no_action_selected));
            return;
        }

        if (eventCalendar.getSelectedItemPosition() == 0){
            Messages.toast(EventsImport.this, getString(R.string.you_dont_select_any_calendar));
            return;
        }

        HashMap<String, String> map = new HashMap<>();

        if (eventsCheck.isChecked()) {
            int selectedPosition = eventCalendar.getSelectedItemPosition() - 1;
            map.put(EVENT_KEY, list.get(selectedPosition).getId());
            boolean isEnabled = prefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
            if (!isEnabled) {
                prefs.saveBoolean(Prefs.EXPORT_TO_CALENDAR, true);
                prefs.savePrefs(Prefs.CALENDAR_NAME, list.get(selectedPosition).getName());
                prefs.savePrefs(Prefs.CALENDAR_ID, list.get(selectedPosition).getId());
            }
            prefs.savePrefs(Prefs.EVENTS_CALENDAR, list.get(selectedPosition).getId());
        }

        new Import(this).execute(map);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.eventsCheck:
                if (isChecked) eventCalendar.setEnabled(true);
                else eventCalendar.setEnabled(false);
                break;
            case R.id.autoCheck:
                if (isChecked) {
                    if (Permissions.checkPermission(EventsImport.this, Permissions.READ_CALENDAR,
                            Permissions.WRITE_CALENDAR)) {
                        autoCheck(true);
                    } else {
                        Permissions.requestPermission(EventsImport.this, 101,
                                Permissions.READ_CALENDAR, Permissions.WRITE_CALENDAR);
                    }
                } else autoCheck(false);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    autoCheck(true);
                } else {
                    Permissions.showInfo(EventsImport.this, Permissions.READ_CALENDAR);
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    importEvents();
                } else {
                    Permissions.showInfo(EventsImport.this, Permissions.READ_CALENDAR);
                }
                break;
        }
    }

    private void autoCheck(boolean isChecked) {
        prefs.saveBoolean(Prefs.AUTO_CHECK_FOR_EVENTS, isChecked);
        syncInterval.setEnabled(isChecked);
        EventsCheckAlarm alarm = new EventsCheckAlarm();
        if (isChecked) alarm.setAlarm(this);
        else alarm.cancelAlarm(this);
    }

    public class Import extends AsyncTask<HashMap<String, String>, Void, Integer>{

        private Context context;
        private ProgressDialog dialog;

        public Import(Context context){
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, null, getString(R.string.please_wait), true, false);
        }

        @SafeVarargs
        @Override
        protected final Integer doInBackground(HashMap<String, String>... params) {
            if (params == null){
                return 0;
            }
            CalendarManager cm = new CalendarManager(context);
            long currTime = System.currentTimeMillis();

            int eventsCount = 0;
            HashMap <String, String> map = params[0];
            if (map.containsKey(EVENT_KEY)){
                ArrayList<CalendarManager.EventItem> eventItems = cm.getEvents(map.get(EVENT_KEY));
                if (eventItems != null && eventItems.size() > 0){
                    DataBase DB = new DataBase(context);
                    DB.open();
                    Cursor c = DB.getCalendarEvents();
                    ArrayList<Long> ids = new ArrayList<>();
                    if (c != null && c.moveToFirst()){
                        do {
                            long eventId = c.getLong(c.getColumnIndex(Constants.COLUMN_EVENT_ID));
                            ids.add(eventId);
                        } while (c.moveToNext());
                    }
                    for (CalendarManager.EventItem item : eventItems){
                        long itemId = item.getId();
                        if (!ids.contains(itemId)) {
                            String rrule = item.getRrule();
                            int repeat = 0;
                            if (rrule != null && !rrule.matches("")) {
                                try {
                                    RecurrenceRule rule = new RecurrenceRule(rrule);
                                    int interval = rule.getInterval();
                                    Freq freq = rule.getFreq();
                                    if (freq == Freq.HOURLY || freq == Freq.MINUTELY || freq == Freq.SECONDLY) {
                                    } else {
                                        if (freq == Freq.WEEKLY) repeat = interval * 7;
                                        else if (freq == Freq.MONTHLY) repeat = interval * 30;
                                        else if (freq == Freq.YEARLY) repeat = interval * 365;
                                        else repeat = interval;
                                    }
                                } catch (InvalidRecurrenceRuleException e) {
                                    e.printStackTrace();
                                }
                            }
                            String summary = item.getTitle();

                            String uuID = SyncHelper.generateID();
                            Cursor cf = DB.queryCategories();
                            String categoryId = null;
                            if (cf != null && cf.moveToFirst()) {
                                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                            }
                            if (cf != null) cf.close();

                            Calendar calendar = Calendar.getInstance();
                            long dtStart = item.getDtStart();
                            calendar.setTimeInMillis(dtStart);
                            if (dtStart >= currTime){
                                eventsCount += 1;
                                JsonRecurrence jsonRecurrence = new JsonRecurrence(0, repeat, -1, null, 0);
                                JsonModel jsonModel = new JsonModel(summary, Constants.TYPE_REMINDER, categoryId, uuID, dtStart,
                                        dtStart, jsonRecurrence, null, null);
                                long id = new DateType(context, Constants.TYPE_REMINDER).save(jsonModel);
                                DB.addCalendarEvent(null, id, item.getId());
                            } else {
                                if (repeat > 0) {
                                    do {
                                        calendar.setTimeInMillis(dtStart + (repeat * AlarmManager.INTERVAL_DAY));
                                        dtStart = calendar.getTimeInMillis();
                                    } while (dtStart < currTime);
                                    eventsCount += 1;
                                    JsonRecurrence jsonRecurrence = new JsonRecurrence(0, repeat, -1, null, 0);
                                    JsonModel jsonModel = new JsonModel(summary, Constants.TYPE_REMINDER, categoryId, uuID, dtStart,
                                            dtStart, jsonRecurrence, null, null);
                                    long id = new DateType(context, Constants.TYPE_REMINDER).save(jsonModel);
                                    DB.addCalendarEvent(null, id, item.getId());
                                }
                            }
                        }
                    }
                    DB.close();
                }
            }
            return eventsCount;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (dialog != null && dialog.isShowing()) dialog.dismiss();

            if (result == 0) Messages.toast(EventsImport.this, getString(R.string.no_events_found));

            if (result > 0) {
                Messages.toast(EventsImport.this, result + " " + getString(R.string.events_found));
                new UpdatesHelper(context).updateWidget();
                new Notifier(context).recreatePermanent();
                finish();
            }
        }
    }
}