package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.CalendarData;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarEventsAdapter extends BaseAdapter{

    Contacts contacts;
    LayoutInflater inflater;
    ArrayList<CalendarData> mDatas;
    Context mContext;
    ColorSetter cs;
    Typeface typeface;
    SharedPrefs prefs;

    @SuppressWarnings("deprecation")
    public CalendarEventsAdapter(Context context, ArrayList<CalendarData> datas) {
        this.mContext = context;
        this.mDatas = new ArrayList<>();
        this.mDatas = datas;
        inflater = LayoutInflater.from(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contacts = new Contacts(context);
        cs = new ColorSetter(context);
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        prefs = new SharedPrefs(context);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position).getType();
    }

    @Override
    public long getItemId(int position) {
        return mDatas.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String type = mDatas.get(position).getType();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_events, null);
        }

        CardView card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        TextView eventColor = (TextView) convertView.findViewById(R.id.eventColor);
        TextView eventType = (TextView) convertView.findViewById(R.id.eventType);
        eventType.setTypeface(typeface);
        TextView eventDate = (TextView) convertView.findViewById(R.id.eventDate);
        eventDate.setTypeface(typeface);
        TextView eventText = (TextView) convertView.findViewById(R.id.eventText);
        eventText.setTypeface(typeface);
        TextView eventNumber = (TextView) convertView.findViewById(R.id.eventNumber);
        eventNumber.setTypeface(typeface);
        boolean is24 = prefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT);

        if (type.matches("birthday")) {
            eventColor.setBackgroundColor(mContext.getResources().getColor(cs.colorBirthdayCalendar()));
            eventType.setText(mContext.getString(R.string.birthday_text));
            String title = mDatas.get(position).getName();
            String phone = mDatas.get(position).getNumber();
            if (phone == null || phone.matches("")) phone = contacts.get_Number(title, mContext);
            if (phone != null && !phone.matches("")){
                eventNumber.setText(phone);
            } else {
                eventNumber.setText("");
            }
            eventText.setText(title);

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(mDatas.get(position).getDate());
            Date time = cl.getTime();
            eventDate.setText(Utils.getDateTime(time, is24));
        } else {
            eventColor.setBackgroundColor(mContext.getResources().getColor(cs.colorReminderCalendar()));
            eventType.setText(mContext.getString(R.string.reminder_type));

            String number = mDatas.get(position).getNumber();

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(mDatas.get(position).getDate());

            eventText.setText(mDatas.get(position).getName());

            if (!number.matches("0")) {
                String contactName = contacts.getContactNameFromNumber(number, mContext);
                eventNumber.setText(number + "\n" + contactName);
            } else {
                eventNumber.setText("");
            }

            Date time = cl.getTime();
            eventDate.setText(Utils.getDateTime(time, is24));
        }

        return convertView;
    }
}