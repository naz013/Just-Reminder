package com.hexrain.flextcal;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * MonthPagerAdapter holds 4 fragments, which provides fragment for current
 * month, previous month and next month. The extra fragment helps for recycle
 * fragments.
 * 
 * @author thomasdao
 * 
 */
public class MonthPagerAdapter extends FragmentPagerAdapter {

	private ArrayList<DateGridFragment> fragments;

	// Lazily create the fragments
	public ArrayList<DateGridFragment> getFragments() {
		if (fragments == null) {
			fragments = new ArrayList<>();
			for (int i = 0; i < getCount(); i++) {
				fragments.add(new DateGridFragment());
			}
		}
		return fragments;
	}

	public void setFragments(ArrayList<DateGridFragment> fragments) {
		this.fragments = fragments;
	}

	public MonthPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return getFragments().get(position);
	}

    @Override
	public int getCount() {
		// We need 4 gridviews for previous month, current month and next month,
		// and 1 extra fragment for fragment recycle
		return FlextCal.NUMBER_OF_PAGES;
	}

}
