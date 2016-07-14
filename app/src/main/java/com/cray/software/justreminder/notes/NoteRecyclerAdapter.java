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
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private ColorSetter cs;
    private NoteDataProvider provider;
    private SimpleListener mEventListener;
    private int mTextSize;
    private boolean mEncrypt;

    public NoteRecyclerAdapter(Context context, NoteDataProvider provider) {
        this.provider = provider;
        mEncrypt = SharedPrefs.getInstance(context).getBoolean(Prefs.NOTE_ENCRYPT);
        mTextSize = SharedPrefs.getInstance(context).getInt(Prefs.TEXT_SIZE) + 12;
        cs = new ColorSetter(context);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
        public TextView textView;
        public ImageView noteImage;
        public CardView itemCard;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.note);
            noteImage = (ImageView) v.findViewById(R.id.noteImage);
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
                mEventListener.onItemClicked(getAdapterPosition(), noteImage);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), noteImage);
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final NoteModel item = provider.getData().get(position);
        String title = item.getNote();
        int color = item.getColor();
        int style = item.getStyle();
        byte[] byteImage = item.getImage();

        holder.textView.setTypeface(cs.getTypeface(style));
        holder.itemCard.setCardBackgroundColor(cs.getNoteLightColor(color));
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                holder.noteImage.setImageBitmap(photo);
            } else {
                holder.noteImage.setImageDrawable(null);
            }
        } else {
            holder.noteImage.setImageDrawable(null);
        }

        if (mEncrypt){
            title = SyncHelper.decrypt(title);
        }

        if (title.length() > 500) {
            String substring = title.substring(0, 500);
            title = substring + "...";
        }
        holder.textView.setText(title);
        holder.textView.setTextSize(mTextSize);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return provider.getData().get(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getData().size();
    }

    public void setEventListener(SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}