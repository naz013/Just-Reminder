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
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.utils.ViewUtils;

/**
 * Recycler view adapter for Shopping list.
 */
public class TaskListRecyclerAdapter extends RecyclerView.Adapter<TaskListRecyclerAdapter.ViewHolder> {

    /**
     * Application context field.
     */
    private Context mContext;

    /**
     * Provider for Shopping list.
     */
    private ShoppingListDataProvider provider;

    /**
     * Application theme flag.
     */
    private boolean isDark;

    /**
     * Action listener for adapter.
     */
    private ActionListener listener;

    /**
     * Adapter constructor.
     * @param context application context.
     * @param provider data provider.
     * @param listener action listener.
     */
    public TaskListRecyclerAdapter(final Context context, final ShoppingListDataProvider provider,
                                   final ActionListener listener) {
        this.mContext = context;
        this.provider = provider;
        this.listener = listener;
        isDark = new SharedPrefs(context).loadBoolean(Prefs.USE_DARK_THEME);
        setHasStableIds(true);
    }

    /**
     * View holder for adapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Title text view.
         */
        TextView textView;

        /**
         * Background container.
         */
        RelativeLayout background;

        /**
         * Item status checkbox.
         */
        CheckBox itemCheck;

        /**
         * Remove item button.
         */
        ImageButton clearButton;

        /**
         * Holder constructor.
         * @param v view.
         */
        public ViewHolder(final View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.shopText);
            clearButton = (ImageButton) v.findViewById(R.id.clearButton);
            background = (RelativeLayout) v.findViewById(R.id.background);
            itemCheck = (CheckBox) v.findViewById(R.id.itemCheck);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
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
                holder.clearButton.setImageResource(R.drawable.ic_clear_black_24dp);
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
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public long getItemId(final int position) {
        return provider.getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getCount();
    }

    /**
     * Get data provider.
     * @return data provider.
     */
    public ShoppingListDataProvider getProvider(){
        return provider;
    }

    /**
     * Action listener interface.
     */
    public interface ActionListener{

        /**
         * On list item click action.
         * @param position item position.
         * @param isChecked item status.
         */
        void onItemCheck(int position, boolean isChecked);

        /**
         * On item delete button click.
         * @param position item position.
         */
        void onItemDelete(int position);

        /**
         * On item changed action.
         * @param position item position.
         */
        void onItemChange(int position);
    }
}
