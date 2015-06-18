package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.TaskManager;
import com.cray.software.justreminder.async.SwitchTaskAsync;
import com.cray.software.justreminder.databases.TasksData;
import com.cray.software.justreminder.datas.ListItems;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.SyncListener;
import com.cray.software.justreminder.interfaces.TasksConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TasksRecyclerAdapter extends BaseAdapter {

    private List<ListItems> mDataset;
    LayoutInflater inflater;
    Context mContext;
    SyncListener mListener;
    TasksData data;
    ColorSetter cs;
    SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM");

    public TasksRecyclerAdapter(Context context, ArrayList<ListItems> myDataset, SyncListener listener) {
        this.mDataset = myDataset;
        this.mContext = context;
        this.mListener = listener;
        data = new TasksData(context);
        cs = new ColorSetter(context);
        inflater = LayoutInflater.from(context.getApplicationContext());
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        data.open();
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_task, null);

            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkDone);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.task);
            holder.txtDate = (TextView) convertView.findViewById(R.id.taskDate);
            holder.listColor = (TextView) convertView.findViewById(R.id.listColor);
            holder.note = (TextView) convertView.findViewById(R.id.note);
            holder.card = (CardView) convertView.findViewById(R.id.card);
            holder.checkBox.setFocusableInTouchMode(false);
            holder.checkBox.setFocusable(false);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    switchTask(position, isChecked);
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Cursor c = data.getTasksList(mDataset.get(position).getListId());
        if (c != null && c.moveToFirst()){
            holder.listColor.setBackgroundColor(
                    mContext.getResources().getColor(
                            cs.getNoteColor(c.getInt(c.getColumnIndex(TasksConstants.COLUMN_COLOR)))));
        } else {
            holder.listColor.setBackgroundColor(
                    mContext.getResources().getColor(cs.getNoteColor(8)));
        }

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
        if (notes != null && !notes.matches("")) holder.note.setText(notes);
        else holder.note.setVisibility(View.GONE);

        long date = mDataset.get(position).getDate();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (date != 0) {
            calendar.setTimeInMillis(date);
            String update = full24Format.format(calendar.getTime());
            holder.txtDate.setText(update);
        } else holder.txtDate.setVisibility(View.INVISIBLE);

        if (mDataset.get(position).getStatus().matches(Constants.TASKS_COMPLETE)){
            holder.checkBox.setChecked(true);
        } else holder.checkBox.setChecked(false);

        holder.txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, TaskManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, mDataset.get(position).getId())
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
            }
        });

        holder.note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, TaskManager.class)
                        .putExtra(Constants.ITEM_ID_INTENT, mDataset.get(position).getId())
                        .putExtra(TasksConstants.INTENT_ACTION, TasksConstants.EDIT));
            }
        });

        return convertView;
    }

    private void switchTask(int position, boolean isDone){
        data.open();
        if (isDone){
            data.setTaskDone(mDataset.get(position).getId());
        } else {
            data.setTaskUnDone(mDataset.get(position).getId());
        }

        new SwitchTaskAsync(mContext,
                mDataset.get(position).getListId(),
                mDataset.get(position).getTaskId(), isDone, mListener)
                .execute();
    }

    public class ViewHolder {
        public TextView txtTitle;
        public TextView txtDate;
        public CheckBox checkBox;
        public TextView listColor;
        public TextView note;
        public CardView card;
    }
}