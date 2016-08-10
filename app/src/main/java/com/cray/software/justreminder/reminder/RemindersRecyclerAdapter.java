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
package com.cray.software.justreminder.reminder;

import android.app.AlarmManager;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.contacts.FilterCallback;
import com.cray.software.justreminder.databinding.ReminderListItemBinding;
import com.cray.software.justreminder.databinding.ShoppingListItemBinding;
import com.cray.software.justreminder.datas.AdapterItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private static ColorSetter cs;
    private List<AdapterItem> mDataList;
    private RecyclerListener mEventListener;
    private FilterCallback mCallback;

    public RemindersRecyclerAdapter(Context context, List<AdapterItem> list, FilterCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
        mDataList = new ArrayList<>(list);
        cs = new ColorSetter(context);
        setHasStableIds(true);
    }

    public ReminderItem getItem(int position) {
        if (position < mDataList.size()) {
            return (ReminderItem) mDataList.get(position).getObject();
        } return null;
    }

    public void removeItem(int position) {
        if (position < mDataList.size()) {
            mDataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeRemoved(0, mDataList.size());
        }
    }

    public class ReminderHolder extends RecyclerView.ViewHolder {
        public RoboTextView listHeader;
        public CardView itemCard;
        public ReminderListItemBinding binding;

        public ReminderHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            listHeader = binding.listHeader;
            itemCard = binding.itemCard;
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }
            binding.itemCard.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
                }
                return true;
            });
            binding.reminderContainer.setBackgroundColor(cs.getCardStyle());
            binding.setClick(v1 -> {
                switch (v1.getId()) {
                    case R.id.itemCard:
                        if (mEventListener != null) {
                            mEventListener.onItemClicked(getAdapterPosition(), binding.itemCheck);
                        }
                        break;
                    case R.id.itemCheck:
                        if (mEventListener != null) {
                            mEventListener.onItemSwitched(getAdapterPosition(), v1);
                        }
                        break;
                }
            });
        }
    }

    public class ShoppingHolder extends RecyclerView.ViewHolder {
        public RoboTextView listHeader;
        public CardView itemCard;
        public ShoppingListItemBinding binding;

        public ShoppingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            listHeader = binding.listHeader;
            itemCard = binding.itemCard;
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }
            binding.itemCard.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
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

    private void initLabel(RoboTextView listHeader, int position) {
        ReminderItem item = (ReminderItem) mDataList.get(position).getObject();
        long due = item.getDateTime();
        String simpleDate = TimeUtil.getSimpleDate(due);
        int isDone = item.getStatus();
        ReminderItem prevItem = null;
        try {
            prevItem = (ReminderItem) mDataList.get(position - 1).getObject();
        } catch (ArrayIndexOutOfBoundsException e) {}
        if (isDone == 1 && position > 0 && (prevItem != null && prevItem.getStatus() == 0)) {
            simpleDate = mContext.getString(R.string.disabled);
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        } else if (isDone == 1 && position > 0 && (prevItem != null && prevItem.getStatus() == 1)) {
            listHeader.setVisibility(View.GONE);
        } else if (isDone == 1 && position == 0) {
            simpleDate = mContext.getString(R.string.disabled);
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        } else if (isDone == 0 && position > 0 && (prevItem != null && simpleDate.equals(TimeUtil.getSimpleDate(prevItem.getDateTime())))) {
            listHeader.setVisibility(View.GONE);
        } else {
            if (due <= 0 || due < (System.currentTimeMillis() - AlarmManager.INTERVAL_DAY)) {
                simpleDate = mContext.getString(R.string.permanent);
            } else {
                if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                    simpleDate = mContext.getString(R.string.today);
                } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                    simpleDate = mContext.getString(R.string.tomorrow);
                }
            }
            listHeader.setText(simpleDate);
            listHeader.setVisibility(View.VISIBLE);
        }
    }

    public void filter(String q, List<AdapterItem> list) {
        List<AdapterItem> res = filter(list, q);
        animateTo(res);
        if (mCallback != null) mCallback.filter(res.size());
    }

    private List<AdapterItem> filter(List<AdapterItem> mData, String q) {
        q = q.toLowerCase();
        if (mData == null) mData = new ArrayList<>();
        List<AdapterItem> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<AdapterItem> getFiltered(List<AdapterItem> models, String query) {
        List<AdapterItem> list = new ArrayList<>();
        for (AdapterItem model : models) {
            final String text = ((ReminderItem) model.getObject()).getSummary().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    public AdapterItem remove(int position) {
        final AdapterItem model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, AdapterItem model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final AdapterItem model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<AdapterItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<AdapterItem> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final AdapterItem model = mDataList.get(i);
            if (!newModels.contains(model)) {
                remove(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<AdapterItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final AdapterItem model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<AdapterItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final AdapterItem model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == AdapterItem.REMINDER) {
            View view = DataBindingUtil.inflate(inflater, R.layout.reminder_list_item, parent, false).getRoot();
            return new ReminderHolder(view);
        } else {
            View view = DataBindingUtil.inflate(inflater, R.layout.shopping_list_item, parent, false).getRoot();
            return new ShoppingHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ReminderItem item = (ReminderItem) mDataList.get(position).getObject();
        if (holder instanceof ReminderHolder) {
            ReminderHolder reminderHolder = (ReminderHolder) holder;
            reminderHolder.binding.setItem(item);
            initLabel(reminderHolder.listHeader, position);
        } else if (holder instanceof ShoppingHolder) {
            ShoppingHolder shoppingHolder = (ShoppingHolder) holder;
            shoppingHolder.binding.setItem(item);
            initLabel(shoppingHolder.listHeader, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mDataList.get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return ((ReminderItem) mDataList.get(position).getObject()).getId();
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }
}