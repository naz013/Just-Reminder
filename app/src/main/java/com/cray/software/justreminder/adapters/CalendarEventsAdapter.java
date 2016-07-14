/**
 * Copyright 2015 Nazar Suhovich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.datas.models.EventsItem;
import com.cray.software.justreminder.enums.EventType;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * RecyclerView adapter for today view fragment.
 */
public class CalendarEventsAdapter extends RecyclerView.Adapter<CalendarEventsAdapter.ViewHolder> {

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
     * RecyclerView action listener.
     */
    private SimpleListener mEventListener;
    private boolean is24;

    /**
     * Adapter constructor.
     * @param context application context.
     * @param datas list of events.
     */
    public CalendarEventsAdapter(final Context context, final ArrayList<EventsItem> datas) {
        this.mContext = context;
        this.mDatas = datas;
        cs = new ColorSetter(context);
        is24 = SharedPrefs.getInstance(context).getBoolean(Prefs.IS_24_TIME_FORMAT);
    }

    /**
     * Set action listener for adapter.
     * @param listener action listener.
     */
    public final void setEventListener(final SimpleListener listener) {
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
        public RoboTextView eventText, eventColor, eventType, eventDate, eventNumber;

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
            eventText = (RoboTextView) v.findViewById(R.id.eventText);
            eventColor = (RoboTextView) v.findViewById(R.id.eventColor);
            eventType = (RoboTextView) v.findViewById(R.id.eventType);
            eventDate = (RoboTextView) v.findViewById(R.id.eventDate);
            eventNumber = (RoboTextView) v.findViewById(R.id.eventNumber);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
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
        if (type == EventType.birthday) {
            holder.eventColor.setBackgroundColor(cs.getColor(cs.colorBirthdayCalendar()));
            holder.eventType.setText(R.string.birthday);
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
                    "\n", TimeUtil.getAgeFormatted(mContext, item.getYear())));
        } else {
            holder.eventColor.setBackgroundColor(cs.getColor(cs.getCategoryColor(item.getColor())));
            holder.eventType.setText(mContext.getString(R.string.type));

            String number = item.getNumber();

            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(item.getDate());

            holder.eventText.setText(item.getName());

            if (!number.matches("0")) {
                String contactName = Contacts.getNameFromNumber(number, mContext);
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
