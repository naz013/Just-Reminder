package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.AssetsUtil;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import java.util.Calendar;
import java.util.Date;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder>
        implements SwipeableItemAdapter<RemindersRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private TimeCount mCount;
    private SharedPrefs prefs;
    private ColorSetter cs;
    private ReminderDataProvider provider;
    private Typeface typeface;
    private RecyclerListener mEventListener;
    private boolean isDark;
    private boolean isGrid;

    public RemindersRecyclerAdapter(Context context, ReminderDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        mCount = new TimeCount(context);
        typeface = AssetsUtil.getLightTypeface(context);
        isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        isGrid = prefs.loadBoolean(Prefs.LIST_GRID);
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

        final ReminderDataProvider.ReminderItem item = provider.getItem(position);

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

        TextView leftTime, taskTitle, taskDate, viewTime, reminder_type, reminder_phone,
                repeatInterval, reminder_contact_name;
        SwitchCompat check;
        ImageView taskIcon, leftTimeIcon;
        ViewGroup container;
        RelativeLayout background;

        public ViewHolder(View v) {
            super(v);
            leftTime = (TextView) v.findViewById(R.id.remainingTime);
            check = (SwitchCompat) v.findViewById(R.id.itemCheck);
            check.setVisibility(View.VISIBLE);
            taskIcon = (ImageView) v.findViewById(R.id.taskIcon);
            background = (RelativeLayout) v.findViewById(R.id.background);
            taskTitle = (TextView) v.findViewById(R.id.taskText);

            taskTitle.setText("");
            taskDate = (TextView) v.findViewById(R.id.taskDate);

            taskDate.setText("");
            viewTime = (TextView) v.findViewById(R.id.taskTime);

            viewTime.setText("");
            reminder_type = (TextView) v.findViewById(R.id.reminder_type);

            reminder_type.setText("");
            reminder_phone = (TextView) v.findViewById(R.id.reminder_phone);

            reminder_phone.setText("");
            repeatInterval = (TextView) v.findViewById(R.id.repeatInterval);

            repeatInterval.setText("");
            reminder_contact_name = (TextView) v.findViewById(R.id.reminder_contact_name);

            reminder_contact_name.setText("");
            leftTimeIcon = (ImageView) v.findViewById(R.id.leftTime);
            leftTimeIcon.setVisibility(View.VISIBLE);
            container = (ViewGroup) v.findViewById(R.id.container);
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
                .inflate(!isGrid ? R.layout.list_item_card : R.layout.grid_item_card, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        boolean mDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);

        final ReminderDataProvider.ReminderItem item = provider.getData().get(position);
        String title = item.getTitle();
        String type = item.getType();
        String number = item.getNumber();
        long due = item.getDue();
        double lat = item.getPlace()[0];
        double lon = item.getPlace()[1];
        int isDone = item.getCompleted();
        String repeat = item.getRepeat();
        int archived = item.getArchived();

        int categoryColor = item.getCatColor();

        holder.reminder_contact_name.setTypeface(typeface);
        holder.taskTitle.setTypeface(typeface);
        holder.taskDate.setTypeface(typeface);
        holder.viewTime.setTypeface(typeface);
        holder.reminder_type.setTypeface(typeface);
        holder.reminder_phone.setTypeface(typeface);
        holder.repeatInterval.setTypeface(typeface);
        holder.background.setBackgroundResource(cs.getCardDrawableStyle());

        holder.repeatInterval.setBackgroundResource(mDark ? R.drawable.round_view_white :
                R.drawable.round_view_black);

        if (isDone == 1){
            holder.check.setChecked(false);
        } else {
            holder.check.setChecked(true);
        }

        holder.taskIcon.setImageDrawable(ViewUtils.getDrawable(mContext, cs.getCategoryIndicator(categoryColor)));
        holder.taskTitle.setText(title);
        holder.reminder_type.setText(ReminderUtils.getTypeString(mContext, type));

        if (type.startsWith(Constants.TYPE_MONTHDAY)){
            if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                holder.reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                holder.reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            }

            holder.leftTimeIcon.setImageDrawable(mCount.
                    getDifference(due));
            holder.repeatInterval.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(due);
            Date mTime = calendar.getTime();
            holder.viewTime.setText(TimeUtil.getTime(mTime, is24));
            holder.taskDate.setText(TimeUtil.dateFormat.format(calendar.getTime()));
            if (isDone == 0) {
                String remaining = mCount.getRemaining(due);
                holder.leftTime.setText(remaining);
            }
        } else if (type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                holder. reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                holder.reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            }

            holder.leftTimeIcon.setImageDrawable(mCount.
                    getDifference(due));
            holder.repeatInterval.setVisibility(View.GONE);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(due);

            holder.taskDate.setText(repeat);
            if (isDone == 0) {
                String remaining = mCount.getRemaining(due);
                holder.leftTime.setText(remaining);
            }
            holder.viewTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
        } else {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                holder.reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                    type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                holder.reminder_phone.setText(number);
                String name = Contacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                holder.reminder_phone.setText(number);
                holder.reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)){
                PackageManager packageManager = mContext.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String name = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                holder.reminder_phone.setText(number);
                holder.reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                holder.reminder_phone.setText(number);
                holder.reminder_contact_name.setText(number);
            }

            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                    type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                    type.startsWith(Constants.TYPE_APPLICATION)) {
                holder.leftTimeIcon.setImageDrawable(mCount.
                        getDifference(due));
                holder.repeatInterval.setText(repeat);
            } else if (type.matches(Constants.TYPE_TIME)) {
                holder.leftTimeIcon.setImageDrawable(mCount.
                        getDifference(due));
                holder.repeatInterval.setText(repeat);
            } else {
                if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)){
                    holder.leftTimeIcon.setVisibility(View.GONE);
                    holder.repeatInterval.setVisibility(View.GONE);
                } else {
                    holder.leftTimeIcon.setVisibility(View.GONE);
                    holder.repeatInterval.setText(mContext.getString(R.string.interval_zero));
                }
            }

            String[] dT = mCount.
                    getNextDateTime(due);
            if (lat != 0.0 || lon != 0.0) {
                holder.taskDate.setText(String.format("%.5f", lat));
                holder.viewTime.setText(String.format("%.5f", lon));
                holder.leftTime.setVisibility(View.GONE);
            } else {
                if (isDone == 0) {
                    holder.leftTime.setText(mCount.
                            getRemaining(due));
                }

                holder.taskDate.setText(dT[0]);
                holder.viewTime.setText(dT[1]);
            }
        }
        if (isDone == 1){
            holder.leftTimeIcon.setImageDrawable(ViewUtils.getDrawable(mContext, R.drawable.drawable_grey));
            holder.leftTime.setVisibility(!isGrid ? View.GONE : View.INVISIBLE);
        }

        if (archived > 0) {
            holder.check.setVisibility(View.GONE);
            holder.leftTime.setVisibility(View.GONE);
            holder.leftTimeIcon.setVisibility(View.GONE);
        }

        if (prefs.loadBoolean(Prefs.ANIMATIONS)) {
            holder.leftTimeIcon.setVisibility(View.GONE);
            holder.repeatInterval.setVisibility(View.GONE);
            holder.taskIcon.setVisibility(View.GONE);
            ViewUtils.zoom(holder.taskIcon, position, 1);
            boolean prev = false;
            if (type.matches(Constants.TYPE_CALL) || type.startsWith(Constants.TYPE_APPLICATION) ||
                    type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_TIME) ||
                    type.startsWith(Constants.TYPE_SKYPE) || type.matches(Constants.TYPE_REMINDER)) {
                ViewUtils.zoom(holder.repeatInterval, position, 2);
                prev = true;
            }
            if (archived == 0) {
                ViewUtils.zoom(holder.leftTimeIcon, position, prev ? 3 : 2);
            }
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventListener != null) mEventListener.onItemClicked(position, holder.check);
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null) mEventListener.onItemLongClicked(position);
                return true;
            }
        });

        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mEventListener != null) mEventListener.onItemSwitched(position, holder.check);
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

    public RecyclerListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }
}