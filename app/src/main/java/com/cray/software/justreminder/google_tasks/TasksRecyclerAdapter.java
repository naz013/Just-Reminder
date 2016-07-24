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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder>  {

    private List<TaskItem> mDataset;
    private Context mContext;
    private ColorSetter cs;
    private SyncListener listener;
    private SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());
    private Map<String, Integer> colors;

    public TasksRecyclerAdapter(Context context, List<TaskItem> myDataset, Map<String, Integer> colors) {
        this.mDataset = myDataset;
        this.mContext = context;
        this.colors = colors;
        cs = new ColorSetter(context);
    }

    public void setListener(SyncListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        RoboTextView txtTitle;
        RoboTextView txtDate;
        RoboCheckBox checkBox;
        RoboTextView listColor;
        RoboTextView note;
        CardView card;

        public ViewHolder(View v) {
            super(v);
            checkBox = (RoboCheckBox) v.findViewById(R.id.checkDone);
            txtTitle = (RoboTextView) v.findViewById(R.id.task);
            txtDate = (RoboTextView) v.findViewById(R.id.taskDate);
            listColor = (RoboTextView) v.findViewById(R.id.listColor);
            note = (RoboTextView) v.findViewById(R.id.note);
            card = (CardView) v.findViewById(R.id.card);
            checkBox.setFocusableInTouchMode(false);
            checkBox.setFocusable(false);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClick(getAdapterPosition());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TaskItem item = mDataset.get(position);
        String listId = item.getListId();
        if (colors.containsKey(listId)) {
            holder.listColor.setBackgroundColor(cs.getNoteColor(colors.get(listId)));
        }
        holder.card.setCardBackgroundColor(cs.getCardStyle());
        holder.txtTitle.setText(item.getTitle());
        holder.txtTitle.setOnClickListener(v -> mContext.startActivity(new Intent(mContext, TaskManager.class)
                .putExtra(Constants.ITEM_ID_INTENT, item.getId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT)));

        String notes = item.getNotes();
        if (notes != null && !notes.matches("")) {
            holder.note.setText(notes);
        } else {
            holder.note.setVisibility(View.GONE);
        }

        long date = item.getDueDate();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (date != 0) {
            calendar.setTimeInMillis(date);
            String update = full24Format.format(calendar.getTime());
            holder.txtDate.setText(update);
        } else {
            holder.txtDate.setVisibility(View.INVISIBLE);
        }

        if (item.getStatus().matches(GTasksHelper.TASKS_COMPLETE)){
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> switchTask(position, isChecked));
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
        mContext.startActivity(new Intent(mContext, TaskManager.class)
                .putExtra(Constants.ITEM_ID_INTENT, mDataset.get(position).getId())
                .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
    }

    private void switchTask(int position, boolean isDone){
        TasksDataBase db = new TasksDataBase(mContext);
        db.open();
        if (isDone){
            db.setTaskDone(mDataset.get(position).getId());
        } else {
            db.setTaskUnDone(mDataset.get(position).getId());
        }

        new SwitchTaskAsync(mContext, mDataset.get(position).getListId(),
                mDataset.get(position).getTaskId(), isDone, listener).execute();
    }
}