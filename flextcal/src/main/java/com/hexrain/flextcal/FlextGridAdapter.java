package com.hexrain.flextcal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import hirondelle.date4j.DateTime;

/**
 * The FlextGridAdapter provides customized view for the dates gridview
 * 
 * @author thomasdao
 * 
 */
public class FlextGridAdapter extends BaseAdapter {
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

	/**
	 * caldroidData belongs to Caldroid
	 */
	protected HashMap<String, Object> caldroidData;

	public void setAdapterDateTime(DateTime dateTime) {
		this.month = dateTime.getMonth();
		this.year = dateTime.getYear();
		this.datetimeList = FlextHelper.getFullWeeks(this.month, this.year, startDayOfWeek);
	}

	// GETTERS AND SETTERS
	public ArrayList<DateTime> getDatetimeList() {
		return datetimeList;
	}

	public void setCaldroidData(HashMap<String, Object> caldroidData) {
		this.caldroidData = caldroidData;

		// Reset parameters
		populateFromCaldroidData();
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param month
	 * @param year
	 * @param caldroidData
	 */
	public FlextGridAdapter(Context context, int month, int year,
							HashMap<String, Object> caldroidData, boolean isDark) {
		super();
		this.month = month;
		this.year = year;
		this.context = context;
		this.caldroidData = caldroidData;
		this.resources = context.getResources();
        this.isDark = isDark;

		populateFromCaldroidData();
	}

	/**
	 * Retrieve internal parameters from caldroid data
	 */
	@SuppressWarnings("unchecked")
	private void populateFromCaldroidData() {
		startDayOfWeek = (Integer) caldroidData.get(FlextCal.START_DAY_OF_WEEK);
        backgroundForToday = (Integer) caldroidData.get(FlextCal._BACKGROUND_FOR_TODAY_);
        textMapForEvents = (HashMap<DateTime, Events>) caldroidData
                .get(FlextCal._EVENTS_);

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
			// Set it
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

	/**
	 * Customize colors of text and background based on states of the cell
	 * (disabled, active, selected, etc)
	 * 
	 * To be used only in getView method
     * @param position
     * @param cellView
     * @param task1
     * @param task2
     */
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
	public int getCount() {
		// TODO Auto-generated method stub
		return this.datetimeList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.date_cell, null, false);

        TextView cellView = (TextView) view.findViewById(R.id.textView);
        TextView task1 = (TextView) view.findViewById(R.id.task1);
        TextView task2 = (TextView) view.findViewById(R.id.task2);
		customizeTextView(position, cellView, task1, task2);
		return view;
	}
}
