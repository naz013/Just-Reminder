package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.enums.EventType;
import com.cray.software.justreminder.datas.models.EventsItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * RecyclerView adapter for today view fragment.
 */
public class CalendarEventsAdapter extends RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder> {

    /**
     * CardView elevation constant.
     */
    private final static float CARD_ELEVATION = 5f;

    /**
     * List of events models.
     */
    private ArrayList<EventsItem> mDatas;

    /**
     * Context field.
     */
    private Context mContext;

    /**
     * ColorSetter helper class field.
     */
    private ColorSetter cs;

    /**
     * Shared Preferences helper class.
     */
    private SharedPrefs prefs;

    /**
     * RecyclerView action listener.
     */
    private SimpleListener mEventListener;

    /**
     * Adapter constructor.
     * @param context application context.
     * @param datas list of events.
     */
    public CalendarEventsAdapter(final Context context, final ArrayList<EventsItem> datas) {
        this.mContext = context;
        this.mDatas = datas;
        cs = new ColorSetter(context);
        prefs = new SharedPrefs(context);
    }

    /**
     * Set action listener for adapter.
     * @param listener action listener.
     */
    public final void setmEventListener(final SimpleListener listener) {
        this.mEventListener = listener;
    }

    /**
     * View Holder class.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {

        /**
         * TextViews in holder.
         */
        public TextView eventText, eventColor, eventType, eventDate,
                eventNumber;

        /**
         * CardView field.
         */
        public CardView itemCard;

        /**
         * Holder constructor.
         * @param v view.
         */
        public ViewHolder(final View v) {
            super(v);
            eventText = (TextView) v.findViewById(R.id.eventText);
            eventColor = (TextView) v.findViewById(R.id.eventColor);
            eventType = (TextView) v.findViewById(R.id.eventType);
            eventDate = (TextView) v.findViewById(R.id.eventDate);
            eventNumber = (TextView) v.findViewById(R.id.eventNumber);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(CARD_ELEVATION);
            }

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public final void onClick(final View v) {
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), itemCard);
            }
        }

        @Override
        public final boolean onLongClick(final View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
            }
            return true;
        }
    }

    @Override
    public final ViewHolder onCreateViewHolder(final ViewGroup parent,
                                               final int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_events, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public final void onBindViewHolder(final ViewHolder holder, final int position) {
        final EventsItem item = mDatas.get(position);
        EventType type = item.getInn();
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);

        if (type == EventType.birthday) {
            holder.eventColor.setBackgroundColor(ViewUtils.getColor(mContext,
                    cs.colorBirthdayCalendar()));
            holder.eventType.setText(mContext.getString(R.string.birthday_text));
            String title = item.getName();
            String phone = item.getNumber();
            if (phone == null || phone.matches("")) {
                phone = Contacts.getNumber(title, mContext);
            }
            if (phone != null && !phone.matches("")) {
                holder.eventNumber.setText(phone);
            } else {
                holder.eventNumber.setText("");
            }
            holder.eventText.setText(title);

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(item.getDate());
            Date time = cl.getTime();
            holder.eventDate.setText(SuperUtil.appendString(TimeUtil.getDateTime(time, is24),
                    "\n", String.valueOf(TimeUtil.getAge(item.getYear())), " ",
                    mContext.getString(R.string.years_string)));
        } else {
            holder.eventColor.setBackgroundColor(ViewUtils.getColor(mContext,
                    cs.getCategoryColor(item.getColor())));
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
    public final long getItemId(final int position) {
        return mDatas.get(position).getId();
    }

    @Override
    public final int getItemCount() {
        return mDatas.size();
    }
}
