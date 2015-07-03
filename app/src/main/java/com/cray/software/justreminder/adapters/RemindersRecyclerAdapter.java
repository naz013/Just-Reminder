package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.async.DisableAsync;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.ReminderDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.DelayReceiver;
import com.cray.software.justreminder.services.MonthDayReceiver;
import com.cray.software.justreminder.services.PositionDelayReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.Utils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import java.util.Calendar;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder>
        implements SwipeableItemAdapter<RemindersRecyclerAdapter.ViewHolder> {

    Context mContext;
    DataBase DB;
    TimeCount mCount;
    Contacts mContacts;
    SharedPrefs prefs;
    ColorSetter cs;
    ReminderDataProvider provider;
    Typeface typeface;
    private EventListener mEventListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;

    public RemindersRecyclerAdapter(Context context, ReminderDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        DB = new DataBase(context);
        mContacts = new Contacts(context);
        prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        mCount = new TimeCount(context);
        typeface = Utils.getLightTypeface(context);

        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeableViewContainerClick(v);
            }
        };
        setHasStableIds(true);
    }

    private void onSwipeableViewContainerClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(
                    RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false);  // false --- not pinned
        }
    }

    public interface EventListener {
        void onItemClicked(int position);

        void onItemLongClicked(int position);

        void toggleItem(int position);

        void onItemViewClicked(View v, boolean isPinned);
    }

    public static class ViewHolder extends AbstractSwipeableItemViewHolder {

        TextView leftTime, taskTitle, taskDate, viewTime, reminder_type, reminder_phone,
                repeatInterval, reminder_contact_name;
        //FrameLayout card;
        CheckBox check;
        ImageView taskIcon, leftTimeIcon;
        ViewGroup container;

        public ViewHolder(View v) {
            super(v);
            //card = (FrameLayout) v.findViewById(R.id.card);
            leftTime = (TextView) v.findViewById(R.id.remainingTime);
            check = (CheckBox) v.findViewById(R.id.itemCheck);
            check.setVisibility(View.VISIBLE);
            taskIcon = (ImageView) v.findViewById(R.id.taskIcon);
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
                .inflate(R.layout.list_item_card, parent, false);

        // create ViewHolder

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        boolean mDark = prefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        boolean is24 = prefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT);
        DB.open();

        ReminderDataProvider.ReminderItem item = provider.getData().get(position);
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
        holder.container.setBackgroundColor(cs.getCardStyle());

        // (if the item is *pinned*, click event comes to the mContainer)
        holder.container.setOnClickListener(mSwipeableViewContainerOnClickListener);

        // set swiping properties
        holder.setSwipeItemSlideAmount(
                item.isPinnedToSwipeLeft() ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT : 0);

        holder.repeatInterval.setBackgroundResource(mDark ? R.drawable.round_view_white :
                R.drawable.round_view_black);

        if (isDone == 1){
            holder.check.setChecked(true);
        } else {
            holder.check.setChecked(false);
        }

        if (!type.startsWith(Constants.TYPE_WEEKDAY)) {
            if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL)) {
                holder.reminder_phone.setText(number);
                holder.reminder_type.setText(mContext.getString(R.string.reminder_make_call));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_REMINDER) || type.matches(Constants.TYPE_TIME)) {
                holder.reminder_type.setText(mContext.getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_LOCATION)) {
                holder.reminder_type.setText(mContext.getString(R.string.reminder_type));
            } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE)) {
                holder.reminder_phone.setText(number);
                holder.reminder_type.setText(mContext.getString(R.string.reminder_send_message));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.startsWith(Constants.TYPE_SKYPE)){
                holder.reminder_phone.setText(number);
                if (type.matches(Constants.TYPE_SKYPE)){
                    holder.reminder_type.setText(mContext.getString(R.string.skype_call_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_VIDEO)){
                    holder.reminder_type.setText(mContext.getString(R.string.skype_video_type_title));
                } else if (type.matches(Constants.TYPE_SKYPE_CHAT)){
                    holder.reminder_type.setText(mContext.getString(R.string.skype_chat_type_title));
                }
                holder.reminder_contact_name.setText(number);
            } else if (type.matches(Constants.TYPE_APPLICATION)){
                PackageManager packageManager = mContext.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {}
                final String name = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                holder.reminder_phone.setText(number);
                holder.reminder_type.setText(mContext.getString(R.string.reminder_type_application));
                holder.reminder_contact_name.setText(name);
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)){
                holder.reminder_phone.setText(number);
                holder.reminder_type.setText(mContext.getString(R.string.reminder_type_open_link));
                holder.reminder_contact_name.setText(number);
            }

            holder.taskIcon.setImageDrawable(Utils.getDrawable(mContext, cs.getCategoryIndicator(categoryColor)));

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
                if (type.startsWith(Constants.TYPE_LOCATION)){
                    holder.leftTime.setVisibility(View.GONE);
                    holder.repeatInterval.setVisibility(View.GONE);
                } else {
                    holder.leftTime.setVisibility(View.GONE);
                    holder.repeatInterval.setText(repeat);
                }
            }

            holder.taskTitle.setText(title);

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
                } else {
                    holder.leftTime.setVisibility(View.GONE);
                }

                holder.taskDate.setText(dT[0]);
                holder.viewTime.setText(dT[1]);
            }
            DB.close();
        } else {
            holder.taskTitle.setText(title);

            holder.taskIcon.setImageDrawable(Utils.getDrawable(mContext, cs.getCategoryIndicator(categoryColor)));

            if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                holder. reminder_phone.setText(number);
                holder.reminder_type.setText(mContext.getString(R.string.reminder_make_call));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                holder.reminder_phone.setText(number);
                holder.reminder_type.setText(mContext.getString(R.string.reminder_send_message));
                String name = mContacts.getContactNameFromNumber(number, mContext);
                if (name != null) holder.reminder_contact_name.setText(name);
                else holder.reminder_contact_name.setText("");
            } else if (type.matches(Constants.TYPE_WEEKDAY)) {
                holder.reminder_type.setText(mContext.getString(R.string.reminder_type));
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
            } else {
                holder.leftTime.setVisibility(View.GONE);
            }
            holder.viewTime.setText(Utils.getTime(calendar.getTime(), is24));
        }
        if (isDone == 1){
            holder.leftTimeIcon.setImageDrawable(Utils.getDrawable(mContext, R.drawable.drawable_grey));
        }

        if (archived > 0) {
            holder.check.setVisibility(View.GONE);
            holder.leftTime.setVisibility(View.GONE);
            holder.leftTimeIcon.setVisibility(View.GONE);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEventListener != null) {
                    mEventListener.onItemClicked(position);
                }
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mEventListener != null) {
                    mEventListener.onItemLongClicked(position);
                }
                return true;
            }
        });

        holder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (prefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW)) {
                    if (mEventListener != null) {
                        mEventListener.toggleItem(position);
                    }
                } else {
                    if (mEventListener != null) {
                        mEventListener.onItemClicked(position);
                    }
                }
            }
        });

        DB.close();
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

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_LEFT |
                RecyclerViewSwipeManager.REACTION_CAN_SWIPE_RIGHT;
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            /*case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;*/
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public int onSwipeItem(ViewHolder holder, int position, int result) {
        Log.d(Constants.LOG_TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        switch (result) {
            // swipe right
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                if (provider.getData().get(position).isPinnedToSwipeLeft()) {
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

    private void editItem(int position){
        Intent intentId = new Intent(mContext, ReminderManager.class);
        long id = getItemId(position);
        if (id != 0) {
            intentId.putExtra(Constants.EDIT_ID, id);
            new AlarmReceiver().cancelAlarm(mContext, id);
            new WeekDayReceiver().cancelAlarm(mContext, id);
            new MonthDayReceiver().cancelAlarm(mContext, id);
            new DelayReceiver().cancelAlarm(mContext, id);
            new PositionDelayReceiver().cancelDelay(mContext, id);
            mContext.startActivity(intentId);
            new DisableAsync(mContext).execute();
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(ViewHolder holder, int position, int result, int reaction) {
        Log.d(Constants.LOG_TAG, "onPerformAfterSwipeReaction(position = " + position +
                ", result = " + result + ", reaction = " + reaction + ")");

        ReminderDataProvider.ReminderItem item = provider.getData().get(position);

        if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM) {
            provider.removeItem(position);
            notifyItemRemoved(position);
        } else if (reaction == RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION) {
            item.setPinnedToSwipeLeft(true);
            notifyItemChanged(position);
            editItem(position);
        } else {
            item.setPinnedToSwipeLeft(false);
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }
}