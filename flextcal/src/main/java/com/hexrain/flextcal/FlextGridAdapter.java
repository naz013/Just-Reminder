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
	protected ArrayList<DateTime> disableDates;
	protected ArrayList<DateTime> selectedDates;

	// Use internally, to make the search for date faster instead of using
	// indexOf methods on ArrayList
	protected HashMap<DateTime, Integer> disableDatesMap = new HashMap<>();
	protected HashMap<DateTime, Integer> selectedDatesMap = new HashMap<>();

	protected DateTime minDateTime;
	protected DateTime maxDateTime;
	protected DateTime today;
	protected int startDayOfWeek;
	protected boolean sixWeeksInCalendar;
	protected Resources resources;
	protected boolean isDark = false;
	protected SharedPreferences prefs;

    protected int backgroundForEventOne, backgroundForEventTwo, backgroundForToday = 0;
    protected HashMap<DateTime, String> textMapForEventOne, textMapForEventTwo;

	/**
	 * caldroidData belongs to Caldroid
	 */
	protected HashMap<String, Object> caldroidData;
	/**
	 * extraData belongs to client
	 */
	protected HashMap<String, Object> extraData;

	public void setAdapterDateTime(DateTime dateTime) {
		this.month = dateTime.getMonth();
		this.year = dateTime.getYear();
		this.datetimeList = FlextHelper.getFullWeeks(this.month, this.year,
				startDayOfWeek, sixWeeksInCalendar);
	}

	// GETTERS AND SETTERS
	public ArrayList<DateTime> getDatetimeList() {
		return datetimeList;
	}

	public DateTime getMinDateTime() {
		return minDateTime;
	}

	public void setMinDateTime(DateTime minDateTime) {
		this.minDateTime = minDateTime;
	}

	public DateTime getMaxDateTime() {
		return maxDateTime;
	}

	public void setMaxDateTime(DateTime maxDateTime) {
		this.maxDateTime = maxDateTime;
	}

	public ArrayList<DateTime> getDisableDates() {
		return disableDates;
	}

	public void setDisableDates(ArrayList<DateTime> disableDates) {
		this.disableDates = disableDates;
	}

	public ArrayList<DateTime> getSelectedDates() {
		return selectedDates;
	}

	public void setSelectedDates(ArrayList<DateTime> selectedDates) {
		this.selectedDates = selectedDates;
	}

	public HashMap<String, Object> getCaldroidData() {
		return caldroidData;
	}

	public void setCaldroidData(HashMap<String, Object> caldroidData) {
		this.caldroidData = caldroidData;

		// Reset parameters
		populateFromCaldroidData();
	}

	public HashMap<String, Object> getExtraData() {
		return extraData;
	}

	public void setExtraData(HashMap<String, Object> extraData) {
		this.extraData = extraData;
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param month
	 * @param year
	 * @param caldroidData
	 * @param extraData
	 */
	public FlextGridAdapter(Context context, int month, int year,
							HashMap<String, Object> caldroidData,
							HashMap<String, Object> extraData) {
		super();
		this.month = month;
		this.year = year;
		this.context = context;
		this.caldroidData = caldroidData;
		this.extraData = extraData;
		this.resources = context.getResources();

        prefs = context.getSharedPreferences("ui_settings", Context.MODE_PRIVATE);
        isDark = prefs.getBoolean("dark_theme", false);

		// Get data from caldroidData
		populateFromCaldroidData();
	}

	/**
	 * Retrieve internal parameters from caldroid data
	 */
	@SuppressWarnings("unchecked")
	private void populateFromCaldroidData() {
		disableDates = (ArrayList<DateTime>) caldroidData
				.get(FlextCal.DISABLE_DATES);
		if (disableDates != null) {
			disableDatesMap.clear();
			for (DateTime dateTime : disableDates) {
				disableDatesMap.put(dateTime, 1);
			}
		}

		selectedDates = (ArrayList<DateTime>) caldroidData
				.get(FlextCal.SELECTED_DATES);
		if (selectedDates != null) {
			selectedDatesMap.clear();
			for (DateTime dateTime : selectedDates) {
				selectedDatesMap.put(dateTime, 1);
			}
		}

		minDateTime = (DateTime) caldroidData.get(FlextCal._MIN_DATE_TIME);
		maxDateTime = (DateTime) caldroidData.get(FlextCal._MAX_DATE_TIME);
		startDayOfWeek = (Integer) caldroidData.get(FlextCal.START_DAY_OF_WEEK);
		sixWeeksInCalendar = (Boolean) caldroidData.get(FlextCal.SIX_WEEKS_IN_CALENDAR);

        backgroundForEventOne = (Integer) caldroidData.get(FlextCal._BACKGROUND_FOR_EVENT_ONE_);
        backgroundForEventTwo = (Integer) caldroidData.get(FlextCal._BACKGROUND_FOR_EVENT_TWO_);
        backgroundForToday = (Integer) caldroidData.get(FlextCal._BACKGROUND_FOR_TODAY_);

        textMapForEventOne = (HashMap<DateTime, String>) caldroidData
                .get(FlextCal._TEXT_FOR_EVENT_ONE);
        textMapForEventTwo = (HashMap<DateTime, String>) caldroidData
                .get(FlextCal._TEXT_FOR_EVENT_TWO);

		this.datetimeList = FlextHelper.getFullWeeks(this.month, this.year,
				startDayOfWeek, sixWeeksInCalendar);
	}

	protected DateTime getToday() {
		if (today == null) {
			today = FlextHelper.convertDateToDateTime(new Date());
		}
		return today;
	}

	@SuppressWarnings("unchecked")
	protected void setCustomResources(DateTime dateTime, View backgroundView, TextView textView, TextView task1, TextView task2) {
		// Set custom background resource
		task1.setTextColor(resources.getColor(backgroundForEventOne));
		task2.setTextColor(resources.getColor(backgroundForEventTwo));
		if (textMapForEventOne != null) {
			// Get background resource for the dateTime

			// Set it
			if (textMapForEventOne.containsKey(dateTime)) {
				task1.setText(textMapForEventOne.get(dateTime));
			}
		}

        if (textMapForEventTwo != null) {
            // Get background resource for the dateTime

            // Set it
            if (textMapForEventTwo.containsKey(dateTime))
                task2.setText(textMapForEventTwo.get(dateTime));
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

		// Get dateTime of this cell
		DateTime dateTime = this.datetimeList.get(position);

		// Set color of the dates in previous / next month
		if (dateTime.getMonth() != month) {
			cellView.setTextColor(resources.getColor(android.R.color.darker_gray));
		}

		boolean shouldResetDiabledView = false;
		boolean shouldResetSelectedView = false;

		// Customize for disabled dates and date outside min/max dates
		if ((minDateTime != null && dateTime.lt(minDateTime))
				|| (maxDateTime != null && dateTime.gt(maxDateTime))
				|| (disableDates != null && disableDatesMap
						.containsKey(dateTime))) {

            if (isDark) {
                cellView.setTextColor(resources.getColor(android.R.color.darker_gray));
            } else {
                cellView.setTextColor(resources.getColor(android.R.color.darker_gray));
            }

			if (FlextCal.disabledBackgroundDrawable == -1) {
				cellView.setBackgroundResource(android.R.color.transparent);
			} else {
				cellView.setBackgroundResource(FlextCal.disabledBackgroundDrawable);
			}

			if (dateTime.equals(getToday())) {
				cellView.setTextColor(resources.getColor(android.R.color.holo_red_light));
			}
		} else {
			shouldResetDiabledView = true;
		}

		// Customize for selected dates
		if (selectedDates != null && selectedDatesMap.containsKey(dateTime)) {
			/*if (FlextCal.selectedBackgroundDrawable != -1) {
				//cellView.setBackgroundResource(FlextCal.selectedBackgroundDrawable);
			} else {
				//cellView.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light));
			}*/

            if (isDark) {
                cellView.setTextColor(resources.getColor(android.R.color.white));
            } else {
                cellView.setTextColor(resources.getColor(android.R.color.black));
            }
		} else {
			shouldResetSelectedView = true;
		}

		if (shouldResetDiabledView && shouldResetSelectedView) {
			// Customize for today
			if (dateTime.equals(getToday())) {
                if (backgroundForToday != 0) {
                    cellView.setTextColor(backgroundForToday);
                }
			} else {
				cellView.setBackgroundResource(android.R.color.transparent);
			}
		}

		cellView.setText("" + dateTime.getDay());

		// Set custom color if required
		setCustomResources(dateTime, cellView, cellView, task1, task2);
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
