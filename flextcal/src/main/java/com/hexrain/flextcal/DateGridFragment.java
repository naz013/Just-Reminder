package com.hexrain.flextcal;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

/**
 * DateGridFragment contains only 1 gridview with 7 columns to display all the
 * dates within a month.
 * 
 * Client must supply gridAdapter and onItemClickListener before the fragment is
 * attached to avoid complex crash due to fragment life cycles.
 * 
 * @author thomasdao
 * 
 */
public class DateGridFragment extends Fragment {
	private GridView gridView;
	private FlextGridAdapter gridAdapter;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;

	public OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}
	
	public OnItemLongClickListener getOnItemLongClickListener() {
		return onItemLongClickListener;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	public FlextGridAdapter getGridAdapter() {
		return gridAdapter;
	}

	public void setGridAdapter(FlextGridAdapter gridAdapter) {
		this.gridAdapter = gridAdapter;
	}

	public GridView getGridView() {
		return gridView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.date_grid_fragment,
				container, false);

		gridView = (GridView) v.findViewById(R.id.calendar_gridview);

        //gridView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		// Client normally needs to provide the adapter and onItemClickListener
		// before the fragment is attached to avoid complex crash due to
		// fragment life cycles
		if (gridAdapter != null) {
			gridView.setAdapter(gridAdapter);
		}

		if (onItemClickListener != null) {
			gridView.setOnItemClickListener(onItemClickListener);
		}
		if(onItemLongClickListener != null) {
			gridView.setOnItemLongClickListener(onItemLongClickListener);
		}
		return v;
	}

}
