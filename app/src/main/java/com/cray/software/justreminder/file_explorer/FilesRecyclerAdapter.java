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

package com.cray.software.justreminder.file_explorer;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.contacts.RecyclerClickListener;
import com.cray.software.justreminder.databinding.FileListItemBinding;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class FilesRecyclerAdapter extends RecyclerView.Adapter<FilesRecyclerAdapter.ContactViewHolder> {

    private Context mContext;
    private List<FileDataItem> mDataList;

    private RecyclerClickListener mListener;

    public FilesRecyclerAdapter(Context context, List<FileDataItem> dataItemList, RecyclerClickListener listener) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ContactViewHolder(DataBindingUtil.inflate(inflater, R.layout.file_list_item, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        FileDataItem item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {
        FileListItemBinding binding;
        public ContactViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.setClick(view -> {
                if (mListener != null) {
                    mListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    public FileDataItem getItem(int position) {
        return mDataList.get(position);
    }

    public FileDataItem removeItem(int position) {
        final FileDataItem model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, FileDataItem model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final FileDataItem model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<FileDataItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<FileDataItem> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final FileDataItem model = mDataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<FileDataItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final FileDataItem model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<FileDataItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final FileDataItem model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    @BindingAdapter("loadImage")
    public static void loadImage(ImageView imageView, int v) {
        boolean isDark = new ColorSetter(imageView.getContext()).isDark();
        Picasso.with(imageView.getContext())
                .load(v)
                .resize(100, 100)
                .placeholder(isDark ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_insert_drive_file_black_24dp)
                .error(isDark ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_insert_drive_file_black_24dp)
                .transform(new CropCircleTransformation())
                .into(imageView);
    }
}
