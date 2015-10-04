package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.FileDataProvider;
import com.cray.software.justreminder.datas.FileItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.SwipeListener;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

public class FileRecyclerAdapter extends RecyclerView.Adapter<FileRecyclerAdapter.ViewHolder>
        implements SwipeableItemAdapter<FileRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private ColorSetter cs;
    private FileDataProvider provider;
    private SwipeListener mEventListener;
    private boolean isDark;

    public FileRecyclerAdapter(Context context, FileDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        setHasStableIds(true);
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder viewHolder, int position, int x, int y) {
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
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

        final FileItem item = provider.getItem(position);

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

        ViewGroup container;
        RelativeLayout background;
        TextView fileName, lastModified, task, type, number, date, time, repeat;

        public ViewHolder(View v) {
            super(v);
            container = (ViewGroup) v.findViewById(R.id.container);
            background = (RelativeLayout) v.findViewById(R.id.background);
            fileName = (TextView) v.findViewById(R.id.fileName);
            lastModified = (TextView) v.findViewById(R.id.lastModified);
            task = (TextView) v.findViewById(R.id.task);
            type = (TextView) v.findViewById(R.id.type);
            number = (TextView) v.findViewById(R.id.number);
            date = (TextView) v.findViewById(R.id.date);
            time = (TextView) v.findViewById(R.id.time);
            repeat = (TextView) v.findViewById(R.id.repeat);
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
                .inflate(R.layout.list_item_file, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.background.setBackgroundResource(cs.getCardDrawableStyle());

        final FileItem item = provider.getData().get(position);

        holder.task.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.time.setText(item.getTime());
        holder.repeat.setText(item.getRepeat());
        holder.lastModified.setText(item.getLastModified());
        holder.type.setText(item.getType());
        holder.number.setText(item.getNumber());
        holder.fileName.setText(item.getFileName());

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventListener != null) mEventListener.onItemClicked(position, holder.background);
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