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

package com.cray.software.justreminder.google_tasks;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GoogleTasks;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databinding.ListItemTaskBinding;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;

import java.util.List;
import java.util.Map;

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder>  {

    private List<TaskItem> mDataset;
    private Context mContext;
    private static ColorSetter cs;
    private static SyncListener listener;
    private static Map<String, Integer> colors;

    public TasksRecyclerAdapter(Context context, List<TaskItem> myDataset, Map<String, Integer> colors) {
        this.mDataset = myDataset;
        this.mContext = context;
        TasksRecyclerAdapter.colors = colors;
        cs = ColorSetter.getInstance(context);
    }

    public void setListener(SyncListener listener) {
        TasksRecyclerAdapter.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ListItemTaskBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.setClick(v1 -> onItemClick(getAdapterPosition()));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(DataBindingUtil.inflate(inflater, R.layout.list_item_task, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TaskItem item = mDataset.get(position);
        holder.binding.setTask(item);
    }

    @Override
    public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void onItemClick(int position){
        mContext.startActivity(new Intent(mContext, TaskActivity.class)
                .putExtra(Constants.ITEM_ID_INTENT, mDataset.get(position).getId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
    }

    private static void switchTask(Context context, long id, boolean isDone, String listId, String taskId){
        TasksHelper.getInstance(context).setStatus(id, isDone);
        new SwitchTaskAsync(context, listId, taskId, isDone, listener).execute();
    }

    @BindingAdapter({"loadMarker"})
    public static void loadMarker(View view, String listId) {
        if (colors.containsKey(listId)) {
            view.setBackgroundColor(cs.getNoteColor(colors.get(listId)));
        }
    }

    @BindingAdapter({"loadCheck"})
    public static void loadCheck(RoboCheckBox checkBox, TaskItem item) {
        if (item.getStatus().matches(GoogleTasks.TASKS_COMPLETE)){
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> switchTask(checkBox.getContext(),
                item.getId(), isChecked, item.getListId(), item.getTaskId()));
    }
}