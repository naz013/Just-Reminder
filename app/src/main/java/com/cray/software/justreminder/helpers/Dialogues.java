/**
 * Copyright 2016 Nazar Suhovich
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

package com.cray.software.justreminder.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.file_explorer.FileExploreActivity;
import com.cray.software.justreminder.groups.GroupHelper;
import com.cray.software.justreminder.groups.GroupItem;
import com.cray.software.justreminder.groups.Position;
import com.cray.software.justreminder.interfaces.LCAMListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.services.AutoSyncAlarm;
import com.cray.software.justreminder.services.EventsCheckAlarm;
import com.cray.software.justreminder.utils.MemoryUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dialogues {

    /**
     * Sound stream selecting dialogue.
     * @param context application context.
     */
    public static void rateDialog(final Activity context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setMessage(context.getString(R.string.can_you_rate_this_application));
        builder.setPositiveButton(context.getString(R.string.rate), (dialog, which) -> {
            dialog.dismiss();
            SharedPrefs.getInstance(context).putBoolean(Prefs.RATE_SHOW, true);
            launchMarket(context);
        });
        builder.setNeutralButton(context.getString(R.string.later), (dialog, which) -> {
            dialog.dismiss();
            SharedPrefs.getInstance(context).putBoolean(Prefs.RATE_SHOW, false);
            SharedPrefs.getInstance(context).putInt(Prefs.APP_RUNS_COUNT, 0);
        });
        builder.setNegativeButton(context.getString(R.string.never), (dialog, which) -> {
            dialog.dismiss();
            SharedPrefs.getInstance(context).putBoolean(Prefs.RATE_SHOW, true);
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static void launchMarket(Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Messages.toast(activity, activity.getString(R.string.could_not_launch_market));
        }
    }

    /**
     * Show long click action dialogue for lists.
     * @param context application context.
     * @param listener listener.
     * @param actions list of actions.
     */
    public static void showLCAM(Context context, final LCAMListener listener, String... actions) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(actions, (dialog, item) -> {
            dialog.dismiss();
            if (listener != null) listener.onAction(item);
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Sound stream selecting dialogue.
     * @param context application context.
     */
    public static void streamDialog(final Activity context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.sound_stream));
        String[] types = new String[]{context.getString(R.string.music),
                context.getString(R.string.alarm),
                context.getString(R.string.notification)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);
        int stream = SharedPrefs.getInstance(context).getInt(Prefs.SOUND_STREAM);
        builder.setSingleChoiceItems(adapter, stream - 3, (dialog, which) -> {
            if (which != -1) {
                dialog.dismiss();
                SharedPrefs.getInstance(context).putInt(Prefs.SOUND_STREAM, which + 3);
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting application screen orientation.
     * @param context application context.
     * @param listener listener for Dialog.
     */
    public static void imageDialog(final Activity context, DialogInterface.OnDismissListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.background));
        String[] types = new String[]{context.getString(R.string.none),
                context.getString(R.string.default_string),
                context.getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);
        String image = SharedPrefs.getInstance(context).getString(Prefs.REMINDER_IMAGE);
        int selection;
        if (image.matches(Constants.NONE)) {
            selection = 0;
        } else if (image.matches(Constants.DEFAULT)){
            selection = 1;
        } else {
            selection = 2;
        }
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            if (which != -1) {
                dialog.dismiss();
                SharedPrefs prefs = SharedPrefs.getInstance(context);
                if (which == 0) {
                    prefs.putString(Prefs.REMINDER_IMAGE, Constants.NONE);
                } else if (which == 1) {
                    prefs.putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
                } else if (which == 2) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    if (Module.isKitkat()) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                    }
                    Intent chooser = Intent.createChooser(intent, context.getString(R.string.image));
                    context.startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            SharedPrefs prefs = SharedPrefs.getInstance(context);
            if (which == 0) {
                prefs.putString(Prefs.REMINDER_IMAGE, Constants.NONE);
            } else if (which == 1) {
                prefs.putString(Prefs.REMINDER_IMAGE, Constants.DEFAULT);
            } else if (which == 2) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (Module.isKitkat()) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                }
                Intent chooser = Intent.createChooser(intent, context.getString(R.string.image));
                context.startActivityForResult(chooser, Constants.ACTION_REQUEST_GALLERY);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(listener);
        dialog.show();
    }

    /**
     * Reminder category selection dialog.
     * @param context application context.
     * @param categoryId current category unique identifier.
     * @param listener dialog callback listener.
     */
    public static void selectCategory(Context context, String categoryId, final OnCategorySelectListener listener) {
        Position position = new Position();
        List<GroupItem> list = new ArrayList<>();
        final List<String> categories = GroupHelper.getInstance(context).getAllNames(position, list, categoryId);
        Log.d(Constants.LOG_TAG, "selectCategory: " + position);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.choose_group);
        builder.setSingleChoiceItems(new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, categories), position.i, (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onCategory(list.get(which));
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Create and AlertDialog with customizable seekbar.
     * @param context Application context.
     * @param max seekbar maximum.
     * @param prefs Preference key for saving result.
     * @param title title for Dialog.
     * @param listener Dialog action listener.
     */
    public static void dialogWithSeek(final Context context, int max, final String prefs, String title, DialogInterface.OnDismissListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(title);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_seekbar, null);
        final TextView textView = (TextView) layout.findViewById(R.id.seekValue);
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.dialogSeek);
        seekBar.setMax(max);
        int progress = SharedPrefs.getInstance(context).getInt(prefs);
        seekBar.setProgress(progress);
        if (prefs.matches(Prefs.TEXT_SIZE)){
            textView.setText(String.valueOf(progress + 12));
        } else {
            textView.setText(String.valueOf(progress));
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (prefs.matches(Prefs.TEXT_SIZE)){
                    textView.setText(String.valueOf(progress + 12));
                } else {
                    textView.setText(String.valueOf(progress));
                }
                SharedPrefs.getInstance(context).putInt(prefs, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setView(layout);
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(listener);
        dialog.show();
    }

    /**
     * AlertDialog for selecting application screen orientation.
     * @param context application context.
     * @param listener listener for Dialog.
     */
    public static void orientationDialog(final Context context, DialogInterface.OnDismissListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.screen_orientation));
        String[] types = new String[]{context.getString(R.string.auto),
                context.getString(R.string.portrait),
                context.getString(R.string.landscape)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);
        String screen = SharedPrefs.getInstance(context).getString(Prefs.SCREEN);
        int selection = 0;
        if (screen.matches(Constants.SCREEN_AUTO)) {
            selection = 0;
        } else if (screen.matches(Constants.SCREEN_PORTRAIT)){
            selection = 1;
        } else if (screen.matches(Constants.SCREEN_LANDSCAPE)){
            selection = 2;
        }
        builder.setSingleChoiceItems(adapter, selection, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs prefs = SharedPrefs.getInstance(context);
                if (which == 0) {
                    prefs.putString(Prefs.SCREEN, Constants.SCREEN_AUTO);
                } else if (which == 1) {
                    prefs.putString(Prefs.SCREEN, Constants.SCREEN_PORTRAIT);
                } else if (which == 2) {
                    prefs.putString(Prefs.SCREEN, Constants.SCREEN_LANDSCAPE);
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(listener);
        dialog.show();
    }

    /**
     * AlertDialog for selecting type of melody - system or custom file.
     * @param context application context.
     * @param prefsToSave Preference key to save result.
     * @param listener action listener for dialog.
     */
    public static void melodyType(final Activity context, final String prefsToSave,
                                  final DialogInterface.OnDismissListener listener, final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.melody));
        String[] types = new String[]{context.getString(R.string.default_string),
                context.getString(R.string.choose_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);
        int position;
        if (!SharedPrefs.getInstance(context).getBoolean(prefsToSave)) {
            position = 0;
        } else {
            position = 1;
        }
        builder.setSingleChoiceItems(adapter, position, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs prefs = SharedPrefs.getInstance(context);
                if (which == 0) {
                    prefs.putBoolean(prefsToSave, false);
                } else {
                    prefs.putBoolean(prefsToSave, true);
                    dialog.dismiss();
                    context.startActivityForResult(new Intent(context, FileExploreActivity.class), requestCode);
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(listener);
        dialog.show();
    }

    /**
     * AlertDialog for selecting default Google Calendar.
     * @param context application context.
     * @param list action listener for dialog.
     */
    public static void selectCalendar(final Context context, final ArrayList<CalendarManager.CalendarItem> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.choose_calendar));
        ArrayList<String> spinnerArray = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (CalendarManager.CalendarItem item : list) {
                spinnerArray.add(item.getName());
            }
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, spinnerArray);
        builder.setSingleChoiceItems(adapter, 0, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs prefs = SharedPrefs.getInstance(context);
                if (list != null) {
                    CalendarManager.CalendarItem item = list.get(which);
                    prefs.putString(Prefs.CALENDAR_NAME, item.getName());
                    prefs.putInt(Prefs.CALENDAR_ID, item.getId());
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
            if (which != -1) {
                SharedPrefs prefs = SharedPrefs.getInstance(context);
                if (list != null) {
                    CalendarManager.CalendarItem item = list.get(which);
                    prefs.putString(Prefs.CALENDAR_NAME, item.getName());
                    prefs.putInt(Prefs.CALENDAR_ID, item.getId());
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting language for voice recognition.
     * @param context application context.
     */
    public static void language(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.language));
        final ArrayList<String> locales = Language.getLanguages(context);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, locales);
        int language = SharedPrefs.getInstance(context).getInt(Prefs.VOICE_LOCALE);
        builder.setSingleChoiceItems(adapter, language, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs.getInstance(context).putInt(Prefs.VOICE_LOCALE, which);
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting LED indicator color for events.
     * @param context application context.
     * @param prefsToSave Preference key to save result.
     */
    public static void ledColor(final Context context, final String prefsToSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.led_color));
        String[] colors = new String[LED.NUM_OF_LEDS];
        for (int i = 0; i < LED.NUM_OF_LEDS; i++) {
            colors[i] = LED.getTitle(context, i);
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, colors);
        int position = SharedPrefs.getInstance(context).getInt(prefsToSave);
        builder.setSingleChoiceItems(adapter, position, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs.getInstance(context).putInt(prefsToSave, which);
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting language for voice notifications (text to speech).
     * @param context application context.
     * @param prefsToSave Preference key for results saving.
     */
    public static void ttsLocale(final Context context, final String prefsToSave) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.language));
        ArrayList<String> names = new ArrayList<>();
        names.add(context.getString(R.string.english));
        names.add(context.getString(R.string.french));
        names.add(context.getString(R.string.german));
        names.add(context.getString(R.string.italian));
        names.add(context.getString(R.string.japanese));
        names.add(context.getString(R.string.korean));
        names.add(context.getString(R.string.polish));
        names.add(context.getString(R.string.russian));
        names.add(context.getString(R.string.spanish));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, names);
        int position = 1;
        String locale = SharedPrefs.getInstance(context).getString(prefsToSave);
        if (locale.matches(Language.ENGLISH)) position = 0;
        if (locale.matches(Language.FRENCH)) position = 1;
        if (locale.matches(Language.GERMAN)) position = 2;
        if (locale.matches(Language.ITALIAN)) position = 3;
        if (locale.matches(Language.JAPANESE)) position = 4;
        if (locale.matches(Language.KOREAN)) position = 5;
        if (locale.matches(Language.POLISH)) position = 6;
        if (locale.matches(Language.RUSSIAN)) position = 7;
        if (locale.matches(Language.SPANISH)) position = 8;
        builder.setSingleChoiceItems(adapter, position, (dialog, which) -> {
            if (which != -1) {
                String locale1 = Language.ENGLISH;
                if (which == 0) locale1 = Language.ENGLISH;
                if (which == 1) locale1 = Language.FRENCH;
                if (which == 2) locale1 = Language.GERMAN;
                if (which == 3) locale1 = Language.ITALIAN;
                if (which == 4) locale1 = Language.JAPANESE;
                if (which == 5) locale1 = Language.KOREAN;
                if (which == 6) locale1 = Language.POLISH;
                if (which == 7) locale1 = Language.RUSSIAN;
                if (which == 8) locale1 = Language.SPANISH;
                SharedPrefs.getInstance(context).putString(prefsToSave, locale1);
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting map type.
     * @param context application context.
     */
    public static void mapType(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.map_type));
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.map_types,
                android.R.layout.simple_list_item_single_choice);
        int type = SharedPrefs.getInstance(context).getInt(Prefs.MAP_TYPE);
        int position;
        if (type == Constants.MAP_NORMAL){
            position = 0;
        } else if (type == Constants.MAP_SATELLITE){
            position = 1;
        } else if (type == Constants.MAP_TERRAIN){
            position = 2;
        } else if (type == Constants.MAP_HYBRID){
            position = 3;
        } else {
            position = 0;
        }
        builder.setSingleChoiceItems(adapter, position, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs.getInstance(context).putInt(Prefs.MAP_TYPE, which + 1);
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting first day of week in calendars.
     * @param context application context
     */
    public static void firstDay(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.first_day));
        String[] items = {context.getString(R.string.sunday),
                context.getString(R.string.monday)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, items);
        int day = SharedPrefs.getInstance(context).getInt(Prefs.START_DAY);
        builder.setSingleChoiceItems(adapter, day, (dialog, which) -> {
            if (which != -1) {
                SharedPrefs.getInstance(context).putInt(Prefs.START_DAY, which);
                new UpdatesHelper(context).updateCalendarWidget();
            }
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting sync intervals for reminders and birthdays.
     * @param context application context.
     * @param prefsToSave Preference key for results.
     * @param titleRes title for dialog.
     */
    public static void selectInterval(final Context context, final String prefsToSave, int titleRes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(titleRes));
        final CharSequence[] items = {context.getString(R.string.one_hour),
                context.getString(R.string.six_hours),
                context.getString(R.string.twelve_hours),
                context.getString(R.string.one_day),
                context.getString(R.string.two_days)};
        int position;
        int interval = SharedPrefs.getInstance(context).getInt(prefsToSave);
        switch (interval){
            case 1:
                position = 0;
                break;
            case 6:
                position = 1;
                break;
            case 12:
                position = 2;
                break;
            case 24:
                position = 3;
                break;
            case 48:
                position = 4;
                break;
            default:
                position = 0;
                break;
        }
        builder.setSingleChoiceItems(items, position, (dialog, item) -> {
            if (item == 0) {
                SharedPrefs.getInstance(context).putInt(prefsToSave, 1);
            } else if (item == 1) {
                SharedPrefs.getInstance(context).putInt(prefsToSave, 6);
            } else if (item == 2) {
                SharedPrefs.getInstance(context).putInt(prefsToSave, 12);
            } else if (item == 3) {
                SharedPrefs.getInstance(context).putInt(prefsToSave, 24);
            } else if (item == 4) {
                SharedPrefs.getInstance(context).putInt(prefsToSave, 48);
            }
            if (prefsToSave.matches(Prefs.AUTO_BACKUP_INTERVAL)) new AutoSyncAlarm().setAlarm(context);
            else new EventsCheckAlarm().setAlarm(context);
        });
        builder.setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Method for deleting all file/folders inside selected folder.
     * @param fileOrDirectory file or directory to delete.
     */
    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    /**
     * AlertDialog for cleaning all application data on SDCard and Cloud's.
     * @param context application context.
     */
    public static void cleanFolders(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(context.getString(R.string.clean));
        builder.setNeutralButton(R.string.local, (dialog, which) -> {
            File dir = MemoryUtil.getParent();
            deleteRecursive(dir);
        });
        builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton(R.string.all, (dialog, which) -> {
            File dir = MemoryUtil.getParent();
            deleteRecursive(dir);
            new Thread(() -> {
                GDriveHelper gdx = new GDriveHelper(context);
                DropboxHelper dbx = new DropboxHelper(context);
                if (SyncHelper.isConnected(context)) {
                    gdx.clean();
                    dbx.cleanFolder();
                }
            }).start();

        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface OnCategorySelectListener{
        void onCategory(GroupItem item);
    }
}
