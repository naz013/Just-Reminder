package com.cray.software.justreminder.reminder;

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
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.interfaces.RecyclerListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.IntervalUtil;
import com.cray.software.justreminder.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Locale;

public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private TimeCount mCount;
    private ColorSetter cs;
    private ArrayList<ReminderModel> mDataList;
    private RecyclerListener mEventListener;
    private boolean is24;
    private boolean isDark;

    public RemindersRecyclerAdapter(Context context, ArrayList<ReminderModel> list) {
        this.mContext = context;
        mDataList = new ArrayList<>(list);
        SharedPrefs prefs = new SharedPrefs(context);
        cs = new ColorSetter(context);
        mCount = new TimeCount(context);
        is24 = prefs.loadBoolean(Prefs.IS_24_TIME_FORMAT);
        isDark = cs.isDark();
        setHasStableIds(true);
    }

    public ReminderModel getItem(int position) {
        if (position < mDataList.size()) {
            return mDataList.get(position);
        } return null;
    }

    public void removeItem(int position) {
        if (position < mDataList.size()) {
            mDataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeRemoved(0, mDataList.size());
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public TextView leftTime, taskTitle, taskDate, reminder_type, reminder_phone,
                repeatInterval, listHeader;
        public SwitchCompat check;
        public CardView itemCard;

        public TextView shoppingTitle, shoppingTime;
        public LinearLayout todoList;
        public LinearLayout subBackground, titleContainer, endContainer;
        public RelativeLayout reminderContainer;

        public ViewHolder(View v) {
            super(v);
            reminderContainer = (RelativeLayout) v.findViewById(R.id.reminderContainer);
            leftTime = (TextView) v.findViewById(R.id.remainingTime);
            listHeader = (TextView) v.findViewById(R.id.listHeader);
            check = (SwitchCompat) v.findViewById(R.id.itemCheck);
            check.setVisibility(View.VISIBLE);
            taskDate = (TextView) v.findViewById(R.id.taskDate);
            taskDate.setText("");
            reminder_type = (TextView) v.findViewById(R.id.reminder_type);
            reminder_type.setText("");
            reminder_phone = (TextView) v.findViewById(R.id.reminder_phone);
            reminder_phone.setText("");
            repeatInterval = (TextView) v.findViewById(R.id.repeatInterval);
            repeatInterval.setText("");

            taskTitle = (TextView) v.findViewById(R.id.taskText);
            shoppingTime = (TextView) v.findViewById(R.id.shoppingTime);
            shoppingTime.setVisibility(View.GONE);
            taskTitle.setText("");
            itemCard = (CardView) v.findViewById(R.id.itemCard);
            itemCard.setCardBackgroundColor(cs.getCardStyle());
            if (Module.isLollipop()) {
                itemCard.setCardElevation(Configs.CARD_ELEVATION);
            }

            endContainer = (LinearLayout) v.findViewById(R.id.endContainer);
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
                    if (mEventListener != null) {
                        mEventListener.onItemSwitched(getAdapterPosition(), check);
                    }
                }
            });

            reminderContainer.setBackgroundColor(cs.getCardStyle());
            subBackground.setBackgroundColor(cs.getCardStyle());
        }

        @Override
        public void onClick(View v) {
            View view = itemCard;
            if (mDataList.get(getAdapterPosition()).getViewType() == ReminderDataProvider.VIEW_REMINDER) {
                view = check;
            }
            if (mEventListener != null) {
                mEventListener.onItemClicked(getAdapterPosition(), view);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mEventListener != null) {
                mEventListener.onItemLongClicked(getAdapterPosition(), itemCard);
            }
            return true;
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ReminderModel item = mDataList.get(holder.getAdapterPosition());
        String title = item.getTitle();
        String type = item.getType();
        String number = item.getNumber();
        long due = item.getDue();
        double lat = item.getPlace()[0];
        double lon = item.getPlace()[1];
        int isDone = item.getCompleted();
        long repeat = item.getRepeat();
        String exclusion = item.getExclusion();
        int archived = item.getArchived();
        int categoryColor = item.getCatColor();

        String simpleDate = TimeUtil.getSimpleDate(due);
        holder.itemCard.setCardBackgroundColor(cs.getColor(cs.getCategoryColor(categoryColor)));

        if (archived == 1){
            if (position > 0 && simpleDate.equals(TimeUtil.getSimpleDate(mDataList.get(position - 1).getDue()))) {
                holder.listHeader.setVisibility(View.GONE);
            } else {
                if (due == 0){
                    simpleDate = mContext.getString(R.string.permanent);
                } else {
                    if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                        simpleDate = mContext.getString(R.string.today);
                    } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                        simpleDate = mContext.getString(R.string.tomorrow);
                    }
                }
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            }
        } else {
            if (isDone == 1 && position > 0 && mDataList.get(position - 1).getCompleted() == 0){
                simpleDate = mContext.getString(R.string.disabled);
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            } else if (isDone == 1 && position > 0 && mDataList.get(position - 1).getCompleted() == 1){
                holder.listHeader.setVisibility(View.GONE);
            } else if (isDone == 1 && position == 0){
                simpleDate = mContext.getString(R.string.disabled);
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            } else if (isDone == 0 && position > 0 && simpleDate.equals(TimeUtil.getSimpleDate(mDataList.get(position - 1).getDue()))){
                holder.listHeader.setVisibility(View.GONE);
            } else {
                if (due <= 0 || due < (System.currentTimeMillis() - AlarmManager.INTERVAL_DAY)){
                    simpleDate = mContext.getString(R.string.permanent);
                } else {
                    if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis()))) {
                        simpleDate = mContext.getString(R.string.today);
                    } else if (simpleDate.equals(TimeUtil.getSimpleDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY))) {
                        simpleDate = mContext.getString(R.string.tomorrow);
                    }
                }
                holder.listHeader.setText(simpleDate);
                holder.listHeader.setVisibility(View.VISIBLE);
            }
        }

        if (getItemViewType(position) == ReminderDataProvider.VIEW_REMINDER){
            holder.reminderContainer.setVisibility(View.VISIBLE);
            holder.subBackground.setVisibility(View.GONE);

            holder.taskTitle.setText(title);
            holder.reminder_type.setText(ReminderUtils.getTypeString(mContext, type));
            holder.reminder_phone.setVisibility(View.VISIBLE);
            if (type.contains(Constants.TYPE_CALL) || type.contains(Constants.TYPE_MESSAGE)) {
                String name = Contacts.getNameFromNumber(number, mContext);
                if (name == null) {
                    holder.reminder_phone.setText(number);
                } else {
                    holder.reminder_phone.setText(name + "(" + number + ")");
                }
            } else if (type.matches(Constants.TYPE_APPLICATION)) {
                PackageManager packageManager = mContext.getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(number, 0);
                } catch (final PackageManager.NameNotFoundException ignored) {
                }
                final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
                holder.reminder_phone.setText(name + "/" + number);
            } else if (type.matches(Constants.TYPE_MAIL)) {
                String name = Contacts.getNameFromMail(number, mContext);
                if (name == null) {
                    holder.reminder_phone.setText(number);
                } else {
                    holder.reminder_phone.setText(name + "(" + number + ")");
                }
            } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
                holder.reminder_phone.setText(number);
            } else {
                holder.reminder_phone.setVisibility(View.GONE);
            }

            if (type.contains(Constants.TYPE_LOCATION)) {
                holder.taskDate.setText(String.format(Locale.getDefault(), "%.5f %.5f (%d)", lat, lon, item.getTotalPlaces()));
                holder.endContainer.setVisibility(View.GONE);
            } else {
                holder.endContainer.setVisibility(View.VISIBLE);
                holder.taskDate.setText(TimeUtil.getFullDateTime(due, is24));
                if (isDone == 0) {
                    holder.leftTime.setText(mCount.getRemaining(due));
                } else holder.leftTime.setText("");

                if (type.startsWith(Constants.TYPE_MONTHDAY)) {
                    holder.repeatInterval.setText(String.format(mContext.getString(R.string.xM), 1));
                } else if (type.startsWith(Constants.TYPE_WEEKDAY)) {
                    holder.repeatInterval.setText(ReminderUtils.getRepeatString(mContext, item.getWeekdays()));
                } else {
                    holder.repeatInterval.setText(IntervalUtil.getInterval(mContext, repeat));
                }
            }

            if (type.matches(Constants.TYPE_TIME) && archived == 0){
                if (exclusion != null){
                    if (new Recurrence(exclusion).isRange()){
                        holder.taskDate.setText(R.string.paused);
                    }
                }
            }

            if (isDone == 1) {
                holder.leftTime.setText("");
                holder.check.setChecked(false);
            } else {
                holder.check.setChecked(true);
            }

            if (archived > 0) {
                holder.check.setVisibility(View.GONE);
                holder.leftTime.setText("");
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

            holder.shoppingTitle.setText(title);

            if (title.matches("")) {
                holder.titleContainer.setVisibility(View.GONE);
            } else {
                holder.titleContainer.setVisibility(View.VISIBLE);
            }

            holder.todoList.setFocusableInTouchMode(false);
            holder.todoList.setFocusable(false);
            holder.todoList.removeAllViewsInLayout();

            ShoppingListDataProvider provider = new ShoppingListDataProvider(item.getShoppings(), false);
            int count = 0;
            for (ShoppingList list : provider.getData()){
                View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_task_item_widget, null, false);
                ImageView checkView = (ImageView) view.findViewById(R.id.checkView);
                TextView textView = (TextView) view.findViewById(R.id.shopText);
                checkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mEventListener != null) {
                            mEventListener.onItemClicked(holder.getAdapterPosition(), holder.subBackground);
                        }
                    }
                });
                if (list.isChecked() == 1) {
                    if (isDark) checkView.setImageResource(R.drawable.ic_check_box_white_24dp);
                    else checkView.setImageResource(R.drawable.ic_check_box_black_24dp);
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    if (isDark) checkView.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp);
                    else checkView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);
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
        return mDataList.get(position).getViewType();
    }

    @Override
    public long getItemId(int position) {
        return mDataList.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    public void setEventListener(RecyclerListener eventListener) {
        mEventListener = eventListener;
    }
}