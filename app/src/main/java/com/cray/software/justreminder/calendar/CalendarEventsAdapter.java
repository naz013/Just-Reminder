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

package com.cray.software.justreminder.calendar;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.birthdays.BirthdayItem;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.databinding.ListItemEventsBinding;
import com.cray.software.justreminder.databinding.ShoppingListItemBinding;
import com.cray.software.justreminder.databinding.TrashListItemBinding;
import com.cray.software.justreminder.datas.AdapterItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.ReminderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for today view fragment.
 */
public class CalendarEventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * List of events models.
     */
    private List<EventsItem> mDataList;

    /**
     * ColorSetter helper class field.
     */
    private ColorSetter cs;

    /**
     * RecyclerView action listener.
     */
    private SimpleListener mEventListener;

    /**
     * Adapter constructor.
     * @param context application context.
     */
    public CalendarEventsAdapter(final Context context, List<EventsItem> list) {
        mDataList = new ArrayList<>(list);
        cs = new ColorSetter(context);
    }

    /**
     * Set action listener for adapter.
     * @param listener action listener.
     */
    public final void setEventListener(final SimpleListener listener) {
        this.mEventListener = listener;
    }

    public class ReminderHolder extends RecyclerView.ViewHolder {

        public TrashListItemBinding binding;

        public ReminderHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                binding.itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }
            binding.itemCard.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), binding.itemCard);
                }
                return true;
            });
            binding.reminderContainer.setBackgroundColor(cs.getCardStyle());
            binding.setClick(v1 -> {
                switch (v1.getId()) {
                    case R.id.itemCard:
                        if (mEventListener != null) {
                            mEventListener.onItemClicked(getAdapterPosition(), binding.itemCard);
                        }
                        break;
                }
            });
        }
    }

    public class ShoppingHolder extends RecyclerView.ViewHolder {

        public ShoppingListItemBinding binding;

        public ShoppingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                binding.itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }
            binding.itemCard.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), binding.itemCard);
                }
                return true;
            });
            binding.subBackground.setBackgroundColor(cs.getCardStyle());
            binding.setClick(v1 -> {
                switch (v1.getId()) {
                    case R.id.itemCard:
                        if (mEventListener != null) {
                            mEventListener.onItemClicked(getAdapterPosition(), binding.subBackground);
                        }
                        break;
                }
            });
        }
    }

    public class BirthdayHolder extends RecyclerView.ViewHolder {

        ListItemEventsBinding binding;

        public BirthdayHolder(final View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                binding.itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }
            binding.itemCard.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), binding.itemCard);
                }
                return true;
            });
            binding.setClick(v1 -> {
                switch (v1.getId()) {
                    case R.id.itemCard:
                        if (mEventListener != null) {
                            mEventListener.onItemClicked(getAdapterPosition(), binding.itemCard);
                        }
                        break;
                }
            });
        }
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                               final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == AdapterItem.REMINDER) {
            View view = DataBindingUtil.inflate(inflater, R.layout.trash_list_item, parent, false).getRoot();
            return new ReminderHolder(view);
        } else if (viewType == AdapterItem.SHOPPING) {
            View view = DataBindingUtil.inflate(inflater, R.layout.shopping_list_item, parent, false).getRoot();
            return new ShoppingHolder(view);
        } else {
            View view = DataBindingUtil.inflate(inflater, R.layout.list_item_events, parent, false).getRoot();
            return new BirthdayHolder(view);
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof BirthdayHolder) {
            BirthdayItem item = (BirthdayItem) mDataList.get(position).getObject();
            BirthdayHolder birthdayHolder = (BirthdayHolder) holder;
            birthdayHolder.binding.setItem(item);
            birthdayHolder.binding.setColor(mDataList.get(position).getColor());
        } else if (holder instanceof ReminderHolder) {
            ReminderItem item = (ReminderItem) mDataList.get(position).getObject();
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.binding.setItem(item);
        } else if (holder instanceof ShoppingHolder) {
            ReminderItem item = (ReminderItem) mDataList.get(position).getObject();
            ShoppingHolder shoppingHolder = (ShoppingHolder) holder;
            shoppingHolder.binding.setItem(item);
        }
    }

    @Override
    public final long getItemId(final int position) {
        return 0;
    }

    @Override
    public final int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).getViewType();
    }
}
