package com.cray.software.justreminder.adapters;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.datas.ReminderModel;
import com.cray.software.justreminder.datas.ShoppingList;
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
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.Calendar;
import java.util.Date;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private TimeCount mCount;
    private ColorSetter cs;
    private ReminderDataProvider provider;
    private RecyclerListener mEventListener;
    private boolean isDark;
    private boolean is24;
    private boolean isGrid;

    public RemindersRecyclerAdapter(Context context, ReminderDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        mCount = new TimeCount(context);
        isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);
        isGrid = prefs.loadBoolean(Prefs.LIST_GRID);
        setHasStableIds(true);
    }

    public static class ViewHolder1 extends RecyclerView.ViewHolder {
        private final TextView taskTitle;
        private final ViewGroup container;
        private final RelativeLayout background;
        private final LinearLayout todoList;
        private final LinearLayout subBackground, titleContainer;

        public ViewHolder1(View v) {
            super(v);
            todoList = (LinearLayout) v.findViewById(R.id.todoList);
            todoList.setVisibility(View.VISIBLE);
            background = (RelativeLayout) v.findViewById(R.id.background);
            subBackground = (LinearLayout) v.findViewById(R.id.subBackground);
            titleContainer = (LinearLayout) v.findViewById(R.id.titleContainer);
            container = (ViewGroup) v.findViewById(R.id.container);

            taskTitle = (TextView) v.findViewById(R.id.taskText);
            taskTitle.setText("");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView leftTime, taskTitle, taskDate, viewTime, reminder_type, reminder_phone,
                repeatInterval, reminder_contact_name;
        private final SwitchCompat check;
        private final ImageView taskIcon, leftTimeIcon;
        private final ViewGroup container;
        private final RelativeLayout background;

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
        RecyclerView.ViewHolder vh;
        if (viewType == ReminderDataProvider.VIEW_SHOPPING_LIST){
            vh = new ViewHolder1(itemLayoutView);
        } else {
            vh = new ViewHolder(itemLayoutView);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ReminderModel item = provider.getItem(position);

        if (getItemViewType(position) == ReminderDataProvider.VIEW_REMINDER){
            final ViewHolder viewHolder = (ViewHolder) holder;

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

            viewHolder.taskTitle.setText("");
            viewHolder.reminder_contact_name.setText("");
            viewHolder.taskDate.setText("");
            viewHolder.viewTime.setText("");
            viewHolder.reminder_type.setText("");
            viewHolder.reminder_phone.setText("");
            viewHolder.repeatInterval.setText("");

            viewHolder.background.setBackgroundResource(cs.getCardDrawableStyle());

            viewHolder.repeatInterval.setBackgroundResource(isDark ? R.drawable.round_view_white :
                    R.drawable.round_view_black);

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
                viewHolder.check.setChecked(false);
            } else {
                viewHolder.check.setChecked(true);
            }

            if (archived > 0) {
                viewHolder.check.setVisibility(View.GONE);
                viewHolder.leftTime.setVisibility(View.GONE);
                viewHolder.leftTimeIcon.setVisibility(View.GONE);
            }

            viewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (mEventListener != null) mEventListener.onItemSwitched(position, viewHolder.check);
                    else compoundButton.setChecked(!b);
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
                    if (mEventListener != null)
                        mEventListener.onItemLongClicked(position, viewHolder.check);
                    return true;
                }
            });
        } else {
            final ViewHolder1 viewHolder1 = (ViewHolder1) holder;
            viewHolder1.background.setBackgroundResource(cs.getCardDrawableStyle());
            viewHolder1.subBackground.setBackgroundColor(ViewUtils.getColor(mContext, cs.getCategoryColor(item.getCatColor())));

            String title = item.getTitle();
            viewHolder1.taskTitle.setText(title);

            viewHolder1.taskTitle.setTextColor(ViewUtils.getColor(mContext, R.color.colorBlack));
            if (title.matches("")) viewHolder1.titleContainer.setVisibility(View.GONE);
            else viewHolder1.titleContainer.setVisibility(View.VISIBLE);

            viewHolder1.todoList.setFocusableInTouchMode(false);
            viewHolder1.todoList.setFocusable(false);

            ShoppingListDataProvider provider = new ShoppingListDataProvider(mContext, item.getId(), ShoppingList.ACTIVE);
            int count = 0;
            for (ShoppingList list : provider.getData()){
                View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_task_item_widget, null, false);
                ImageView checkView = (ImageView) view.findViewById(R.id.checkView);
                TextView textView = (TextView) view.findViewById(R.id.shopText);
                checkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mEventListener != null)
                            mEventListener.onItemClicked(position, viewHolder1.subBackground);
                    }
                });
                textView.setTextColor(ViewUtils.getColor(mContext, R.color.colorBlack));
                if (list.isChecked() == 1) {
                    checkView.setImageResource(R.drawable.ic_check_box_black_24dp);
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    checkView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
                    textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                count++;
                if (count == 9) {
                    checkView.setVisibility(View.INVISIBLE);
                    textView.setText("...");
                    viewHolder1.todoList.addView(view);
                    break;
                } else {
                    checkView.setVisibility(View.VISIBLE);
                    textView.setText(list.getTitle());
                    viewHolder1.todoList.addView(view);
                }
            }

            viewHolder1.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mEventListener != null) mEventListener.onItemClicked(position, viewHolder1.subBackground);
                }
            });

            viewHolder1.container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (mEventListener != null)
                        mEventListener.onItemLongClicked(position, viewHolder1.subBackground);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return provider.getItem(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return provider.getItem(position).getId();
    }

    @Override
    public int getItemCount() {
        return provider.getCount();
    }

    public RecyclerListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }
}