package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.Note;
import com.cray.software.justreminder.datas.NoteDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SwipeListener;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.LegacySwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder>
        implements LegacySwipeableItemAdapter<NoteRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private NoteDataProvider provider;
    private SharedPrefs prefs;
    private SyncHelper syncHelper;
    private SwipeListener mEventListener;
    private boolean isDark;

    public NoteRecyclerAdapter(Context context, NoteDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        prefs = new SharedPrefs(context);
        syncHelper = new SyncHelper(context);
        cs = new ColorSetter(context);
        isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        setHasStableIds(true);
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder viewHolder, int position, int x, int y) {
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(ViewHolder viewHolder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                if (isDark) bgRes = R.drawable.bg_swipe_item_neutral_dark;
                else bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                if (isDark) bgRes = R.drawable.bg_swipe_item_left;
                else bgRes = R.drawable.bg_swipe_item_left_dark;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                if (isDark) bgRes = R.drawable.bg_swipe_item_right;
                else bgRes = R.drawable.bg_swipe_item_right_dark;
                break;
        }

        viewHolder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public int onSwipeItem(ViewHolder holder, int position, int result) {
        switch (result) {
            // swipe right
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                if (provider.getItem(position).isPinnedToSwipeLeft()) {
                    // pinned --- back to default position
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
                } else {
                    // not pinned --- remove
                    return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
                }
                // swipe left -- pin
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION;
            // other --- do nothing
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(ViewHolder holder, int position, int result, int reaction) {
        Log.d(Constants.LOG_TAG,
                "onPerformAfterSwipeReaction(position = " + position + ", result = " + result + ", reaction = " + reaction + ")");

        final Note item = provider.getItem(position);

        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            if (mEventListener != null) {
                mEventListener.onSwipeToRight(position);
            }
        } else if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION) {
            if (mEventListener != null) {
                mEventListener.onSwipeToLeft(position);
            }
        } else {
            if (item != null) item.setPinnedToSwipeLeft(false);
        }
    }

    public static class ViewHolder extends AbstractSwipeableItemViewHolder {

        TextView textView;
        ViewGroup container;
        LinearLayout noteBackground;
        ImageView noteImage;

        public ViewHolder(View v) {
            super(v);
            textView = (TextView) v.findViewById(R.id.note);
            container = (ViewGroup) v.findViewById(R.id.container);
            noteBackground = (LinearLayout) v.findViewById(R.id.noteBackground);
            noteImage = (ImageView) v.findViewById(R.id.noteImage);
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_note, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Note item = provider.getData().get(position);
        String title = item.getNote();
        int color = item.getColor();
        int style = item.getStyle();
        byte[] byteImage = item.getImage();

        holder.textView.setTypeface(cs.getTypeface(style));
        holder.noteBackground.setBackgroundColor(cs.getNoteLightColor(color));
        if (byteImage != null){
            Bitmap photo = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.length);
            if (photo != null){
                holder.noteImage.setImageBitmap(photo);
            } else holder.noteImage.setImageDrawable(null);
        } else holder.noteImage.setImageDrawable(null);

        if (prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            title = syncHelper.decrypt(title);
        }
        holder.textView.setText(title);
        holder.textView.setTextSize(prefs.loadInt(Prefs.TEXT_SIZE) + 12);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventListener != null) mEventListener.onItemClicked(position, holder.noteImage);
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null) mEventListener.onItemLongClicked(position);
                return true;
            }
        });

        final int swipeState = holder.getSwipeStateFlags();

        if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;

            if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_swipe_item_left;
            } else {
                bgResId = R.color.colorWhite;
            }

            holder.container.setBackgroundResource(bgResId);
        }

        // set swiping properties
        holder.setSwipeItemSlideAmount(
                item.isPinnedToSwipeLeft() ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
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

    public SwipeListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(SwipeListener eventListener) {
        mEventListener = eventListener;
    }
}