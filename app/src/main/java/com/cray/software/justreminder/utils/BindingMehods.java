package com.cray.software.justreminder.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Configs;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.contacts.Contacts;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingListItem;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Recurrence;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.TimeCount;
import com.cray.software.justreminder.helpers.Module;
import com.cray.software.justreminder.notes.NoteItem;
import com.cray.software.justreminder.reminder.ReminderItem;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.reminder.json.JPlace;
import com.cray.software.justreminder.reminder.json.JShopping;
import com.cray.software.justreminder.reminder.json.JsonModel;
import com.cray.software.justreminder.roboto_views.RoboSwitchCompat;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.theme.RetrofitBuilder;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class BindingMehods {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static SimpleDateFormat full24Format = new SimpleDateFormat("EEE,\ndd/MM", Locale.getDefault());

    @BindingAdapter({"loadDue"})
    public static void loadDue(RoboTextView view, long due) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        if (due != 0) {
            calendar.setTimeInMillis(due);
            String update = full24Format.format(calendar.getTime());
            view.setText(update);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @BindingAdapter({"loadTaskCard"})
    public static void loadTaskCard(CardView cardView, int i) {
        cardView.setCardBackgroundColor(ColorSetter.getInstance(cardView.getContext()).getCardStyle());
        if (Module.isLollipop()) {
            cardView.setCardElevation(Configs.CARD_ELEVATION);
        }
    }

    @BindingAdapter({"loadNote"})
    public static void loadNote(TextView textView, NoteItem note) {
        String title = note.getNote();
        Context context = textView.getContext();
        if (SharedPrefs.getInstance(context).getBoolean(Prefs.NOTE_ENCRYPT)) {
            title = SyncHelper.decrypt(title);
        }
        if (title.length() > 500) {
            String substring = title.substring(0, 500);
            title = substring + "...";
        }
        textView.setText(title);
        textView.setTypeface(ColorSetter.getInstance(context).getTypeface(note.getStyle()));
        textView.setTextSize(SharedPrefs.getInstance(context).getInt(Prefs.TEXT_SIZE) + 12);
    }

    @BindingAdapter({"loadImage"})
    public static void loadImage(ImageView imageView, byte[] image) {
        if (image != null) {
            Bitmap photo = BitmapFactory.decodeByteArray(image, 0, image.length);
            if (photo != null) {
                imageView.setImageBitmap(photo);
            } else {
                imageView.setImageDrawable(null);
            }
        } else {
            imageView.setImageDrawable(null);
        }
    }

    @BindingAdapter({"loadNoteCard"})
    public static void loadNoteCard(CardView cardView, int color) {
        cardView.setCardBackgroundColor(ColorSetter.getInstance(cardView.getContext()).getNoteLightColor(color));
        if (Module.isLollipop()) {
            cardView.setCardElevation(Configs.CARD_ELEVATION);
        }
    }

    @BindingAdapter({"loadBirthday"})
    public static void loadBirthday(RoboTextView textView, String fullDate) {
        Date date = null;
        int hour = SharedPrefs.getInstance(textView.getContext()).getInt(Prefs.BIRTHDAY_REMINDER_HOUR);
        int minute = SharedPrefs.getInstance(textView.getContext()).getInt(Prefs.BIRTHDAY_REMINDER_MINUTE);
        boolean is24 = SharedPrefs.getInstance(textView.getContext()).getBoolean(Prefs.IS_24_TIME_FORMAT);
        try {
            date = format.parse(fullDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time = System.currentTimeMillis();
        int year = 0;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int bDay = calendar.get(Calendar.DAY_OF_MONTH);
            int bMonth = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.MONTH, bMonth);
            calendar.set(Calendar.DAY_OF_MONTH, bDay);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            time = calendar.getTimeInMillis();
        }
        textView.setText(SuperUtil.appendString(TimeUtil.getFullDateTime(time, is24),
                "\n", TimeUtil.getAgeFormatted(textView.getContext(), year)));
    }

    @BindingAdapter({"loadType"})
    public static void loadType(RoboTextView textView, String type) {
        textView.setText(ReminderUtils.getTypeString(textView.getContext(), type));
    }

    @BindingAdapter({"loadItems"})
    public static void loadItems(LinearLayout container, List<JShopping> shoppings) {
        boolean isDark = ColorSetter.getInstance(container.getContext()).isDark();
        container.setFocusableInTouchMode(false);
        container.setFocusable(false);
        container.removeAllViewsInLayout();
        ShoppingListDataProvider provider = new ShoppingListDataProvider(shoppings, false);
        int count = 0;
        for (ShoppingListItem list : provider.getData()){
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.list_item_task_item_widget, null, false);
            ImageView checkView = (ImageView) view.findViewById(R.id.checkView);
            RoboTextView textView = (RoboTextView) view.findViewById(R.id.shopText);
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
                container.addView(view);
                break;
            } else {
                checkView.setVisibility(View.VISIBLE);
                textView.setText(list.getTitle());
                container.addView(view);
            }
        }
    }

    @BindingAdapter({"loadCard"})
    public static void loadCard(CardView cardView, String groupId) {
        ColorSetter cs = ColorSetter.getInstance(cardView.getContext());
        GroupItem item = GroupHelper.getInstance(cardView.getContext()).getGroup(groupId);
        if (item != null) {
            cardView.setCardBackgroundColor(cs.getColor(cs.getCategoryColor(item.getColor())));
        } else {
            cardView.setCardBackgroundColor(cs.getColor(cs.getCategoryColor(0)));
        }
    }

    @BindingAdapter({"loadDate"})
    public static void loadDate(RoboTextView textView, JsonModel model) {
        boolean is24 = SharedPrefs.getInstance(textView.getContext()).getBoolean(Prefs.IS_24_TIME_FORMAT);
        JPlace place = model.getPlace();
        if (model.getType().contains(Constants.TYPE_LOCATION)) {
            textView.setText(String.format(Locale.getDefault(), "%.5f %.5f (%d)", place.getLatitude(), place.getLongitude(), model.getPlaces().size()));
        } else {
            textView.setText(TimeUtil.getFullDateTime(model.getEventTime(), is24));
        }
        if (model.getType().matches(Constants.TYPE_TIME)){
            if (model.getExclusion() != null){
                if (new Recurrence(model.getExclusion()).isRange()){
                    textView.setText(R.string.paused);
                }
            }
        }
    }

    @BindingAdapter({"loadShoppingDate"})
    public static void loadShoppingDate(RoboTextView textView, long due) {
        boolean is24 = SharedPrefs.getInstance(textView.getContext()).getBoolean(Prefs.IS_24_TIME_FORMAT);
        if (due > 0){
            textView.setText(TimeUtil.getFullDateTime(due, is24));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"loadShoppingTitle"})
    public static void loadShoppingTitle(RoboTextView textView, String title) {
        if (title.matches("")) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter({"loadCheck"})
    public static void loadCheck(RoboSwitchCompat switchCompat, ReminderItem item) {
        if (item.getStatus() == 1) {
            switchCompat.setChecked(false);
        } else {
            switchCompat.setChecked(true);
        }
        if (item.getList() == 1) {
            switchCompat.setVisibility(View.GONE);
        }
    }

    @BindingAdapter({"loadLeft"})
    public static void loadLeft(RoboTextView textView, ReminderItem item) {
        if (item.getStatus() == 0) {
            textView.setText(TimeCount.getInstance(textView.getContext()).getRemaining(item.getDateTime()));
        } else {
            textView.setText("");
        }
    }

    @BindingAdapter({"loadRepeat"})
    public static void loadRepeat(RoboTextView textView, JsonModel model) {
        if (model.getType().startsWith(Constants.TYPE_MONTHDAY)) {
            textView.setText(String.format(textView.getContext().getString(R.string.xM), 1));
        } else if (model.getType().startsWith(Constants.TYPE_WEEKDAY)) {
            textView.setText(ReminderUtils.getRepeatString(textView.getContext(), model.getRecurrence().getWeekdays()));
        } else {
            textView.setText(IntervalUtil.getInterval(textView.getContext(), model.getRecurrence().getRepeat()));
        }
    }

    @BindingAdapter({"loadContainer"})
    public static void loadContainer(LinearLayout layout, String type) {
        if (type.contains(Constants.TYPE_LOCATION)) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter({"loadContact"})
    public static void loadContact(RoboTextView textView, JsonModel model) {
        String type = model.getType();
        String number = model.getAction().getTarget();
        textView.setVisibility(View.VISIBLE);
        if (type.contains(Constants.TYPE_CALL) || type.contains(Constants.TYPE_MESSAGE)) {
            String name = Contacts.getNameFromNumber(number, textView.getContext());
            if (name == null) {
                textView.setText(number);
            } else {
                textView.setText(name + "(" + number + ")");
            }
        } else if (type.matches(Constants.TYPE_APPLICATION)) {
            PackageManager packageManager = textView.getContext().getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(number, 0);
            } catch (final PackageManager.NameNotFoundException ignored) {
            }
            final String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");
            textView.setText(name + "/" + number);
        } else if (type.matches(Constants.TYPE_MAIL)) {
            String name = Contacts.getNameFromMail(number, textView.getContext());
            if (name == null) {
                textView.setText(number);
            } else {
                textView.setText(name + "(" + number + ")");
            }
        } else if (type.matches(Constants.TYPE_APPLICATION_BROWSER)) {
            textView.setText(number);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("loadPhoto")
    public static void loadPhoto(ImageView imageView, long id) {
        boolean isDark = ColorSetter.getInstance(imageView.getContext()).isDark();
        String url = RetrofitBuilder.getImageLink(id, 800, 480);
        Picasso.with(imageView.getContext())
                .load(url)
                .error(isDark ? R.drawable.ic_broken_image_white : R.drawable.ic_broken_image)
                .into(imageView);
    }
}
