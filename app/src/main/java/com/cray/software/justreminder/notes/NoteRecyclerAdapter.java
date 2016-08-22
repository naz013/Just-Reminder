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

package com.cray.software.justreminder.notes;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.FilterCallback;
import com.cray.software.justreminder.databinding.ItemNoteLayoutBinding;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SimpleListener;

import java.util.ArrayList;
import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private List<NoteItem> mDataList;
    private SimpleListener mEventListener;
    private FilterCallback mCallback;

    private boolean isEncrypted = false;

    public NoteRecyclerAdapter(Context context, List<NoteItem> list, FilterCallback callback) {
        this.mDataList = list;
        this.mCallback = callback;
        isEncrypted = SharedPrefs.getInstance(context).getBoolean(Prefs.NOTE_ENCRYPT);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ItemNoteLayoutBinding binding;

        public ViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
            binding.setClick(v1 -> {
                if (mEventListener != null) {
                    mEventListener.onItemClicked(getAdapterPosition(), v1);
                }
            });
            binding.noteClick.setOnLongClickListener(view -> {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(getAdapterPosition(), view);
                }
                return false;
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(DataBindingUtil.inflate(inflater, R.layout.item_note_layout, parent, false).getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final NoteItem item = mDataList.get(position);
        holder.binding.setNote(item);
    }

    public NoteItem getItem(int position) {
        return mDataList.get(position);
    }

    public void filter(String q, List<NoteItem> list) {
        List<NoteItem> res = filter(list, q);
        animateTo(res);
        if (mCallback != null) mCallback.filter(res.size());
    }

    private List<NoteItem> filter(List<NoteItem> mData, String q) {
        q = q.toLowerCase();
        if (mData == null) mData = new ArrayList<>();
        List<NoteItem> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<NoteItem> getFiltered(List<NoteItem> models, String query) {
        List<NoteItem> list = new ArrayList<>();
        for (NoteItem model : models) {
            String text = model.getNote();
            if (isEncrypted) text = SyncHelper.decrypt(text);
            if (text.toLowerCase().contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    public NoteItem remove(int position) {
        final NoteItem model = mDataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, NoteItem model) {
        mDataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final NoteItem model = mDataList.remove(fromPosition);
        mDataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<NoteItem> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<NoteItem> newModels) {
        for (int i = mDataList.size() - 1; i >= 0; i--) {
            final NoteItem model = mDataList.get(i);
            if (!newModels.contains(model)) {
                remove(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<NoteItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final NoteItem model = newModels.get(i);
            if (!mDataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<NoteItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final NoteItem model = newModels.get(toPosition);
            final int fromPosition = mDataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
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

    public void setEventListener(SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}