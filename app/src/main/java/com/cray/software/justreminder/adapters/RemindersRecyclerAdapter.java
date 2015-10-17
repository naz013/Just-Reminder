package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
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
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.LegacySwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import java.util.Calendar;
import java.util.Date;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements LegacySwipeableItemAdapter<RecyclerView.ViewHolder> {

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
    public int onGetSwipeReactionType(RecyclerView.ViewHolder viewHolder, int position, int x, int y) {
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(RecyclerView.ViewHolder viewHolder, int position, int type) {
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
    public int onSwipeItem(RecyclerView.ViewHolder holder, int position, int result) {
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
    public void onPerformAfterSwipeReaction(RecyclerView.ViewHolder holder, int position, int result, int reaction) {
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

    public static class ViewHolder1 extends AbstractSwipeableItemViewHolder {

        TextView taskTitle;
        ViewGroup container;
        RelativeLayout background;
        RecyclerView todoList;
        LinearLayout subBackground, titleContainer;

        public ViewHolder1(View v) {
            super(v);
            todoList = (RecyclerView) v.findViewById(R.id.todoList);
            todoList.setVisibility(View.VISIBLE);
            background = (RelativeLayout) v.findViewById(R.id.background);
            subBackground = (LinearLayout) v.findViewById(R.id.subBackground);
            titleContainer = (LinearLayout) v.findViewById(R.id.titleContainer);
            container = (ViewGroup) v.findViewById(R.id.container);

            taskTitle = (TextView) v.findViewById(R.id.taskText);
            taskTitle.setText("");
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
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
            background = (RelativeLayout) v.findViewById(R.id.background);
            container = (ViewGroup) v.findViewById(R.id.container);

            taskTitle = (TextView) v.findViewById(R.id.taskText);
            taskTitle.setText("");
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(!isGrid ? R.layout.list_item_card : R.layout.grid_item_card, parent, false);
        if (viewType == ReminderDataProvider.VIEW_SHOPPING_LIST) {
            itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_card_with_list, parent, false);
        }

        // create ViewHolder
        if (viewType == ReminderDataProvider.VIEW_SHOPPING_LIST){
            return new ViewHolder1(itemLayoutView);
        } else {
            return new ViewHolder(itemLayoutView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        boolean is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);

        final ReminderDataProvider.ReminderItem item = provider.getData().get(position);

        if (getItemViewType(position) == ReminderDataProvider.VIEW_REMINDER){
            final ViewHolder viewHolder = (ViewHolder) holder;

            String title = item.getTitle();
            viewHolder.taskTitle.setTypeface(typeface);

            String type = item.getType();
            String number = item.getNumber();
            long due = item.getDue();
            double lat = item.getPlace()[0];
            double lon = item.getPlace()[1];
            int isDone = item.getCompleted();
            String repeat = item.getRepeat();
            int archived = item.getArchived();

            int categoryColor = item.getCatColor();

            viewHolder.reminder_contact_name.setTypeface(typeface);
            viewHolder.taskDate.setTypeface(typeface);
            viewHolder.viewTime.setTypeface(typeface);
            viewHolder.reminder_type.setTypeface(typeface);
            viewHolder.reminder_phone.setTypeface(typeface);
            viewHolder.repeatInterval.setTypeface(typeface);
            viewHolder.background.setBackgroundResource(cs.getCardDrawableStyle());

            viewHolder.repeatInterval.setBackgroundResource(isDark ? R.drawable.round_view_white :
                    R.drawable.round_view_black);

            if (isDone == 1) {
                viewHolder.check.setChecked(false);
            } else {
                viewHolder.check.setChecked(true);
            }

            viewHolder.taskIcon.setImageDrawable(ViewUtils.getDrawable(mContext, cs.getCategoryIndicator(categoryColor)));
            viewHolder.taskTitle.setText(title);
            viewHolder.reminder_type.setText(ReminderUtils.getTypeString(mContext, type));

            if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                if (type.startsWith(Constants.TYPE_MONTHDAY_CALL)) {
                    viewHolder.reminder_phone.setText(number);
                    String name = Contacts.getContactNameFromNumber(number, mContext);
                    if (name != null) viewHolder.reminder_contact_name.setText(name);
                    else viewHolder.reminder_contact_name.setText("");
                } else if (type.startsWith(Constants.TYPE_MONTHDAY_MESSAGE)) {
                    viewHolder.reminder_phone.setText(number);
                    String name = Contacts.getContactNameFromNumber(number, mContext);
                    if (name != null) viewHolder.reminder_contact_name.setText(name);
                    else viewHolder.reminder_contact_name.setText("");
                }

                viewHolder.leftTimeIcon.setImageDrawable(mCount.
                        getDifference(due));
                viewHolder.repeatInterval.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(due);
                Date mTime = calendar.getTime();
                viewHolder.viewTime.setText(TimeUtil.getTime(mTime, is24));
                viewHolder.taskDate.setText(TimeUtil.dateFormat.format(calendar.getTime()));
                if (isDone == 0) {
                    String remaining = mCount.getRemaining(due);
                    viewHolder.leftTime.setText(remaining);
                }
            } else if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                    viewHolder.reminder_phone.setText(number);
                    String name = Contacts.getContactNameFromNumber(number, mContext);
                    if (name != null) viewHolder.reminder_contact_name.setText(name);
                    else viewHolder.reminder_contact_name.setText("");
                } else if (type.matches(Constants.TYPE_WEEKDAY_MESSAGE)) {
                    viewHolder.reminder_phone.setText(number);
                    String name = Contacts.getContactNameFromNumber(number, mContext);
                    if (name != null) viewHolder.reminder_contact_name.setText(name);
                    else viewHolder.reminder_contact_name.setText("");
                }

                viewHolder.leftTimeIcon.setImageDrawable(mCount.
                        getDifference(due));
                viewHolder.repeatInterval.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(due);

                viewHolder.taskDate.setText(repeat);
                if (isDone == 0) {
                    String remaining = mCount.getRemaining(due);
                    viewHolder.leftTime.setText(remaining);
                }
                viewHolder.viewTime.setText(TimeUtil.getTime(calendar.getTime(), is24));
            } else {
                if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_LOCATION_CALL) ||
                        type.matches(Constants.TYPE_LOCATION_OUT_CALL)) {
                    viewHolder.reminder_phone.setText(number);
                    String name = Contacts.getContactNameFromNumber(number, mContext);
                    if (name != null) viewHolder.reminder_contact_name.setText(name);
                    else viewHolder.reminder_contact_name.setText("");
                } else if (type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_LOCATION_MESSAGE) ||
                        type.matches(Constants.TYPE_LOCATION_OUT_MESSAGE)) {
                    viewHolder.reminder_phone.setText(number);
                    String name = Contacts.getContactNameFromNumber(number, mContext);
                    if (name != null) viewHolder.reminder_contact_name.setText(name);
                    else viewHolder.reminder_contact_name.setText("");
                } else if (type.startsWith(Constants.TYPE_SKYPE)) {
                    viewHolder.reminder_phone.setText(number);
                    viewHolder.reminder_contact_name.setText(number);
                } else if (type.matches(Constants.TYPE_APPLICATION)) {
                    PackageManager packageManager = mContext.getPackageManager();
                    ApplicationInfo applicationInfo = null;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(number, 0);
                    } catch (final PackageManager.NameNotFoundException ignored) {
                    }
                    final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                    viewHolder.reminder_phone.setText(number);
                    viewHolder.reminder_contact_name.setText(name);
                } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                    viewHolder.reminder_phone.setText(number);
                    viewHolder.reminder_contact_name.setText(number);
                }

                if (type.matches(Constants.TYPE_CALL) || type.matches(Constants.TYPE_MESSAGE) ||
                        type.matches(Constants.TYPE_REMINDER) || type.startsWith(Constants.TYPE_SKYPE) ||
                        type.startsWith(Constants.TYPE_APPLICATION)) {
                    viewHolder.leftTimeIcon.setImageDrawable(mCount.
                            getDifference(due));
                    viewHolder.repeatInterval.setText(repeat);
                } else if (type.matches(Constants.TYPE_TIME)) {
                    viewHolder.leftTimeIcon.setImageDrawable(mCount.
                            getDifference(due));
                    viewHolder.repeatInterval.setText(repeat);
                } else {
                    if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                        viewHolder.leftTimeIcon.setVisibility(View.GONE);
                        viewHolder.repeatInterval.setVisibility(View.GONE);
                    } else {
                        viewHolder.leftTimeIcon.setVisibility(View.GONE);
                        viewHolder.repeatInterval.setText(mContext.getString(R.string.interval_zero));
                    }
                }

                String[] dT = mCount.
                        getNextDateTime(due);
                if (lat != 0.0 || lon != 0.0) {
                    viewHolder.taskDate.setText(String.format("%.5f", lat));
                    viewHolder.viewTime.setText(String.format("%.5f", lon));
                    if (archived > 0) viewHolder.leftTime.setVisibility(View.GONE);
                    else viewHolder.leftTime.setVisibility(View.GONE);
                } else {
                    if (isDone == 0) {
                        viewHolder.leftTime.setText(mCount.
                                getRemaining(due));
                    }

                    viewHolder.taskDate.setText(dT[0]);
                    viewHolder.viewTime.setText(dT[1]);
                }
            }
            if (isDone == 1) {
                viewHolder.leftTimeIcon.setImageDrawable(ViewUtils.getDrawable(mContext, R.drawable.drawable_grey));
                viewHolder.leftTime.setVisibility(View.GONE);
            }

            if (archived > 0) {
                viewHolder.check.setVisibility(View.GONE);
                viewHolder.leftTime.setVisibility(View.GONE);
                viewHolder.leftTimeIcon.setVisibility(View.GONE);
            }

            if (prefs.loadBoolean(Prefs.ANIMATIONS)) {
                viewHolder.leftTimeIcon.setVisibility(View.GONE);
                viewHolder.repeatInterval.setVisibility(View.GONE);
                viewHolder.taskIcon.setVisibility(View.GONE);
                ViewUtils.zoom(viewHolder.taskIcon, position, 1);
                boolean prev = false;
                if (type.matches(Constants.TYPE_CALL) || type.startsWith(Constants.TYPE_APPLICATION) ||
                        type.matches(Constants.TYPE_MESSAGE) || type.matches(Constants.TYPE_TIME) ||
                        type.startsWith(Constants.TYPE_SKYPE) || type.matches(Constants.TYPE_REMINDER)) {
                    ViewUtils.zoom(viewHolder.repeatInterval, position, 2);
                    prev = true;
                }
                if (archived == 0) {
                    ViewUtils.zoom(viewHolder.leftTimeIcon, position, prev ? 3 : 2);
                }
            }

            viewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (mEventListener != null) mEventListener.onItemSwitched(position, viewHolder.check);
                }
            });

            viewHolder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mEventListener != null) mEventListener.onItemClicked(position, viewHolder.check);
                }
            });

            viewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mEventListener != null) mEventListener.onItemLongClicked(position);
                    return true;
                }
            });

            final int swipeState = viewHolder.getSwipeStateFlags();

            if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0) {
                int bgResId;

                if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_SWIPING) != 0) {
                    bgResId = R.drawable.bg_swipe_item_left;
                } else {
                    bgResId = R.color.colorWhite;
                }

                viewHolder.container.setBackgroundResource(bgResId);
            }

            // set swiping properties
            viewHolder.setSwipeItemSlideAmount(
                    item.isPinnedToSwipeLeft() ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
        } else {
            final ViewHolder1 viewHolder1 = (ViewHolder1) holder;
            viewHolder1.background.setBackgroundResource(cs.getCardDrawableStyle());
            viewHolder1.subBackground.setBackgroundColor(mContext.getResources().getColor(cs.getCategoryColor(item.getCatColor())));

            String title = item.getTitle();
            viewHolder1.taskTitle.setTypeface(typeface);
            viewHolder1.taskTitle.setText(title);

            viewHolder1.taskTitle.setTextColor(ViewUtils.getColor(mContext, R.color.colorBlack));
            if (title.matches("")) viewHolder1.titleContainer.setVisibility(View.GONE);
            else viewHolder1.titleContainer.setVisibility(View.VISIBLE);

            viewHolder1.todoList.setFocusableInTouchMode(false);
            viewHolder1.todoList.setFocusable(false);

            ShoppingListDataProvider provider = new ShoppingListDataProvider(mContext, item.getId());
            int size = provider.getCount();
            if (size > 8) size = 8;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder1.todoList.getLayoutParams();
            params.height = QuickReturnUtils.dp2px(mContext, size * 35);
            viewHolder1.todoList.setLayoutParams(params);

            Log.d(Constants.LOG_TAG, "Data size " + size);
            TaskListRecyclerAdapter shoppingAdapter = new TaskListRecyclerAdapter(mContext, provider, null);
            viewHolder1.todoList.setLayoutManager(new LinearLayoutManager(mContext));
            viewHolder1.todoList.setAdapter(shoppingAdapter);

            viewHolder1.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mEventListener != null) mEventListener.onItemClicked(position, viewHolder1.subBackground);
                }
            });

            viewHolder1.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mEventListener != null) mEventListener.onItemLongClicked(position);
                    return true;
                }
            });

            final int swipeState = viewHolder1.getSwipeStateFlags();

            if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_IS_UPDATED) != 0) {
                int bgResId;

                if ((swipeState & RecyclerViewSwipeManager.STATE_FLAG_SWIPING) != 0) {
                    bgResId = R.drawable.bg_swipe_item_left;
                } else {
                    bgResId = R.color.colorWhite;
                }

                viewHolder1.container.setBackgroundResource(bgResId);
            }

            // set swiping properties
            viewHolder1.setSwipeItemSlideAmount(
                    item.isPinnedToSwipeLeft() ? RecyclerViewSwipeManager.OUTSIDE_OF_THE_WINDOW_LEFT : 0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return provider.getItem(position).getViewType();
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