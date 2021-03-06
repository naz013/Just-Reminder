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

package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.FileConfig;
import com.cray.software.justreminder.datas.models.FileItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;

/**
 * Recycler view adapter for backup files list.
 */
public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.ViewHolder> {

    /**
     * ColorSetter class field.
     */
    private ColorSetter cs;

    /**
     * List provider.
     */
    private ArrayList<FileItem> list;

    /**
     * Recycler view action listener.
     */
    private SimpleListener mEventListener;

    private boolean isDark;

    /**
     * Adapter constructor.
     * @param context application context.
     * @param list list of files in folder.
     */
    public FileRecyclerAdapter(final Context context, ArrayList<FileItem> list) {
        this.list = new ArrayList<>();
        this.list.clear();
        this.list = list;
        cs = ColorSetter.getInstance(context);
        setHasStableIds(true);
        isDark = cs.isDark();
    }

    /**
     * View holder class for Recycler adapter.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener, View.OnLongClickListener {

        /**
         * CardView field.
         */
        public CardView itemCard;

        /**
         * TextView fields
         */
        public RoboTextView fileName, lastModified;
        public ImageView fileIcon;

        /**
         * Ho;der constructor.
         * @param v view
         */
        public ViewHolder(final View v) {
            super(v);
            fileName = (RoboTextView) v.findViewById(R.id.fileName);
            fileIcon = (ImageView) v.findViewById(R.id.fileIcon);
            lastModified = (RoboTextView) v.findViewById(R.id.lastModified);
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), itemCard);
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
            }
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_file, parent, false);
        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FileItem item = list.get(position);
        holder.lastModified.setText(item.getLastModified());
        holder.fileName.setText(item.getFileName());
        holder.fileIcon.setImageResource(getIcon(item.getFileName()));
    }

    private int getIcon(String fileName) {
        if (fileName.endsWith(FileConfig.FILE_NAME_GROUP)) {
            if (isDark) return R.drawable.ic_local_offer_white_24dp;
            else return R.drawable.ic_local_offer_black_24dp;
        } else if (fileName.endsWith(FileConfig.FILE_NAME_REMINDER)) {
            if (isDark) return R.drawable.ic_notifications_white_24dp;
            else return R.drawable.ic_notifications_black_24dp;
        } else if (fileName.endsWith(FileConfig.FILE_NAME_NOTE)) {
            if (isDark) return R.drawable.ic_event_note_white_24dp;
            else return R.drawable.ic_event_note_black_24dp;
        } else if (fileName.endsWith(FileConfig.FILE_NAME_BIRTHDAY)) {
            if (isDark) return R.drawable.ic_cake_white_24dp;
            else return R.drawable.ic_cake_black_24dp;
        } else {
            if (isDark) return R.drawable.ic_insert_drive_file_white_24dp;
            else return R.drawable.ic_insert_drive_file_black_24dp;
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Set action listener for adapter.
     * @param eventListener action listener.
     */
    public void setEventListener(final SimpleListener eventListener) {
        mEventListener = eventListener;
    }
}
