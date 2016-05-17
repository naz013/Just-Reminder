/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.apps;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.contacts.ContactData;
import com.cray.software.justreminder.databinding.ApplicationListItemBinding;

import java.util.ArrayList;
import java.util.List;

public class AppsRecyclerAdapter extends RecyclerView.Adapter<AppsRecyclerAdapter.ApplicationViewHolder> {

    private Context mContext;
    private List<AppData> mDataList;

    private RecyclerClickListener mListener;

    public AppsRecyclerAdapter(Context context, List<AppData> dataItemList, RecyclerClickListener listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
    }

    @Override
    public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ApplicationViewHolder(DataBindingUtil.inflate(inflater, R.layout.application_list_item, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ApplicationViewHolder holder, int position) {
        AppData item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public class ApplicationViewHolder extends RecyclerView.ViewHolder {

        ApplicationListItemBinding binding;

        public ApplicationViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.setClick(new AdapterListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public AppData removeItem(int position) {
        final AppData model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, AppData model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final AppData model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<AppData> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<AppData> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final AppData model = mDataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<AppData> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final AppData model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<AppData> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final AppData model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public AppData getItem(int position) {
        if (position < mDataList.size()) return mDataList.get(position);
        else return null;
    }

    @BindingAdapter("app:loadImage")
    public static void loadImage(ImageView imageView, Drawable v) {
        imageView.setImageDrawable(v);
    }
}
