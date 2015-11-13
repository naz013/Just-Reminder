package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.FileDataProvider;
import com.cray.software.justreminder.datas.FileModel;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.SimpleListener;

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private FileDataProvider provider;
    private SimpleListener mEventListener;

    public FileRecyclerAdapter(Context context, FileDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ViewGroup container;
        RelativeLayout background;
        TextView fileName, lastModified, task, type, number, date, time, repeat;

        public ViewHolder(View v) {
            super(v);
            container = (ViewGroup) v.findViewById(R.id.container);
            background = (RelativeLayout) v.findViewById(R.id.background);
            fileName = (TextView) v.findViewById(R.id.fileName);
            lastModified = (TextView) v.findViewById(R.id.lastModified);
            task = (TextView) v.findViewById(R.id.task);
            type = (TextView) v.findViewById(R.id.type);
            number = (TextView) v.findViewById(R.id.number);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
            repeat = (TextView) v.findViewById(R.id.repeat);
            container.setOnClickListener(this);
            container.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mEventListener != null) mEventListener.onItemClicked(getAdapterPosition(), background);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null)
                mEventListener.onItemLongClicked(getAdapterPosition(), background);
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_file, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.background.setBackgroundResource(cs.getCardDrawableStyle());

        final FileModel item = provider.getData().get(position);

        holder.task.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.time.setText(item.getTime());
        holder.repeat.setText(item.getRepeat());
        holder.lastModified.setText(item.getLastModified());
        holder.type.setText(item.getType());
        holder.number.setText(item.getNumber());
        holder.fileName.setText(item.getFileName());
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return provider.getData().get(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getData().size();
    }

    public SimpleListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}