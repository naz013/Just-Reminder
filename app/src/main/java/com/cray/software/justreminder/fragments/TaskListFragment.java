package com.cray.software.justreminder.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TasksRecyclerAdapter;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.models.Task;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;

public class TaskListFragment extends Fragment implements SyncListener {

    private RecyclerView currentList;
    private LinearLayout emptyItem;
    private ArrayList<Task> datas;

    private NavigationCallbacks mCallbacks;

    public void setData(ArrayList<Task> datas){
        this.datas = datas;
    }

    public void setmCallbacks(NavigationCallbacks mCallbacks) {
        this.mCallbacks = mCallbacks;
    }

    public static TaskListFragment newInstance() {
        TaskListFragment pageFragment = new TaskListFragment();
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intent = getArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        emptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) view.findViewById(R.id.emptyText);
        emptyText.setText(R.string.no_google_tasks);
        emptyItem.setVisibility(View.VISIBLE);

        ImageView emptyImage = (ImageView) view.findViewById(R.id.emptyImage);
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.clear_white);
        } else {
            emptyImage.setImageResource(R.drawable.clear);
        }

        if (Module.isLollipop()) {
            emptyImage.setImageDrawable(ViewUtils.getVector(getActivity(),
                    new ColorSetter(getActivity()).colorAccent(),
                    R.drawable.ic_assignment_turned_in_black_24dp));
        }

        currentList = (RecyclerView) view.findViewById(R.id.currentList);
        loaderAdapter();
        return view;
    }

    public void loaderAdapter(){
        TasksRecyclerAdapter customAdapter = new TasksRecyclerAdapter(getActivity(), datas);
        customAdapter.setListener(this);
        currentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        currentList.setAdapter(customAdapter);
        currentList.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(currentList);
        }
        reloadView();
    }

    private void reloadView() {
        if (datas != null && datas.size() > 0){
            currentList.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            currentList.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void endExecution(boolean result) {
        loaderAdapter();
    }
}
