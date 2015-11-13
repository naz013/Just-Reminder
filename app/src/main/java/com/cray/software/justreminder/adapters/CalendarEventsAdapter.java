package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.EventsDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarEventsAdapter extends RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder> {

    private ArrayList<EventsDataProvider.EventsItem> mDatas;
    private Context mContext;
    private ColorSetter cs;
    private SharedPrefs prefs;
    private SimpleListener mEventListener;

    @SuppressWarnings("deprecation")
    public CalendarEventsAdapter(Context context, ArrayList<EventsDataProvider.EventsItem> datas) {
        this.mContext = context;
        this.mDatas = datas;
        cs = new ColorSetter(context);
        prefs = new SharedPrefs(context);
    }

    public void setmEventListener(SimpleListener mEventListener) {
        this.mEventListener = mEventListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView eventText, eventColor, eventType, eventDate, eventNumber;
        ViewGroup container;
        RelativeLayout background;

        public ViewHolder(View v) {
            super(v);
            eventText = (TextView) v.findViewById(R.id.eventText);
            eventColor = (TextView) v.findViewById(R.id.eventColor);
            eventType = (TextView) v.findViewById(R.id.eventType);
            eventDate = (TextView) v.findViewById(R.id.eventDate);
            eventNumber = (TextView) v.findViewById(R.id.eventNumber);
            container = (ViewGroup) v.findViewById(R.id.container);
            background = (RelativeLayout) v.findViewById(R.id.background);
            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mEventListener != null) mEventListener.onItemClicked(getAdapterPosition(), background);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null)
                mEventListener.onItemLongClicked(getAdapterPosition(), background);
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_events, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.background.setBackgroundResource(cs.getCardDrawableStyle());

        final EventsDataProvider.EventsItem item = mDatas.get(position);
        EventsDataProvider.Type type = item.getInn();
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);

        if (type == EventsDataProvider.Type.birthday) {
            holder.eventColor.setBackgroundColor(ViewUtils.getColor(mContext, cs.colorBirthdayCalendar()));
            holder.eventType.setText(mContext.getString(R.string.birthday_text));
            String title = item.getName();
            String phone = item.getNumber();
            if (phone == null || phone.matches("")) phone = Contacts.getNumber(title, mContext);
            if (phone != null && !phone.matches("")){
                holder.eventNumber.setText(phone);
            } else {
                holder.eventNumber.setText("");
            }
            holder.eventText.setText(title);

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(item.getDate());
            Date time = cl.getTime();
            holder.eventDate.setText(SuperUtil.appendString(TimeUtil.getDateTime(time, is24),
                    "\n", String.valueOf(TimeUtil.getAge(item.getYear())), " " ,
                    mContext.getString(R.string.years_string)));
        } else {
            holder.eventColor.setBackgroundColor(ViewUtils.getColor(mContext, cs.getCategoryColor(item.getColor())));
            holder.eventType.setText(mContext.getString(R.string.reminder_type));

            String number = item.getNumber();

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(item.getDate());

            holder.eventText.setText(item.getName());

            if (!number.matches("0")) {
                String contactName = Contacts.getContactNameFromNumber(number, mContext);
                holder.eventNumber.setText(SuperUtil.appendString(number, "\n", contactName));
            } else {
                holder.eventNumber.setText("");
            }

            Date time = cl.getTime();
            holder.eventDate.setText(TimeUtil.getDateTime(time, is24));
        }
    }

    @Override
    public long getItemId(int position) {
        return mDatas.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }
}