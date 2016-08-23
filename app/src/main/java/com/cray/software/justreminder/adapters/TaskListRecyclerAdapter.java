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

package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingListItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboTextView;
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
        isDark = ColorSetter.getInstance(context).isDark();
        setHasStableIds(true);
    }

    /**
     * View holder for adapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * Title text view.
         */
        RoboTextView textView;

        /**
         * Background container.
         */
        RelativeLayout background;

        /**
         * Item status checkbox.
         */
        RoboCheckBox itemCheck;

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
            textView = (RoboTextView) v.findViewById(R.id.shopText);
            clearButton = (ImageButton) v.findViewById(R.id.clearButton);
            background = (RelativeLayout) v.findViewById(R.id.background);
            itemCheck = (RoboCheckBox) v.findViewById(R.id.itemCheck);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task_item_card, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ShoppingListItem item = provider.getItem(holder.getAdapterPosition());
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
                holder.clearButton.setImageResource(R.drawable.ic_clear_white_vector);
            } else {
                holder.clearButton.setImageResource(R.drawable.ic_clear_black_vector);
            }

            holder.itemCheck.setVisibility(View.VISIBLE);
            holder.clearButton.setVisibility(View.VISIBLE);
            holder.clearButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDelete(holder.getAdapterPosition());
                }
            });

            holder.textView.setOnClickListener(v -> {
                if (listener != null && item.getStatus() == ShoppingListItem.DELETED) {
                    listener.onItemChange(holder.getAdapterPosition());
                }
            });

            holder.itemCheck.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                if (listener != null) {
                    listener.onItemCheck(holder.getAdapterPosition(), isChecked1);
                }
            });
        }

        if (item.getStatus() == ShoppingListItem.DELETED){
            holder.textView.setTextColor(ViewUtils.getColor(mContext, R.color.redPrimaryDark));
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public long getItemId(final int position) {
        return 0;
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
