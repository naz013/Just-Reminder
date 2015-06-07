package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.Interval;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CustomCursorAdapter extends CursorAdapter implements Filterable {

    LayoutInflater inflater;
    private Cursor c;
    Context cContext;
    DataBase DB;
    TimeCount mCount;
    Contacts mContacts;
    Interval mInterval;
    CardView card;
    SyncListener mListener;
    long mId;
    ArrayList<Long> arrayList;

    @SuppressWarnings("deprecation")
    public CustomCursorAdapter(Context context, Cursor c, SyncListener clickListener) {
        super(context, c);
        this.cContext = context;
        inflater = LayoutInflater.from(context);
        this.c = c;
        this.mListener = clickListener;
        arrayList = new ArrayList<>();
        c.moveToFirst();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.list_item_card, null);
    }

    @Override
    public long getItemId(int position) {
        Cursor cursor = getCursor();
        cursor.moveToPosition(position);
        return cursor.getLong(cursor.getColumnIndex("_id"));
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    class ViewHolder {
        TextView leftTime;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        c.moveToPosition(position);
        DB = new DataBase(cContext);
        mContacts = new Contacts(cContext);
        mInterval = new Interval(cContext);
        Typeface typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Light.ttf");
        DB.open();
        String title = c.getString(c.getColumnIndex(Constants.COLUMN_TEXT));
        String type = c.getString(c.getColumnIndex(Constants.COLUMN_TYPE));
        String number = c.getString(c.getColumnIndex(Constants.COLUMN_NUMBER));
        String weekdays = c.getString(c.getColumnIndex(Constants.COLUMN_WEEKDAYS));
        String categoryId = c.getString(c.getColumnIndex(Constants.COLUMN_CATEGORY));
        int hour = c.getInt(c.getColumnIndex(Constants.COLUMN_HOUR));
        int minute = c.getInt(c.getColumnIndex(Constants.COLUMN_MINUTE));
        int seconds = c.getInt(c.getColumnIndex(Constants.COLUMN_SECONDS));
        int day = c.getInt(c.getColumnIndex(Constants.COLUMN_DAY));
        int month = c.getInt(c.getColumnIndex(Constants.COLUMN_MONTH));
        int year = c.getInt(c.getColumnIndex(Constants.COLUMN_YEAR));
        int repCode = c.getInt(c.getColumnIndex(Constants.COLUMN_REPEAT));
        int repTime = c.getInt(c.getColumnIndex(Constants.COLUMN_REMIND_TIME));
        int isDone = c.getInt(c.getColumnIndex(Constants.COLUMN_IS_DONE));
        int archived = c.getInt(c.getColumnIndex(Constants.COLUMN_ARCHIVED));
        double lat = c.getDouble(c.getColumnIndex(Constants.COLUMN_LATITUDE));
        double lon = c.getDouble(c.getColumnIndex(Constants.COLUMN_LONGITUDE));
        int repCount = c.getInt(c.getColumnIndex(Constants.COLUMN_REMINDERS_COUNT));
        int delay = c.getInt(c.getColumnIndex(Constants.COLUMN_DELAY));
        mId = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
        arrayList.add(mId);

        Cursor cf = DB.getCategory(categoryId);
        int categoryColor = 0;
        if (cf != null && cf.moveToFirst()) {
            categoryColor = cf.getInt(cf.getColumnIndex(Constants.COLUMN_COLOR));
        }
        if (cf != null) cf.close();

        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_card, null);

            holder.leftTime = (TextView) convertView.findViewById(R.id.remainingTime);
            holder.leftTime.setTypeface(typeface);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CheckBox check = (CheckBox) convertView.findViewById(R.id.itemCheck);
        check.setFocusable(false);
        check.setVisibility(View.VISIBLE);
        ImageView taskIcon = (ImageView) convertView.findViewById(R.id.taskIcon);
        TextView taskTitle = (TextView) convertView.findViewById(R.id.taskText);
        taskTitle.setTypeface(typeface);
        taskTitle.setText("");
        TextView taskDate = (TextView) convertView.findViewById(R.id.taskDate);
        taskDate.setTypeface(typeface);
        taskDate.setText("");
        TextView viewTime = (TextView) convertView.findViewById(R.id.taskTime);
        viewTime.setTypeface(typeface);
        viewTime.setText("");
        TextView reminder_type = (TextView) convertView.findViewById(R.id.reminder_type);
        reminder_type.setTypeface(typeface);
        reminder_type.setText("");
        TextView reminder_phone = (TextView) convertView.findViewById(R.id.reminder_phone);
        reminder_phone.setTypeface(typeface);
        reminder_phone.setText("");
        TextView repeatInterval = (TextView) convertView.findViewById(R.id.repeatInterval);
        repeatInterval.setTypeface(typeface);
        repeatInterval.setText("");
        TextView reminder_contact_name = (TextView) convertView.findViewById(R.id.reminder_contact_name);
        reminder_contact_name.setTypeface(typeface);
        reminder_contact_name.setText("");
        ImageView leftTimeIcon = (ImageView) convertView.findViewById(R.id.leftTime);
        leftTimeIcon.setVisibility(View.VISIBLE);

        ColorSetter cs = new ColorSetter(cContext);

        card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        if (isDone == 1){
            check.setChecked(true);
        } else {
            check.setChecked(false);
        }

        mCount = new TimeCount(cContext);

        if (type.startsWith(Constants.TYPE_MONTHDAY)){
            taskTitle.setText(title);

            taskIcon.setImageDrawable(cContext.getResources().getDrawable(cs.getCategoryIndicator(categoryColor)));

            if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_make_call));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_send_message));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_MONTHDAY) ||
                    type.matches(Constants.TYPE_MONTHDAY_LAST)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            }

            long time = mCount.getNextMonthDayTime(hour, minute, day, delay);

            leftTimeIcon.setImageDrawable(mCount.
                    getDifference(time));
            repeatInterval.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String formattedTime;
            if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            calendar.setTimeInMillis(time);
            String date = dateFormat.format(calendar.getTime());

            taskDate.setText(date);
            if (isDone == 0) {
                String remaining = mCount.getRemaining(time);
                holder.leftTime.setText(remaining);
            } else {
                holder.leftTime.setVisibility(View.GONE);
            }
            viewTime.setText(formattedTime);

            DB.close();
        } else if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_make_call));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_REMINDER) || type.matches(Constants.TYPE_TIME)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_LOCATION)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_send_message));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                reminder_phone.setText(number);
                if (type.matches(Constants.TYPE_SKYPE)){
                    reminder_type.setText(cContext.getString(R.string.skype_call_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
                    reminder_type.setText(cContext.getString(R.string.skype_video_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
                    reminder_type.setText(cContext.getString(R.string.skype_chat_type_title));
                }
                reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)){
                PackageManager packageManager = cContext.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String name = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_type_application));
                reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_type_open_link));
                reminder_contact_name.setText(number);
            }

            taskIcon.setImageDrawable(cContext.getResources().getDrawable(cs.getCategoryIndicator(categoryColor)));

            long time = mCount.getEventTime(year, month, day, hour, minute, seconds, repTime,
                    repCode, repCount, delay);

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                    type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                    type.startsWith(Constants.TYPE_APPLICATION)) {
                leftTimeIcon.setImageDrawable(mCount.
                        getDifference(time));
                repeatInterval.setText(mInterval.getInterval(repCode));
            } else if (type.matches(Constants.TYPE_TIME)) {
                leftTimeIcon.setImageDrawable(mCount.
                        getDifference(time));
                repeatInterval.setText(mInterval.getTimeInterval(repCode));
            } else {
                if (type.startsWith(Constants.TYPE_LOCATION)){
                    leftTimeIcon.setVisibility(View.GONE);
                    repeatInterval.setVisibility(View.GONE);
                } else {
                    leftTimeIcon.setVisibility(View.GONE);
                    repeatInterval.setText(cContext.getString(R.string.interval_zero));
                }
            }

            taskTitle.setText(title);

            String[] dT = mCount.
                    getNextDateTime(time);
            if (lat != 0.0 || lon != 0.0) {
                taskDate.setText(String.format("%.5f", lat));
                viewTime.setText(String.format("%.5f", lon));
                holder.leftTime.setVisibility(View.GONE);
            } else {
                if (isDone == 0) {
                    holder.leftTime.setText(mCount.
                            getRemaining(time));
                } else {
                    holder.leftTime.setVisibility(View.GONE);
                }

                taskDate.setText(dT[0]);
                viewTime.setText(dT[1]);
            }
            DB.close();
        } else {
            taskTitle.setText(title);

            taskIcon.setImageDrawable(cContext.getResources().getDrawable(cs.getCategoryIndicator(categoryColor)));

            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_make_call));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                reminder_phone.setText(number);
                reminder_type.setText(cContext.getString(R.string.reminder_send_message));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) reminder_contact_name.setText(name);
                else reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY)) {
                reminder_type.setText(cContext.getString(R.string.reminder_type));
            }

            long time = mCount.getNextWeekdayTime(hour, minute, weekdays, delay);

            leftTimeIcon.setImageDrawable(mCount.
                    getDifference(time));
            repeatInterval.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            String formattedTime;
            if (new SharedPrefs(mContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                formattedTime = sdf.format(calendar.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("K:mm a");
                formattedTime = sdf.format(calendar.getTime());
            }

            if (weekdays.length() == 7) {
                taskDate.setText(getRepeatString(weekdays));
                if (isDone == 0) {
                    String remaining = mCount.getRemaining(time);
                    holder.leftTime.setText(remaining);
                } else {
                    holder.leftTime.setVisibility(View.GONE);
                }
            }
            viewTime.setText(formattedTime);

            DB.close();
        }
        if (isDone == 1){
            leftTimeIcon.setImageDrawable(cContext.getResources().getDrawable(R.drawable.drawable_grey));
        }

        if (archived > 0) {
            check.setVisibility(View.GONE);
            holder.leftTime.setVisibility(View.GONE);
            leftTimeIcon.setVisibility(View.GONE);
        }
        return convertView;
    }

    private String getRepeatString(String repCode){
        String res;
        StringBuilder sb = new StringBuilder();
        if (Character.toString(repCode.charAt(0)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_monday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(1)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_tuesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(2)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_wednesday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(3)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_thursday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(4)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_friday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(5)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_saturday));
            sb.append(",");
        }
        if (Character.toString(repCode.charAt(6)).matches(Constants.DAY_CHECKED)){
            sb.append(cContext.getString(R.string.weekday_sunday));
        }
        if (repCode.matches(Constants.ALL_CHECKED)){
            res = cContext.getString(R.string.interval_day);
        } else res = sb.toString();
        return res;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}