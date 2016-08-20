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

package com.cray.software.justreminder.theme;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databinding.PhotoListItemBinding;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.ArrayList;
import java.util.List;

public class ImagesRecyclerAdapter extends RecyclerView.Adapter<ImagesRecyclerAdapter.PhotoViewHolder> {

    private Context mContext;
    private List<ImageItem> mDataList;
    private int prevSelected = -1;
    private SelectListener mListener;

    public ImagesRecyclerAdapter(Context context, List<ImageItem> dataItemList, SelectListener listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
    }

    public void setPrevSelected(int prevSelected) {
        this.prevSelected = prevSelected;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new PhotoViewHolder(DataBindingUtil.inflate(inflater, R.layout.photo_list_item, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder holder, int position) {
        ImageItem item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        PhotoListItemBinding binding;
        public PhotoViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.container.setOnClickListener(view -> performClick(getAdapterPosition()));
            binding.container.setOnLongClickListener(view -> {
                if (mListener != null) {
                    mListener.onItemLongClicked(getAdapterPosition(), view);
                }
                return true;
            });
        }
    }

    public void addItems(List<ImageItem> list) {
        mDataList.addAll(list);
        notifyItemInserted(getItemCount() - list.size());
    }

    private void performClick(int position) {
        if (position == prevSelected) {
            mDataList.get(prevSelected).setSelected(false);
            notifyItemChanged(prevSelected);
            prevSelected = -1;
            SharedPrefs.getInstance(mContext).putString(Prefs.MAIN_IMAGE_PATH, "");
            SharedPrefs.getInstance(mContext).putInt(Prefs.MAIN_IMAGE_ID, -1);
            if (mListener != null) mListener.onImageSelected(false);
        } else {
            if (prevSelected != -1) {
                if (prevSelected >= getItemCount() && mListener != null) {
                    mListener.deselectOverItem(prevSelected);
                } else {
                    mDataList.get(prevSelected).setSelected(false);
                    notifyItemChanged(prevSelected);
                }
            }
            prevSelected = position;
            mDataList.get(position).setSelected(true);
            ImageItem item = mDataList.get(position);
            SharedPrefs.getInstance(mContext).putString(Prefs.MAIN_IMAGE_PATH, RetrofitBuilder.getImageLink(item.getId()));
            SharedPrefs.getInstance(mContext).putInt(Prefs.MAIN_IMAGE_ID, position);
            notifyItemChanged(position);
            if (mListener != null) mListener.onImageSelected(true);
        }
    }
}
