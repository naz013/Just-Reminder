package com.cray.software.justreminder.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.FileExplore;
import com.cray.software.justreminder.async.LoadSounds;
import com.cray.software.justreminder.cloud.DropboxHelper;
import com.cray.software.justreminder.cloud.GDriveHelper;
import com.cray.software.justreminder.datas.CategoryDataProvider;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.LED;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.services.AutoSyncAlarm;
import com.cray.software.justreminder.services.EventsCheckAlarm;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Copyright 2015 Nazar Suhovich
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
public class Dialogues {

    public interface OnCategorySelectListener{
        void onCategory(String catId, String title);
    }

    /**
     * Reminder category selection dialog.
     * @param context application context.
     * @param categoryId current category unique identifier.
     * @param listener dialog callback listener.
     */
    public static void selectCategory(Context context, String categoryId, final OnCategorySelectListener listener) {
        final CategoryDataProvider provider = new CategoryDataProvider(context);
        final ArrayList<String> categories = new ArrayList<>();
        for (CategoryModel item : provider.getData()){
            categories.add(item.getTitle());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.string_select_category));
        builder.setSingleChoiceItems(new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, categories), provider.getPosition(categoryId), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (listener != null) listener.onCategory(provider.getItem(which).getUuID(), provider.getItem(which).getTitle());
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
        final SharedPrefs sharedPrefs = new SharedPrefs(context);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_seekbar, null);
        final TextView textView = (TextView) layout.findViewById(R.id.seekValue);
        SeekBar seekBar = (SeekBar) layout.findViewById(R.id.dialogSeek);
        seekBar.setMax(max);
        int progress = sharedPrefs.loadInt(prefs);
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
                sharedPrefs.saveInt(prefs, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        builder.setView(layout);
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.screen_orientation_title));
        String[] types = new String[]{context.getString(R.string.screen_auto),
                context.getString(R.string.screen_portrait),
                context.getString(R.string.screen_landscape)};

        SharedPrefs prefs = new SharedPrefs(context);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);

        String screen = prefs.loadPrefs(Prefs.SCREEN);
        int selection = 0;
        if (screen.matches(Constants.SCREEN_AUTO)) {
            selection = 0;
        } else if (screen.matches(Constants.SCREEN_PORTRAIT)){
            selection = 1;
        } else if (screen.matches(Constants.SCREEN_LANDSCAPE)){
            selection = 2;
        }
        builder.setSingleChoiceItems(adapter, selection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (which == 0) {
                        prefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_AUTO);
                    } else if (which == 1) {
                        prefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_PORTRAIT);
                    } else if (which == 2) {
                        prefs.savePrefs(Prefs.SCREEN, Constants.SCREEN_LANDSCAPE);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.sound_type_dialog_title));
        String[] types = new String[]{context.getString(R.string.sound_default),
                context.getString(R.string.sound_custom),
                context.getString(R.string.select_file)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, types);

        SharedPrefs prefs = new SharedPrefs(context);
        int position;
        if (!prefs.loadBoolean(prefsToSave)) {
            position = 0;
        } else {
            position = 1;
        }

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (which == 0) {
                        prefs.saveBoolean(prefsToSave, false);
                    } else if (which == 1) {
                        prefs.saveBoolean(prefsToSave, true);
                        dialog.dismiss();
                        new LoadSounds(context, prefsToSave, listener).execute();
                    } else {
                        prefs.saveBoolean(prefsToSave, true);
                        dialog.dismiss();
                        context.startActivityForResult(new Intent(context, FileExplore.class), requestCode);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPrefs prefs = new SharedPrefs(context);
                if (!prefs.loadBoolean(prefsToSave)) {
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                    new LoadSounds(context, prefsToSave, listener).execute();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * AlertDialog for selecting melody for event.
     * @param context application context.
     * @param prefsToSave Preference key to save result.
     * @param names list of melody file names.
     * @param folders list of path for each file name.
     * @param listener action listener for dialog.
     */
    public static void customMelody(final Context context, final String prefsToSave, ArrayList<String> names,
                                    final ArrayList<String> folders, DialogInterface.OnDismissListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(context.getString(R.string.sounds_dialog_title));
        final String newPrefs;
        if (prefsToSave.matches(Prefs.CUSTOM_SOUND)) newPrefs = Prefs.CUSTOM_SOUND_FILE;
        else newPrefs = Prefs.BIRTHDAY_CUSTOM_SOUND_FILE;

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, names);

        final Sound sound = new Sound(context);

        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    sound.play(folders.get(which));
                    SharedPrefs prefs = new SharedPrefs(context);
                    prefs.savePrefs(newPrefs, folders.get(which));
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sound.stop();
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.select_calendar_settings_title));

        ArrayList<String> spinnerArray = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (CalendarManager.CalendarItem item : list) {
                spinnerArray.add(item.getName());
            }
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, spinnerArray);

        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    CalendarManager.CalendarItem item = list.get(which);
                    prefs.savePrefs(Prefs.CALENDAR_NAME, item.getName());
                    prefs.savePrefs(Prefs.CALENDAR_ID, item.getId());
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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
        builder.setTitle(context.getString(R.string.select_language_title));
        final ArrayList<String> locales = new ArrayList<>();
        locales.clear();

        final String localeCheck = Locale.getDefault().toString().toLowerCase();
        int ru;
        int uk;
        int en;
        if (localeCheck.startsWith("uk")) {
            uk = 0;
            ru = 2;
            en = 1;
            locales.add(context.getString(R.string.language_ukrainian) + " (" + Constants.LANGUAGE_UK + ")");
            locales.add(context.getString(R.string.language_english) + " (" + Constants.LANGUAGE_EN + ")");
            locales.add(context.getString(R.string.language_russian) + " (" + Constants.LANGUAGE_RU + ")");
        } else if (localeCheck.startsWith("ru")) {
            uk = 2;
            ru = 0;
            en = 1;
            locales.add(context.getString(R.string.language_russian) + " (" + Constants.LANGUAGE_RU + ")");
            locales.add(context.getString(R.string.language_english) + " (" + Constants.LANGUAGE_EN + ")");
            locales.add(context.getString(R.string.language_ukrainian) + " (" + Constants.LANGUAGE_UK + ")");
        } else {
            uk = 1;
            ru = 2;
            en = 0;
            locales.add(context.getString(R.string.language_english) + " (" + Constants.LANGUAGE_EN + ")");
            locales.add(context.getString(R.string.language_ukrainian) + " (" + Constants.LANGUAGE_UK + ")");
            locales.add(context.getString(R.string.language_russian) + " (" + Constants.LANGUAGE_RU + ")");
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, locales);

        SharedPrefs prefs = new SharedPrefs(context);
        int i;
        String language = prefs.loadPrefs(Prefs.VOICE_LANGUAGE);
        if (language.matches(Constants.LANGUAGE_EN)){
            i = en;
        } else if (language.matches(Constants.LANGUAGE_RU)){
            i = ru;
        } else if (language.matches(Constants.LANGUAGE_UK)){
            i = uk;
        } else i = 0;

        builder.setSingleChoiceItems(adapter, i, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (localeCheck.startsWith("uk")) {
                        if (which == 0)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_UK);
                        if (which == 1)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_EN);
                        if (which == 2)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_RU);
                    } else if (localeCheck.startsWith("ru")) {
                        if (which == 0)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_RU);
                        if (which == 1)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_EN);
                        if (which == 2)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_UK);
                    } else {
                        if (which == 0)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_EN);
                        if (which == 1)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_UK);
                        if (which == 2)
                            prefs.savePrefs(Prefs.VOICE_LANGUAGE, Constants.LANGUAGE_RU);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.select_led_color_title));
        String[] colors = new String[]{context.getString(R.string.led_color_white),
                context.getString(R.string.led_color_red),
                context.getString(R.string.led_color_green),
                context.getString(R.string.led_color_blue),
                context.getString(R.string.led_color_orange),
                context.getString(R.string.led_color_yellow),
                context.getString(R.string.led_color_pink),
                context.getString(R.string.led_color_green_light),
                context.getString(R.string.led_color_blue_light)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, colors);

        SharedPrefs prefs = new SharedPrefs(context);
        int position;
        int color = prefs.loadInt(prefsToSave);
        switch (color){
            case LED.WHITE:
                position = 0;
                break;
            case LED.RED:
                position = 1;
                break;
            case LED.GREEN:
                position = 2;
                break;
            case LED.BLUE:
                position = 3;
                break;
            case LED.ORANGE:
                position = 4;
                break;
            case LED.YELLOW:
                position = 5;
                break;
            case LED.PINK:
                position = 6;
                break;
            case LED.GREEN_LIGHT:
                position = 7;
                break;
            case LED.BLUE_LIGHT:
                position = 8;
                break;
            default:
                position = 0;
                break;
        }

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    String locale = Language.ENGLISH;
                    if (which == 0) {
                        prefs.saveInt(prefsToSave, LED.WHITE);
                    } else if (which == 1) {
                        prefs.saveInt(prefsToSave, LED.RED);
                    } else if (which == 2) {
                        prefs.saveInt(prefsToSave, LED.GREEN);
                    } else if (which == 3) {
                        prefs.saveInt(prefsToSave, LED.BLUE);
                    } else if (which == 4) {
                        prefs.saveInt(prefsToSave, LED.ORANGE);
                    } else if (which == 5) {
                        prefs.saveInt(prefsToSave, LED.YELLOW);
                    } else if (which == 6) {
                        prefs.saveInt(prefsToSave, LED.PINK);
                    } else if (which == 7) {
                        prefs.saveInt(prefsToSave, LED.GREEN_LIGHT);
                    } else if (which == 8) {
                        prefs.saveInt(prefsToSave, LED.BLUE_LIGHT);
                    } else {
                        prefs.saveInt(prefsToSave, LED.BLUE);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.select_language_title));
        ArrayList<String> names = new ArrayList<>();
        names.add(context.getString(R.string.locale_english));
        names.add(context.getString(R.string.locale_french));
        names.add(context.getString(R.string.locale_german));
        names.add(context.getString(R.string.locale_italian));
        names.add(context.getString(R.string.locale_japanese));
        names.add(context.getString(R.string.locale_korean));
        names.add(context.getString(R.string.locale_polish));
        names.add(context.getString(R.string.locale_russian));
        names.add(context.getString(R.string.locale_spanish));

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, names);

        SharedPrefs prefs = new SharedPrefs(context);
        int position = 1;
        String locale = prefs.loadPrefs(prefsToSave);
        if (locale.matches(Language.ENGLISH)) position = 0;
        if (locale.matches(Language.FRENCH)) position = 1;
        if (locale.matches(Language.GERMAN)) position = 2;
        if (locale.matches(Language.ITALIAN)) position = 3;
        if (locale.matches(Language.JAPANESE)) position = 4;
        if (locale.matches(Language.KOREAN)) position = 5;
        if (locale.matches(Language.POLISH)) position = 6;
        if (locale.matches(Language.RUSSIAN)) position = 7;
        if (locale.matches(Language.SPANISH)) position = 8;

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    String locale = Language.ENGLISH;
                    if (which == 0) locale = Language.ENGLISH;
                    if (which == 1) locale = Language.FRENCH;
                    if (which == 2) locale = Language.GERMAN;
                    if (which == 3) locale = Language.ITALIAN;
                    if (which == 4) locale = Language.JAPANESE;
                    if (which == 5) locale = Language.KOREAN;
                    if (which == 6) locale = Language.POLISH;
                    if (which == 7) locale = Language.RUSSIAN;
                    if (which == 8) locale = Language.SPANISH;
                    prefs.savePrefs(prefsToSave, locale);
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.map_type_title));

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.map_types,
                android.R.layout.simple_list_item_single_choice);

        SharedPrefs prefs = new SharedPrefs(context);
        String type = prefs.loadPrefs(Prefs.MAP_TYPE);
        int position;
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            position = 0;
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            position = 1;
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            position = 2;
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            position = 3;
        } else {
            position = 0;
        }

        builder.setSingleChoiceItems(adapter, position, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    if (which == 0) {
                        prefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_NORMAL);
                    } else if (which == 1) {
                        prefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_SATELLITE);
                    } else if (which == 2) {
                        prefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_HYBRID);
                    } else if (which == 3) {
                        prefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_TERRAIN);
                    } else {
                        prefs.savePrefs(Prefs.MAP_TYPE, Constants.MAP_TYPE_NORMAL);
                    }
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.first_day_dialog_title));
        String[] items = {context.getString(R.string.start_day_sunday),
                context.getString(R.string.start_day_monday)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_single_choice, items);

        SharedPrefs prefs = new SharedPrefs(context);
        int day = prefs.loadInt(Prefs.START_DAY);

        builder.setSingleChoiceItems(adapter, day, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which != -1) {
                    SharedPrefs prefs = new SharedPrefs(context);
                    prefs.saveInt(Prefs.START_DAY, which);
                    new UpdatesHelper(context).updateCalendarWidget();
                }
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        SharedPrefs prefs = new SharedPrefs(context);
        int interval = prefs.loadInt(prefsToSave);
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

        builder.setSingleChoiceItems(items, position, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                SharedPrefs prefs = new SharedPrefs(context);
                if (item == 0) {
                    prefs.saveInt(prefsToSave, 1);
                } else if (item == 1) {
                    prefs.saveInt(prefsToSave, 6);
                } else if (item == 2) {
                    prefs.saveInt(prefsToSave, 12);
                } else if (item == 3) {
                    prefs.saveInt(prefsToSave, 24);
                } else if (item == 4) {
                    prefs.saveInt(prefsToSave, 48);
                }
                if (prefsToSave.matches(Prefs.AUTO_BACKUP_INTERVAL)) new AutoSyncAlarm().setAlarm(context);
                else new EventsCheckAlarm().setAlarm(context);
            }
        });
        builder.setPositiveButton(context.getString(R.string.button_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
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
        builder.setTitle(context.getString(R.string.settings_clean_title));
        builder.setMessage(context.getString(R.string.clean_dialog_message));
        builder.setNeutralButton(context.getString(R.string.clean_dialog_button_local), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (SyncHelper.isSdPresent()) {
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/");
                    deleteRecursive(sdPathDr);
                }
            }
        });
        builder.setNegativeButton(context.getString(R.string.button_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(context.getString(R.string.clean_dialog_button_full), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (SyncHelper.isSdPresent()) {
                    File sdPath = Environment.getExternalStorageDirectory();
                    File sdPathDr = new File(sdPath.getAbsolutePath() + "/JustReminder/");
                    deleteRecursive(sdPathDr);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        GDriveHelper gdx = new GDriveHelper(context);
                        DropboxHelper dbx = new DropboxHelper(context);
                        if (SyncHelper.isConnected(context)) {
                            gdx.clean();
                            dbx.cleanFolder();
                        }
                    }
                }).start();

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
