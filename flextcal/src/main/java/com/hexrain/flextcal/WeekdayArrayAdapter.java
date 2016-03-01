package com.hexrain.flextcal;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Customize the weekday gridview
 */
public class WeekdayArrayAdapter extends ArrayAdapter<String> {
	public static int textColor = Color.LTGRAY;
	private boolean isDark;

	public WeekdayArrayAdapter(Context context, int textViewResourceId,
							   List<String> objects, boolean isDark) {
		super(context, textViewResourceId, objects);
		this.isDark = isDark;
	}

	// To prevent cell highlighted when clicked
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	// Set color to gray and text size to 12sp
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// To customize text size and color
		TextView textView = (TextView) super.getView(position, convertView,
				parent);

		// Set content
		String item = getItem(position);

		// Show smaller text if the size of the text is 4 or more in some
		// locale
		if (item.length() <= 3) {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
		} else {
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		}

        if (isDark){
            textColor = getContext().getResources().getColor(android.R.color.white);
        } else textColor = getContext().getResources().getColor(android.R.color.black);

		textView.setTextColor(textColor);
		textView.setGravity(Gravity.CENTER);
		return textView;
	}

}
