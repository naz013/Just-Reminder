package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

public class TaskListRecyclerAdapter extends RecyclerView.Adapter<TaskListRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private SharedPrefs prefs;
    private ColorSetter cs;
    private ShoppingListDataProvider provider;
    private boolean isDark;

    public TaskListRecyclerAdapter(Context context, ShoppingListDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        setHasStableIds(true);
    }

    public static class ViewHolder extends AbstractDraggableItemViewHolder {
        TextView textView;
        ViewGroup container;
        RelativeLayout background;
        CheckBox itemCheck;
        EditText taskEdit;
        ImageButton clearButton, newButton;
        View dragHandle;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.textView);
            container = (ViewGroup) v.findViewById(R.id.container);
            clearButton = (ImageButton) v.findViewById(R.id.clearButton);
            newButton = (ImageButton) v.findViewById(R.id.newButton);
            dragHandle = v.findViewById(R.id.dragHandle);
            background = (RelativeLayout) v.findViewById(R.id.background);
            itemCheck = (CheckBox) v.findViewById(R.id.itemCheck);
            taskEdit = (EditText) v.findViewById(R.id.taskEdit);
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
        ShoppingList.Type type = item.getType();
        if (type == ShoppingList.Type.NEW) {
            holder.itemCheck.setVisibility(View.GONE);
            holder.taskEdit.setVisibility(View.GONE);
            holder.clearButton.setVisibility(View.GONE);
            holder.newButton.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.VISIBLE);
            holder.dragHandle.setVisibility(View.INVISIBLE);
        }
        if (type == ShoppingList.Type.EDITABLE) {
            holder.itemCheck.setVisibility(View.VISIBLE);
            holder.taskEdit.setVisibility(View.VISIBLE);
            holder.clearButton.setVisibility(View.VISIBLE);
            holder.newButton.setVisibility(View.GONE);
            holder.textView.setVisibility(View.GONE);
            holder.dragHandle.setVisibility(View.VISIBLE);
        }
        if (type == ShoppingList.Type.READY) {
            holder.itemCheck.setVisibility(View.VISIBLE);
            holder.taskEdit.setVisibility(View.GONE);
            holder.clearButton.setVisibility(View.VISIBLE);
            holder.newButton.setVisibility(View.GONE);
            holder.textView.setVisibility(View.VISIBLE);
            holder.dragHandle.setVisibility(View.VISIBLE);
        }

        if (isDark) {
            holder.newButton.setImageResource(R.drawable.ic_add_white_24dp);
            holder.clearButton.setImageResource(R.drawable.ic_clear_white_24dp);
            holder.dragHandle.setBackgroundResource(R.drawable.ic_reorder_white_24dp);
        } else {
            holder.newButton.setImageResource(R.drawable.ic_add_grey600_24dp);
            holder.clearButton.setImageResource(R.drawable.ic_clear_grey600_24dp);
            holder.dragHandle.setBackgroundResource(R.drawable.ic_reorder_grey600_24dp);
        }

        holder.taskEdit.setText(item.getTitle());
        holder.textView.setText(item.getTitle());
        if (item.isChecked()) holder.textView.setTextColor(mContext.getResources().getColor(R.color.colorRed));
        else {
            if (position < provider.getCount() - 1) {
                holder.textView.setTextColor(mContext.getResources().getColor(R.color.colorGreen));
            }
        }

        holder.newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = provider.addItem();
                notifyDataSetChanged();
                Log.d(Constants.LOG_TAG, "Position " + position);
            }
        });

        holder.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                provider.removeItem(position);
                notifyDataSetChanged();
                Log.d(Constants.LOG_TAG, "Position " + position);
            }
        });

        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position < provider.getCount() - 1) {
                    holder.taskEdit.setText(provider.getItem(position).getTitle());
                    provider.getItem(position).setType(ShoppingList.Type.EDITABLE);
                    holder.textView.setVisibility(View.GONE);
                    holder.taskEdit.setVisibility(View.VISIBLE);
                    Log.d(Constants.LOG_TAG, "Position " + position);
                } else {
                    int position = provider.addItem();
                    notifyDataSetChanged();
                    Log.d(Constants.LOG_TAG, "Position " + position);
                }
            }
        });

        holder.itemCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                provider.getItem(position).setIsChecked(isChecked);
                notifyDataSetChanged();
                Log.d(Constants.LOG_TAG, "Position " + position);
            }
        });

        holder.taskEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ShoppingList shoppingList = provider.getItem(position);
                if (shoppingList != null) shoppingList.setTitle(s.toString());
                Log.d(Constants.LOG_TAG, "Position " + position);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        holder.taskEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    holder.textView.setText(provider.getItem(position).getTitle());
                    provider.getItem(position).setType(ShoppingList.Type.READY);
                    holder.taskEdit.setVisibility(View.GONE);
                    holder.textView.setVisibility(View.VISIBLE);
                    Log.d(Constants.LOG_TAG, "Position " + position);
                }
            }
        });
        holder.taskEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) return false;
                else return false;
            }
        });
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
        return provider.getData().size();
    }
}