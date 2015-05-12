package com.cray.software.justreminder.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Constants;

import java.util.ArrayList;
import java.util.TimeZone;

public class CalendarManager {

    Context ctx;
    SharedPrefs sPrefs;

    public CalendarManager(Context context){
        this.ctx = context;
        sPrefs = new SharedPrefs(context);
    }

    public void addEvent(String summary, long startTime, long id){
        sPrefs = new SharedPrefs(ctx);
        String m_selectedCalendarId = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_CALENDAR_ID);
        int i = 0;
        if (m_selectedCalendarId != null){
            try{
                i = Integer.parseInt(m_selectedCalendarId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            TimeZone tz = TimeZone.getDefault();
            String timeZone = tz.getDisplayName();
            ContentResolver cr = ctx.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(CalendarContract.Events.DTSTART, startTime);
            values.put(CalendarContract.Events.DTEND, startTime +
                    (60 * 1000 * sPrefs.loadInt(Constants.APP_UI_PREFERENCES_EVENT_DURATION)));
            if (summary != null) {
                values.put(CalendarContract.Events.TITLE, summary);
            }
            values.put(CalendarContract.Events.CALENDAR_ID, i);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);
            values.put(CalendarContract.Events.ALL_DAY, 0);
            values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
            values.put(CalendarContract.Events.DESCRIPTION, ctx.getString(R.string.event_description));
            Uri l_eventUri;
            if (Build.VERSION.SDK_INT >= 8) {
                l_eventUri = Uri.parse("content://com.android.calendar/events");
            } else {
                l_eventUri = Uri.parse("content://calendar/events");
            }
            Uri event;
            try {
                event = cr.insert(l_eventUri, values);
                DataBase db = new DataBase(ctx);
                db.open();
                if (event != null) {
                    long eventID = Long.parseLong(event.getLastPathSegment());
                    db.addCalendarEvent(event.toString(), id, eventID);
                }
            } catch (Exception e) {
                Toast.makeText(ctx, ctx.getString(R.string.no_google_calendars_found), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void deleteEvents(long id){
        DataBase db = new DataBase(ctx);
        db.open();
        Cursor c = db.getCalendarEvents(id);
        if (c != null && c.moveToFirst()){
            do {
                long mID = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                long eventID = c.getLong(c.getColumnIndex(Constants.COLUMN_EVENT_ID));
                String uri = c.getString(c.getColumnIndex(Constants.COLUMN_DELETE_URI));
                Uri l_eventUri = CalendarContract.Calendars.CONTENT_URI;
                Uri eventUri = Uri.parse(uri);
                ContentResolver cr = ctx.getContentResolver();
                cr.delete(eventUri, null, null);
                db.deleteCalendarEvent(mID);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
    }

    public void addEventToStock(String summary, long startTime){
        sPrefs = new SharedPrefs(ctx);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime +
                        (60 * 1000 * sPrefs.loadInt(Constants.APP_UI_PREFERENCES_EVENT_DURATION)))
                .putExtra(CalendarContract.Events.TITLE, summary)
                .putExtra(CalendarContract.Events.DESCRIPTION, ctx.getString(R.string.event_description));
        ctx.startActivity(intent);
    }

    public ArrayList<String> getCalendars() {
        ArrayList<String> ids = new ArrayList<>();
        ids.clear();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] mProjection = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(uri, mProjection, null, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
        if (c != null && c.moveToFirst()) {
            String mID;
            do {
                mID = c.getString(c.getColumnIndex(mProjection[0]));
                ids.add(mID);
            } while (c.moveToNext());
        }
        if (c != null) c.close();

        if (ids.size() == 0) return null;
        else return ids;
    }

    public String getCalendarName(String id) {
        String mName = null;
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] mProjection = new String[] {
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };
        Cursor c = null;
        try {
            c = ctx.getContentResolver().query(uri, mProjection, CalendarContract.Calendars._ID + "=" + id, null, null);
        } catch (Exception e){
            e.printStackTrace();
        }
        if (c != null && c.moveToFirst()) {
            do {
                mName = c.getString(c.getColumnIndex(mProjection[2]));
            } while (c.moveToNext());
        }
        if (c != null) c.close();

        return mName;
    }
}