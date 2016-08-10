package com.hexrain.flextcal;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
	private RecyclerView gridView;
	private FlextGridAdapter gridAdapter;
	private FlextGridAdapter.OnItemClickListener onItemClickListener;
	private FlextGridAdapter.OnItemLongClickListener onItemLongClickListener;

	public FlextGridAdapter.OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

	public void setOnItemClickListener(FlextGridAdapter.OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}
	
	public FlextGridAdapter.OnItemLongClickListener getOnItemLongClickListener() {
		return onItemLongClickListener;
	}
	
	public void setOnItemLongClickListener(FlextGridAdapter.OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	public FlextGridAdapter getGridAdapter() {
		return gridAdapter;
	}

	public void setGridAdapter(FlextGridAdapter gridAdapter) {
		this.gridAdapter = gridAdapter;
	}

	public RecyclerView getGridView() {
		return gridView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.date_grid_fragment,
				container, false);

		gridView = (RecyclerView) v.findViewById(R.id.calendar_gridview);
		gridView.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        gridView.setHasFixedSize(true);
		if (gridAdapter != null) {
			gridView.setAdapter(gridAdapter);
		}
		if (onItemClickListener != null) {
            gridAdapter.setOnItemClickListener(onItemClickListener);
		}
		if(onItemLongClickListener != null) {
            gridAdapter.setOnItemLongClickListener(onItemLongClickListener);
		}
		return v;
	}

}
