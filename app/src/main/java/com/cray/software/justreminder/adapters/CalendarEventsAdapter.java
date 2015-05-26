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
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.datas.CalendarData;
import com.cray.software.justreminder.interfaces.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CalendarEventsAdapter extends BaseAdapter{

    TextView eventColor, eventType, eventDate, eventText, eventNumber;
    Contacts contacts;
    LayoutInflater inflater;
    ArrayList<CalendarData> mDatas = new ArrayList<>();
    Context cContext;

    @SuppressWarnings("deprecation")
    public CalendarEventsAdapter(Context context, ArrayList<CalendarData> datas) {
        this.cContext = context;
        this.mDatas = datas;
        inflater = LayoutInflater.from(context);
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
        contacts = new Contacts(cContext);
        String type = mDatas.get(position).getType();
        if (convertView == null) {
            inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_events, null);
        }

        ColorSetter cs = new ColorSetter(cContext);
        CardView card = (CardView) convertView.findViewById(R.id.card);
        card.setCardBackgroundColor(cs.getCardStyle());

        Typeface typeface = Typeface.createFromAsset(cContext.getAssets(), "fonts/Roboto-Light.ttf");

        eventColor = (TextView) convertView.findViewById(R.id.eventColor);
        eventType = (TextView) convertView.findViewById(R.id.eventType);
        eventType.setTypeface(typeface);
        eventDate = (TextView) convertView.findViewById(R.id.eventDate);
        eventDate.setTypeface(typeface);
        eventText = (TextView) convertView.findViewById(R.id.eventText);
        eventText.setTypeface(typeface);
        eventNumber = (TextView) convertView.findViewById(R.id.eventNumber);
        eventNumber.setTypeface(typeface);

        if (type.matches("birthday")) {
            eventColor.setBackgroundColor(cContext.getResources().getColor(cs.colorBirthdayCalendar()));
            eventType.setText(cContext.getString(R.string.birthday_text));
            String title = mDatas.get(position).getName();
            String phone = mDatas.get(position).getNumber();
            if (phone == null || phone.matches("")) phone = contacts.get_Number(title, cContext);
            if (phone != null && !phone.matches("")){
                eventNumber.setText(phone);
            } else {
                eventNumber.setText("");
            }
            eventText.setText(title);

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(mDatas.get(position).getDate());

            String formattedTime;
            if (new SharedPrefs(cContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
                formattedTime = sdf.format(cl.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, K:mm a");
                formattedTime = sdf.format(cl.getTime());
            }
            eventDate.setText(formattedTime);
        } else {
            eventColor.setBackgroundColor(cContext.getResources().getColor(cs.colorReminderCalendar()));
            eventType.setText(cContext.getString(R.string.reminder_type));

            String number = mDatas.get(position).getNumber();

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(mDatas.get(position).getDate());

            String formattedTime;
            if (new SharedPrefs(cContext).loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT)){
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
                formattedTime = sdf.format(cl.getTime());
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, K:mm a");
                formattedTime = sdf.format(cl.getTime());
            }

            eventText.setText(mDatas.get(position).getName());

            if (!number.matches("0")) {
                String contactName = contacts.getContactNameFromNumber(number, cContext);
                eventNumber.setText(number + "\n" + contactName);
            } else {
                eventNumber.setText("");
            }
            eventDate.setText(formattedTime);
        }

        return convertView;
    }
}