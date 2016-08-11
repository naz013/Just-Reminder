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

package com.cray.software.justreminder.templates;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;
import java.util.List;

public class SelectableRecyclerAdapter extends RecyclerView.Adapter<SelectableRecyclerAdapter.ViewHolder> {

    private ColorSetter cs;
    private List<TemplateItem> mDataList;
    private int selectedPosition = -1;

    public SelectableRecyclerAdapter(Context context, List<TemplateItem> list) {
        this.mDataList = new ArrayList<>(list);
        cs = ColorSetter.getInstance(context);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RoboTextView textView;
        public CardView itemCard;

        public ViewHolder(View v) {
            super(v);
            textView = (RoboTextView) v.findViewById(R.id.textView);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            selectItem(getAdapterPosition());
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public TemplateItem getItem(int position) {
        return mDataList.get(position);
    }

    public void selectItem(int position) {
        if (position == selectedPosition) return;
        if (selectedPosition != -1 && selectedPosition < mDataList.size()) {
            mDataList.get(selectedPosition).setSelected(false);
            notifyItemChanged(selectedPosition);
        }
        this.selectedPosition = position;
        if (position < mDataList.size()) {
            mDataList.get(position).setSelected(true);
            notifyItemChanged(position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_simple_card, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final TemplateItem item = mDataList.get(position);
        String title = item.getTitle();
        holder.textView.setText(title);
        if (item.getSelected()) {
            holder.itemCard.setCardBackgroundColor(cs.getColor(cs.colorAccent()));
        } else {
            holder.itemCard.setCardBackgroundColor(cs.getCardStyle());
        }
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
}