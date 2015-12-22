package com.cray.software.justreminder.adapters;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ReminderModel;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.RecurrHelper;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.utils.ViewUtils;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private TimeCount mCount;
    private ColorSetter cs;
    private ReminderDataProvider provider;
    private RecyclerListener mEventListener;
    private boolean isDark;
    private boolean is24;

    public RemindersRecyclerAdapter(Context context, ReminderDataProvider provider) {
        this.mContext = context;
        this.provider = provider;
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        mCount = new TimeCount(context);
        isDark = prefs.loadBoolean(Prefs.USE_DARK_THEME);
        is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public TextView leftTime, taskTitle, taskDate, reminder_type, reminder_phone,
                repeatInterval, reminder_contact_name, listHeader;
        public SwitchCompat check;
        public ImageView taskIcon, leftTimeIcon;
        public CardView itemCard;

        public TextView shoppingTitle, shoppingTime;
        public LinearLayout todoList;
        public LinearLayout subBackground, titleContainer;
        public RelativeLayout reminderContainer;

        public ViewHolder(View v) {
            super(v);
            reminderContainer = (RelativeLayout) v.findViewById(R.id.reminderContainer);
            leftTime = (TextView) v.findViewById(R.id.remainingTime);
            listHeader = (TextView) v.findViewById(R.id.listHeader);
            check = (SwitchCompat) v.findViewById(R.id.itemCheck);
            check.setVisibility(View.VISIBLE);
            taskIcon = (ImageView) v.findViewById(R.id.taskIcon);
            taskDate = (TextView) v.findViewById(R.id.taskDate);
            taskDate.setText("");
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

            taskTitle = (TextView) v.findViewById(R.id.taskText);
            shoppingTime = (TextView) v.findViewById(R.id.shoppingTime);
            shoppingTime.setVisibility(View.GONE);
            taskTitle.setText("");
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) itemCard.setCardElevation(5f);

            todoList = (LinearLayout) v.findViewById(R.id.todoList);
            todoList.setVisibility(View.VISIBLE);
            subBackground = (LinearLayout) v.findViewById(R.id.subBackground);
            titleContainer = (LinearLayout) v.findViewById(R.id.titleContainer);
            shoppingTitle = (TextView) v.findViewById(R.id.shoppingTitle);
            shoppingTitle.setText("");

            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEventListener != null)
                        mEventListener.onItemSwitched(getAdapterPosition(), check);
                }
            });

            repeatInterval.setBackgroundResource(isDark ? R.drawable.round_view_white :
                    R.drawable.round_view_black);
        }

        @Override
        public void onClick(View v) {
            View view = itemCard;
            if (provider.getItem(getAdapterPosition()).getViewType() == ReminderDataProvider.VIEW_REMINDER)
                view = check;
            if (mEventListener != null) mEventListener.onItemClicked(getAdapterPosition(), view);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null)
                mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_card, parent, false);

        // create ViewHolder
        ViewHolder vh = new ViewHolder(itemLayoutView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ReminderModel item = provider.getItem(position);
        String title = item.getTitle();
        String type = item.getType();
        String number = item.getNumber();
        long due = item.getDue();
        double lat = item.getPlace()[0];
        double lon = item.getPlace()[1];
        int isDone = item.getCompleted();
        String repeat = item.getRepeat();
        String exclusion = item.getExclusion();
        int archived = item.getArchived();
        int categoryColor = item.getCatColor();

        String simpleDate = TimeUtil.getSimpleDate(due);

        if (archived == 1){
            if (position > 0 && simpleDate.equals(TimeUtil.getSimpleDate(provider.getItem(position - 1).getDue()))) {
                holder.listHeader.setVisibility(View.GONE);
            } else {
                if (due == 0){
                    simpleDate = mContext.getString(R.string.permanent_reminders);
                } else {
                    if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis())))
                        simpleDate = mContext.getString(R.string._today);
                    else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY)))
                        simpleDate = mContext.getString(R.string._tomorrow);
                }
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            }
        } else {
            if (isDone == 1 && position > 0 && provider.getItem(position - 1).getCompleted() == 0){
                simpleDate = mContext.getString(R.string.simple_disabled);
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            } else if (isDone == 1 && position > 0 && provider.getItem(position - 1).getCompleted() == 1){
                holder.listHeader.setVisibility(View.GONE);
            } else if (isDone == 1 && position == 0){
                simpleDate = mContext.getString(R.string.simple_disabled);
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            } else if (isDone == 0 && position > 0 && simpleDate.equals(TimeUtil.getSimpleDate(provider.getItem(position - 1).getDue()))){
                holder.listHeader.setVisibility(View.GONE);
            } else {
                if (due <= 0 || due < (System.currentTimeMillis() - AlarmManager.INTERVAL_DAY)){
                    simpleDate = mContext.getString(R.string.permanent_reminders);
                } else {
                    if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis())))
                        simpleDate = mContext.getString(R.string._today);
                    else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY)))
                        simpleDate = mContext.getString(R.string._tomorrow);
                }
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            }
        }

        if (getItemViewType(position) == ReminderDataProvider.VIEW_REMINDER){

            holder.reminderContainer.setVisibility(View.VISIBLE);
            holder.subBackground.setVisibility(View.GONE);

            holder.taskTitle.setText("");
            holder.reminder_contact_name.setText("");
            holder.taskDate.setText("");
            holder.reminder_type.setText("");
            holder.reminder_phone.setText("");
            holder.repeatInterval.setText("");

            holder.taskIcon.setImageDrawable(ViewUtils.getDrawable(mContext, cs.getCategoryIndicator(categoryColor)));
            holder.taskTitle.setText(title);
            holder.reminder_type.setText(ReminderUtils.getTypeString(mContext, type));

            if (type.startsWith(Constants.TYPE_MONTHDAY)) {
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
                holder.taskDate.setText(TimeUtil.getFullDateTime(due, is24));

                if (isDone == 0) {
                    holder.leftTime.setText(mCount.getRemaining(due));
                }
            } else if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                if (type.matches(Constants.TYPE_WEEKDAY_CALL)) {
                    holder.reminder_phone.setText(number);
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
                if (isDone == 0) {
                    holder.leftTime.setText(mCount.getRemaining(due));
                }
                holder.taskDate.setText(TimeUtil.getFullDateTime(due, is24));
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
                } else if (type.startsWith(Constants.TYPE_SKYPE)) {
                    holder.reminder_phone.setText(number);
                    holder.reminder_contact_name.setText(number);
                } else if (type.matches(Constants.TYPE_APPLICATION)) {
                    PackageManager packageManager = mContext.getPackageManager();
                    ApplicationInfo applicationInfo = null;
                    try {
                        applicationInfo = packageManager.getApplicationInfo(number, 0);
                    } catch (final PackageManager.NameNotFoundException ignored) {
                    }
                    final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                    holder.reminder_phone.setText(number);
                    holder.reminder_contact_name.setText(name);
                } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
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
                    if (type.startsWith(Constants.TYPE_LOCATION) || type.startsWith(Constants.TYPE_LOCATION_OUT)) {
                        holder.leftTimeIcon.setVisibility(View.GONE);
                        holder.repeatInterval.setVisibility(View.GONE);
                    } else {
                        holder.leftTimeIcon.setVisibility(View.GONE);
                        holder.repeatInterval.setText(mContext.getString(R.string.interval_zero));
                    }
                }

                if (lat != 0.0 || lon != 0.0) {
                    holder.taskDate.setText(String.format("%.5f", lat) + "\n" + String.format("%.5f", lon));
                    holder.leftTime.setVisibility(View.GONE);
                } else {
                    if (isDone == 0) holder.leftTime.setText(mCount.getRemaining(due));
                    holder.taskDate.setText(TimeUtil.getFullDateTime(due, is24));
                }
            }

            if (type.matches(Constants.TYPE_TIME) && archived == 0){
                if (exclusion != null){
                    if (new RecurrHelper(exclusion).isRange()){
                        holder.leftTimeIcon.setVisibility(View.GONE);
                        holder.taskDate.setText(R.string.paused);
                    } else {
                        holder.leftTimeIcon.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (isDone == 1) {
                holder.leftTimeIcon.setImageDrawable(ViewUtils.getDrawable(mContext, R.drawable.drawable_grey));
                holder.leftTime.setVisibility(View.GONE);
                holder.check.setChecked(false);
            } else {
                holder.check.setChecked(true);
            }

            if (archived > 0) {
                holder.check.setVisibility(View.GONE);
                holder.leftTime.setVisibility(View.GONE);
                holder.leftTimeIcon.setVisibility(View.GONE);
            }
        } else {
            if (due > 0){
                holder.shoppingTime.setText(TimeUtil.getFullDateTime(due, is24));
                holder.shoppingTime.setVisibility(View.VISIBLE);
            } else {
                holder.shoppingTime.setVisibility(View.GONE);
            }

            holder.reminderContainer.setVisibility(View.GONE);
            holder.subBackground.setVisibility(View.VISIBLE);

            holder.itemCard.setCardBackgroundColor(ViewUtils.getColor(mContext, cs.getCategoryColor(categoryColor)));

            holder.shoppingTitle.setText(title);

            holder.shoppingTitle.setTextColor(ViewUtils.getColor(mContext, R.color.blackPrimary));
            if (title.matches("")) holder.titleContainer.setVisibility(View.GONE);
            else holder.titleContainer.setVisibility(View.VISIBLE);

            holder.todoList.setFocusableInTouchMode(false);
            holder.todoList.setFocusable(false);
            holder.todoList.removeAllViewsInLayout();

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
                            mEventListener.onItemClicked(position, holder.subBackground);
                    }
                });
                textView.setTextColor(ViewUtils.getColor(mContext, R.color.blackPrimary));
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
                    holder.todoList.addView(view);
                    break;
                } else {
                    checkView.setVisibility(View.VISIBLE);
                    textView.setText(list.getTitle());
                    holder.todoList.addView(view);
                }
            }
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