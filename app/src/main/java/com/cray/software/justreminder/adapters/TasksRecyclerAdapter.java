package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.async.SwitchTaskAsync;
import com.cray.software.justreminder.cloud.GTasksHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.TasksConstants;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.models.Task;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SyncListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TasksRecyclerAdapter extends RecyclerView.Adapter<TasksRecyclerAdapter.ViewHolder>  {

    private List<Task> mDataset;
    private Context mContext;
    private ColorSetter cs;
    private SyncListener listener;
    private SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());

    public TasksRecyclerAdapter(Context context, ArrayList<Task> myDataset) {
        this.mDataset = myDataset;
        this.mContext = context;
        cs = new ColorSetter(context);
    }

    public void setListener(SyncListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtTitle;
        TextView txtDate;
        CheckBox checkBox;
        TextView listColor;
        TextView note;
        CardView card;

        public ViewHolder(View v) {
            super(v);
            checkBox = (CheckBox) v.findViewById(R.id.checkDone);
            txtTitle = (TextView) v.findViewById(R.id.task);
            txtDate = (TextView) v.findViewById(R.id.taskDate);
            listColor = (TextView) v.findViewById(R.id.listColor);
            note = (TextView) v.findViewById(R.id.note);
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
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.listColor.setBackgroundColor(cs.getNoteColor(mDataset.get(position).getColor()));
        holder.card.setCardBackgroundColor(cs.getCardStyle());

        final String name = mDataset.get(position).getTitle();

        holder.txtTitle.setText(name);
        holder.txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, TaskManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, mDataset.get(position).getId())
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
            }
        });

        String notes = mDataset.get(position).getNote();
        if (notes != null && !notes.matches("")) {
            holder.note.setText(notes);
        } else {
            holder.note.setVisibility(View.GONE);
        }

        long date = mDataset.get(position).getDate();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (date != 0) {
            calendar.setTimeInMillis(date);
            String update = full24Format.format(calendar.getTime());
            holder.txtDate.setText(update);
        } else {
            holder.txtDate.setVisibility(View.INVISIBLE);
        }

        if (mDataset.get(position).getStatus().matches(GTasksHelper.TASKS_COMPLETE)){
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchTask(position, isChecked);
            }
        });
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
        TasksData db = new TasksData(mContext);
        db.open();
        if (isDone){
            db.setTaskDone(mDataset.get(position).getId());
        } else {
            db.setTaskUnDone(mDataset.get(position).getId());
        }

        new SwitchTaskAsync(mContext,
                mDataset.get(position).getListId(),
                mDataset.get(position).getTaskId(), isDone, listener)
                .execute();
    }
}