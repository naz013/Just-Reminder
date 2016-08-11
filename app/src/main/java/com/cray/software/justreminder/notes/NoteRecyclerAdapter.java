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
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databinding.ItemNoteLayoutBinding;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;

import java.util.List;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private static ColorSetter cs;
    private List<NoteItem> mDataList;
    private SimpleListener mEventListener;
    private static int mTextSize;
    private static boolean mEncrypt;

    public NoteRecyclerAdapter(Context context, List<NoteItem> list) {
        this.mDataList = list;
        mEncrypt = SharedPrefs.getInstance(context).getBoolean(Prefs.NOTE_ENCRYPT);
        mTextSize = SharedPrefs.getInstance(context).getInt(Prefs.TEXT_SIZE) + 12;
        cs = ColorSetter.getInstance(context);
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

    @BindingAdapter({"loadCard"})
    public static void loadCard(CardView cardView, int color) {
        cardView.setCardBackgroundColor(cs.getNoteLightColor(color));
        if (Module.isLollipop()) {
            cardView.setCardElevation(Configs.CARD_ELEVATION);
        }
    }

    @BindingAdapter({"loadNote"})
    public static void loadNote(TextView textView, NoteItem note) {
        String title = note.getNote();
        if (mEncrypt) {
            title = SyncHelper.decrypt(title);
        }
        if (title.length() > 500) {
            String substring = title.substring(0, 500);
            title = substring + "...";
        }
        textView.setText(title);
        textView.setTypeface(cs.getTypeface(note.getStyle()));
        textView.setTextSize(mTextSize);
    }

    @BindingAdapter({"loadImage"})
    public static void loadImage(ImageView imageView, byte[] image) {
        if (image != null) {
            Bitmap photo = BitmapFactory.decodeByteArray(image, 0, image.length);
            if (photo != null) {
                imageView.setImageBitmap(photo);
            } else {
                imageView.setImageDrawable(null);
            }
        } else {
            imageView.setImageDrawable(null);
        }
    }
}