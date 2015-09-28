package com.cray.software.justreminder.dialogs.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.CalendarManager;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.EventsCheckAlarm;
import com.cray.software.justreminder.widgets.UpdatesHelper;

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

    private ArrayList<CalendarManager.CalendarItem> list;

    public static final String EVENT_KEY = "Events";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(EventsImport.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.activity_events_import);

        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.settings_events_import));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        TextView button = (TextView) findViewById(R.id.button);
        button.setOnClickListener(this);

        eventsCheck = (CheckBox) findViewById(R.id.eventsCheck);
        CheckBox autoCheck = (CheckBox) findViewById(R.id.autoCheck);
        eventsCheck.setOnCheckedChangeListener(this);
        autoCheck.setOnCheckedChangeListener(this);
        autoCheck.setChecked(prefs.loadBoolean(Prefs.AUTO_CHECK_FOR_EVENTS));

        eventCalendar = (Spinner) findViewById(R.id.eventCalendar);
        loadCalendars();
    }

    private void loadCalendars() {
        list = new ArrayList<>();
        list = new CalendarManager(this).getCalendarsList();

        if (list == null || list.size() == 0){
            showMessage(getString(R.string.no_google_calendars_found));
            finish();
        }

        ArrayList<String> spinnerArray = new ArrayList<>();
        spinnerArray.add(getString(R.string.select_calendar_settings_title));
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
                importEvents();
                break;
        }
    }

    private void importEvents() {
        if (!eventsCheck.isChecked()) {
            showMessage(getString(R.string.string_no_action_selected));
            return;
        }

        if (eventCalendar.getSelectedItemPosition() == 0){
            showMessage(getString(R.string.string_no_calendar_selected));
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

    private void showMessage(String text){
        Toast.makeText(EventsImport.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.eventsCheck:
                if (isChecked) eventCalendar.setEnabled(true);
                else eventCalendar.setEnabled(false);
                break;
            case R.id.autoCheck:
                autoCheck(isChecked);
                break;
        }
    }

    private void autoCheck(boolean isChecked) {
        if (isChecked) prefs.saveBoolean(Prefs.AUTO_CHECK_FOR_EVENTS, true);
        else prefs.saveBoolean(Prefs.AUTO_CHECK_FOR_EVENTS, false);
        EventsCheckAlarm alarm = new EventsCheckAlarm();
        if (isChecked) alarm.setAlarm(this);
        else alarm.cancelAlarm(this);
    }

    public class Import extends AsyncTask<HashMap<String, String>, Void, Integer>{

        private Context context;

        public Import(Context context){
            this.context = context;
        }

        @SafeVarargs
        @Override
        protected final Integer doInBackground(HashMap<String, String>... params) {
            if (params == null){
                return 0;
            }
            CalendarManager cm = new CalendarManager(context);

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
                            eventsCount += 1;
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
                            String text = item.getTitle();
                            String type = Constants.TYPE_REMINDER;

                            String uuID = SyncHelper.generateID();
                            Cursor cf = DB.queryCategories();
                            String categoryId = null;
                            if (cf != null && cf.moveToFirst()) {
                                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                            }
                            if (cf != null) cf.close();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(item.getDtStart());
                            int day = calendar.get(Calendar.DAY_OF_MONTH);
                            int month = calendar.get(Calendar.MONTH);
                            int year = calendar.get(Calendar.YEAR);
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            int minute = calendar.get(Calendar.MINUTE);

                            long id = DB.insertReminder(text, type, day, month, year, hour,
                                    minute, 0, null, repeat, 0, 0, 0, 0, uuID, null, 1, null, 0, 0,
                                    0, categoryId);
                            DB.updateReminderDateTime(id);
                            DB.addCalendarEvent(null, id, item.getId());
                            new AlarmReceiver().setAlarm(context, id);
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
            if (result == 0) showMessage(getString(R.string.string_no_events_found));

            if (result > 0) {
                showMessage(getString(R.string.simple_imported) + " " + result + " " +
                        getString(R.string.simple_event) +
                        (result == 1 ? "." : getString(R.string.char_s_with_point)));
                new UpdatesHelper(context).updateWidget();
                new Notifier(context).recreatePermanent();
                finish();
            }
        }
    }
}