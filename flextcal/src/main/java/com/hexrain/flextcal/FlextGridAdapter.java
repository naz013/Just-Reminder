package com.hexrain.flextcal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import hirondelle.date4j.DateTime;

public class FlextGridAdapter extends RecyclerView.Adapter<FlextGridAdapter.MyHolder> {
	protected ArrayList<DateTime> datetimeList;
	protected int month;
	protected int year;
	protected Context context;
	protected DateTime today;
	protected int startDayOfWeek;
	protected Resources resources;
	protected boolean isDark = false;
	protected SharedPreferences prefs;

    protected int backgroundForToday = -1;
    protected HashMap<DateTime, Events> textMapForEvents;

	protected HashMap<String, Object> mData;

	protected OnItemClickListener onItemClickListener;
	protected OnItemLongClickListener onItemLongClickListener;

	public void setAdapterDateTime(DateTime dateTime) {
		this.month = dateTime.getMonth();
		this.year = dateTime.getYear();
		this.datetimeList = FlextHelper.getFullWeeks(this.month, this.year, startDayOfWeek);
	}

	public ArrayList<DateTime> getDatetimeList() {
		return datetimeList;
	}

	public void setCaldroidData(HashMap<String, Object> caldroidData) {
		this.mData = caldroidData;
		populateFromCaldroidData();
	}

	public FlextGridAdapter(Context context, int month, int year,
							HashMap<String, Object> caldroidData, boolean isDark) {
		super();
		this.month = month;
		this.year = year;
		this.context = context;
		this.mData = caldroidData;
		this.resources = context.getResources();
        this.isDark = isDark;
		populateFromCaldroidData();
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	class MyHolder extends RecyclerView.ViewHolder {

		TextView cellView;
		TextView task1;
		TextView task2;

		public MyHolder(View itemView) {
			super(itemView);
			cellView = (TextView) itemView.findViewById(R.id.textView);
			task1 = (TextView) itemView.findViewById(R.id.task1);
			task2 = (TextView) itemView.findViewById(R.id.task2);
			cellView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (onItemClickListener != null) {
						onItemClickListener.onItemClick(view, getAdapterPosition());
					}
				}
			});
			cellView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					if (onItemLongClickListener != null) {
						onItemLongClickListener.onItemLongClick(view, getAdapterPosition());
					}
					return false;
				}
			});
		}
	}

	private void populateFromCaldroidData() {
		startDayOfWeek = (Integer) mData.get(FlextCal.START_DAY_OF_WEEK);
        backgroundForToday = (Integer) mData.get(FlextCal._BACKGROUND_FOR_TODAY_);
        textMapForEvents = (HashMap<DateTime, Events>) mData.get(FlextCal._EVENTS_);
		this.datetimeList = FlextHelper.getFullWeeks(this.month, this.year, startDayOfWeek);
	}

	protected DateTime getToday() {
		if (today == null) {
			today = FlextHelper.convertDateToDateTime(new Date());
		}
		return today;
	}

	protected void setCustomResources(DateTime dateTime, TextView task1, TextView task2) {
		if (textMapForEvents != null) {
			if (textMapForEvents.containsKey(dateTime)) {
				Events events = textMapForEvents.get(dateTime);
				if (events.hasNext()) {
					Events.Event event = events.getNext();
					task1.setText(event.getTask());
					task1.setTextColor(event.getColor());
				}
				if (events.hasNext()) {
					Events.Event event = events.getLast();
					task2.setText(event.getTask());
					task2.setTextColor(event.getColor());
				}
				events.mPosition = 0;
			}
		}
	}

	protected void customizeTextView(int position, TextView cellView, TextView task1, TextView task2) {
        if (isDark){
            cellView.setTextColor(resources.getColor(android.R.color.white));
        } else {
            cellView.setTextColor(resources.getColor(android.R.color.black));
        }
		DateTime dateTime = this.datetimeList.get(position);
		boolean notCurrent = dateTime.getMonth() != month;
		if (notCurrent) {
			cellView.setTextColor(resources.getColor(android.R.color.darker_gray));
		}
		if (dateTime.equals(getToday())) {
			if (backgroundForToday != 0) {
				cellView.setTextColor(backgroundForToday);
			} else {
				cellView.setTextColor(resources.getColor(android.R.color.holo_red_light));
			}
		} else {
			cellView.setBackgroundResource(android.R.color.transparent);
		}
		cellView.setText(String.valueOf(dateTime.getDay()));
		if (!notCurrent) setCustomResources(dateTime, task1, task2);
	}

	@Override
	public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new MyHolder(inflater.inflate(R.layout.date_cell, null, false));
	}

	@Override
	public void onBindViewHolder(MyHolder holder, int position) {
		customizeTextView(position, holder.cellView, holder.task1, holder.task2);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public int getItemCount() {
		return datetimeList.size();
	}

	public interface OnItemClickListener {
		void onItemClick(View view, int position);
	}

	public interface OnItemLongClickListener {
		void onItemLongClick(View view, int position);
	}
}
