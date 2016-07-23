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

package com.cray.software.justreminder.groups;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;
import java.util.List;

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ViewHolder> {

    private ColorSetter cs;
    private List<GroupItem> mDataList;
    private SimpleListener mEventListener;

    public GroupsRecyclerAdapter(Context context, List<GroupItem> list) {
        this.mDataList = new ArrayList<>(list);
        cs = new ColorSetter(context);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {

        public RoboTextView textView;
        public CardView itemCard;
        public View indicator;

        public ViewHolder(View v) {
            super(v);
            textView = (RoboTextView) v.findViewById(R.id.textView);
            indicator = v.findViewById(R.id.indicator);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), indicator);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), indicator);
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_category_card, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final GroupItem item = mDataList.get(position);
        String title = item.getTitle();
        int indicator = item.getColor();
        holder.textView.setText(title);
        holder.indicator.setBackgroundResource(cs.getCategoryIndicator(indicator));
    }

    public GroupItem getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return mDataList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void setEventListener(SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}