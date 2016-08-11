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

package com.cray.software.justreminder.contacts;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databinding.ContactListItemBinding;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

public class ContactsRecyclerAdapter extends RecyclerView.Adapter<ContactsRecyclerAdapter.ContactViewHolder> {

    private Context mContext;
    private List<ContactData> mDataList;

    private RecyclerClickListener mListener;
    private FilterCallback mCallback;

    public ContactsRecyclerAdapter(Context context, List<ContactData> dataItemList, RecyclerClickListener listener, FilterCallback callback) {
        this.mContext = context;
        this.mDataList = new ArrayList<>(dataItemList);
        this.mListener = listener;
        this.mCallback = callback;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        return new ContactViewHolder(DataBindingUtil.inflate(inflater, R.layout.contact_list_item, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, int position) {
        ContactData item = mDataList.get(position);
        holder.binding.setItem(item);
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder {

        ContactListItemBinding binding;

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

    public void filter(String q, List<ContactData> list) {
        List<ContactData> res = filter(list, q);
        animateTo(res);
        if (mCallback != null) mCallback.filter(res.size());
    }

    private List<ContactData> filter(List<ContactData> mData, String q) {
        q = q.toLowerCase();
        List<ContactData> filteredModelList = new ArrayList<>();
        if (mData == null) mData = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<ContactData> getFiltered(List<ContactData> models, String query) {
        List<ContactData> list = new ArrayList<>();
        for (ContactData model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    public ContactData removeItem(int position) {
        final ContactData model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, ContactData model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ContactData model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<ContactData> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<ContactData> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final ContactData model = mDataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<ContactData> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ContactData model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<ContactData> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ContactData model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public ContactData getItem(int position) {
        if (position < mDataList.size()) return mDataList.get(position);
        else return null;
    }

    @BindingAdapter("loadImage")
    public static void loadImage(ImageView imageView, String v) {
        boolean isDark = ColorSetter.getInstance(imageView.getContext()).isDark();
        if (v == null) {
            imageView.setImageResource(isDark ? R.drawable.ic_perm_identity_white_24dp : R.drawable.ic_perm_identity_black_24dp);
            return;
        }
        Picasso.with(imageView.getContext())
                .load(Uri.parse(v))
                .resize(100, 100)
                .centerCrop()
                .placeholder(isDark ? R.drawable.ic_perm_identity_white_24dp : R.drawable.ic_perm_identity_black_24dp)
                .error(isDark ? R.drawable.ic_perm_identity_white_24dp : R.drawable.ic_perm_identity_black_24dp)
                .transform(new CropCircleTransformation())
                .into(imageView);
    }
}
