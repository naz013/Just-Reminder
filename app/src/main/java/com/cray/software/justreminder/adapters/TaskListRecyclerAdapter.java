package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.utils.ViewUtils;

public class TaskListRecyclerAdapter extends RecyclerView.Adapter<TaskListRecyclerAdapter.ViewHolder> {
    private Context mContext;
    private ShoppingListDataProvider provider;
    private boolean isDark;
    private ActionListener listener;

    public TaskListRecyclerAdapter(Context context, ShoppingListDataProvider provider, ActionListener listener) {
        this.mContext = context;
        this.provider = provider;
        this.listener = listener;
        isDark = new SharedPrefs(context).loadBoolean(Prefs.USE_DARK_THEME);
        setHasStableIds(true);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        RelativeLayout background;
        CheckBox itemCheck;
        ImageButton clearButton;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.shopText);
            clearButton = (ImageButton) v.findViewById(R.id.clearButton);
            background = (RelativeLayout) v.findViewById(R.id.background);
            itemCheck = (CheckBox) v.findViewById(R.id.itemCheck);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task_item_card, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final ShoppingList item = provider.getItem(position);
        String title = item.getTitle();

        int isChecked = item.isChecked();
        if (isChecked == 1){
            holder.textView.setPaintFlags(holder.textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.textView.setPaintFlags(holder.textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        holder.itemCheck.setChecked(isChecked == 1);
        holder.textView.setText(title);

        if (listener == null){
            holder.clearButton.setVisibility(View.GONE);
            holder.itemCheck.setEnabled(false);
            holder.textView.setTextColor(ViewUtils.getColor(mContext, R.color.blackPrimary));
        } else {
            if (isDark) {
                holder.clearButton.setImageResource(R.drawable.ic_clear_white_24dp);
            } else {
                holder.clearButton.setImageResource(R.drawable.ic_clear_grey600_24dp);
            }

            holder.itemCheck.setVisibility(View.VISIBLE);
            holder.clearButton.setVisibility(View.VISIBLE);
            holder.clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onItemDelete(position);
                }
            });

            holder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null && item.getStatus() == ShoppingList.DELETED)
                        listener.onItemChange(position);
                }
            });

            holder.itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null) listener.onItemCheck(position, isChecked);
                }
            });
        }

        if (item.getStatus() == ShoppingList.DELETED){
            holder.textView.setTextColor(ViewUtils.getColor(mContext, R.color.redPrimaryDark));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return provider.getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getCount();
    }

    public ShoppingListDataProvider getProvider(){
        return provider;
    }

    public interface ActionListener{
        void onItemCheck(int position, boolean isChecked);
        void onItemDelete(int position);
        void onItemChange(int position);
    }
}