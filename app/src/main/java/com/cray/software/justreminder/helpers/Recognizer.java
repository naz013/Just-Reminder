package com.cray.software.justreminder.helpers;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.SettingsActivity;
import com.cray.software.justreminder.SplashScreen;
import com.cray.software.justreminder.VoiceHelp;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.note.NotesBase;
import com.cray.software.justreminder.dialogs.AddBirthday;
import com.cray.software.justreminder.dialogs.BirthdaysVoiceList;
import com.cray.software.justreminder.dialogs.QuickAddReminder;
import com.cray.software.justreminder.dialogs.VoiceResult;
import com.cray.software.justreminder.dialogs.utils.SelectVolume;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.services.AlarmReceiver;
import com.cray.software.justreminder.services.WeekDayReceiver;
import com.cray.software.justreminder.utils.RecognizerUtils;
import com.cray.software.justreminder.utils.ReminderUtils;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.hexrain.design.ScreenManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Recognizer {

    Context ctx;
    DataBase DB = new DataBase(ctx);
    SyncHelper sHelp;
    UpdatesHelper updatesHelper;
    SharedPrefs sPrefs;

    AlarmReceiver alarm = new AlarmReceiver();

    public final SimpleDateFormat[] dateTaskFormats = {
            new SimpleDateFormat("HH mm"),
            new SimpleDateFormat("HH:mm"),
            new SimpleDateFormat("HH")
    };

    SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm");

    long min = 60 * 1000;
    long hourLong = min * 60;
    long dayLong = hourLong * 24;

    public Recognizer(Context context){
        this.ctx = context;
    }

    public void selectTask(ArrayList matches, boolean isWidget){
        for (Object key:matches){
            String keyStr = key.toString().toLowerCase().trim();
            Log.d(Constants.LOG_TAG, keyStr);
            if (RecognizerUtils.isNumberContains(keyStr)) keyStr = RecognizerUtils.convertToNumbered(keyStr);
            if (keyStr.matches(".*відкрити.*") ||
                    keyStr.matches(".*открыть.*") ||
                    keyStr.matches(".*open.*")) {
                if (keyStr.matches(".*налаштування.*") ||
                        keyStr.matches(".*settings?.*") ||
                        keyStr.matches(".*настройки.*")){
                    ctx.startActivity(new Intent(ctx, SettingsActivity.class));
                    break;
                } else if (keyStr.matches(".*календар.*") ||
                        keyStr.matches(".*calendar.*") ||
                        keyStr.matches(".*календарь.*")){
                    ctx.startActivity(new Intent(ctx, ScreenManager.class)
                            .putExtra("tag", ScreenManager.ACTION_CALENDAR));
                    break;
                } else if (keyStr.matches(".*локації.*") ||
                        keyStr.matches(".*locations?.*") ||
                        keyStr.matches(".*локации.*")){
                    ctx.startActivity(new Intent(ctx, ScreenManager.class)
                            .putExtra("tag", ScreenManager.FRAGMENT_LOCATIONS));
                    break;
                } else if ((keyStr.matches(".*додаток.*") ||
                        keyStr.matches(".*application.*") ||
                        keyStr.matches(".*приложение.*")) && isWidget){
                    ctx.startActivity(new Intent(ctx, SplashScreen.class));
                    break;
                } else if ((keyStr.matches(".*підказки.*") || keyStr.matches(".*підказку.*") ||
                        keyStr.matches(".*допомогу.*") || keyStr.matches(".*подсказку.*") ||
                        keyStr.matches(".*help.*") || keyStr.matches(".*подсказки.*") ||
                        keyStr.matches(".*помощь.*"))){
                    ctx.startActivity(new Intent(ctx, VoiceHelp.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    break;
                }
            } else if (keyStr.matches(".*додати.*") ||
                    keyStr.matches(".*добавить.*") ||
                    keyStr.matches(".*new.*")){
                if (keyStr.matches(".*день народження.*") ||
                        keyStr.matches(".*birthday.*") ||
                        keyStr.matches(".*день рождения.*")){
                    ctx.startActivity(new Intent(ctx, AddBirthday.class));
                    break;
                } else if (keyStr.matches(".*нагадування.*") ||
                        keyStr.matches(".*reminder.*") ||
                        keyStr.matches(".*напоминание.*")){
                    ctx.startActivity(new Intent(ctx, QuickAddReminder.class));
                    break;
                }
            } else if ((keyStr.matches(".*налаштувати гучність.*") ||
                    keyStr.matches(".*настроить громкость.*") ||
                    keyStr.matches(".*volume settings.*"))){
                ctx.startActivity(new Intent(ctx, SelectVolume.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                break;
            } else if (keyStr.matches("завтра .* [0-9][0-9]? .*") ||
                    keyStr.matches("tomorrow .* [0-9][0-9]? .*")){
                unknownTomorrow(keyStr, isWidget);
                break;
            } else if (keyStr.matches("([0-9][0-9]?) (січ|лют|бер|квіт|трав|черв|лип|серп|вер|жовт|лист|груд).*") ||
                    keyStr.matches("([0-9][0-9]?) (янв|февр|мар|апр|ма|июн|июл|авг|сен|окт|ноя|дек).*") ||
                    keyStr.matches("(jan|feb|mar|apr|ma|jun|jul|aug|sep|oct|nov|dec).* ([0-9][0-9]?) .*")){
                unknownDate(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".*кожн.* ([0-9]?[0-9]?) ?(міс|дн|рок|тиж|рік|ден).*я?у?і?и?в? .*") ||
                    keyStr.matches(".*кажд.* ([0-9]?[0-9]?) ?(мес|дн|год|нед|лет|ден).*а?ы?т?с?в?й?ь?я?и?ц?д? .*") ||
                    keyStr.matches(".*every.* ([0-9]?[0-9]?) ?(day|month|week|year).*s? .*") ||
                    keyStr.matches("що.* ?([0-9]?[0-9]?) ?(міс|дн|рок|тиж|рік|ден).*я?у?і?и?в? .*")){
                unknownRepeat(keyStr, isWidget);
                break;
            } else if (keyStr.matches("через ([0-9]?[0-9]?) ?(хвил|годи|тижн|місяц|рок|рік)?.*я?у?і?и?в?н?ь? .*") ||
                    keyStr.matches("через ([0-9]?[0-9]?) ?(минут|час|неде|месяц|год|дн|ден)?.*я?у?и?ы?в?т?ь?с?а?й?ю?д? .*") ||
                    keyStr.matches("after ([0-9]?[0-9]?) ?(minute|hour|week|month|year)?.*s? .*")){
                unknownAfter(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".* (jan|feb|mar|apr|ma|jun|jul|aug|sep|oct|nov|dec).* \\d\\d?t?h? at \\d\\d? o'clock \\d\\d? minutes?.*") ||
                    keyStr.matches(".* \\d\\d? (січ|лют|бер|квіт|трав|черв|лип|серп|вер|жовт|лист|груд).* об? \\d\\d? годині? \\d\\d? хвилин.*") ||
                    keyStr.matches(".* \\d\\d? (янв|февр|мар|апр|ма|июн|июл|авг|сен|окт|ноя|дек).* в? ?\\d\\d? часов \\d\\d? минут.*")) {
                // set time reminder
                fullDateTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".* (jan|feb|mar|apr|ma|jun|jul|aug|sep|oct|nov|dec).* .*d?s?t?h? at \\d\\d? o'clock \\d\\d? minutes?.*") ||
                    keyStr.matches(".* .*г?о? (січ|лют|бер|квіт|трав|черв|лип|серп|вер|жовт|лист|груд).* об? \\d\\d? годині? \\d\\d? хвилин.*") ||
                    keyStr.matches(".* .*г?о? (янв|февр|мар|апр|ма|июн|июл|авг|сен|окт|ноя|дек).* в? ?\\d\\d? часов \\d\\d? минут.*")) {
                // set time reminder
                fullTextDateTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".*remind .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".*нагадай?т?и? .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".*напомнит?ь? .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
                // set date reminder
                dateTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".*remind .* after \\d\\d?\\d? (hour|minute)s?.*") ||
                    keyStr.matches(".*нагадай?т?и? .* через \\d\\d?\\d? (годин|хвилин)и?у?.*") ||
                    keyStr.matches(".*напомнит?ь? .* через \\d\\d?\\d? (час|минут)ы?о?в?у?.*")) {
                // set time reminder
                timeTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".* in .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".* у .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".* во? .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
                // set week reminder
                weekTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".*call .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".*подзвонит?и? .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".*позвонит?ь? .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
                // set call reminder
                callTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".*надіслати .* до .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".*send .* to .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*") ||
                    keyStr.matches(".*отправить .* до .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
                // set message reminder
                messageTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".*нотатка .*") ||
                    keyStr.matches(".*note .*") ||
                    keyStr.matches(".*заметка .*")){
                // save note
                saveNote(keyStr);
                break;
            } else if (keyStr.matches(".*показати .* \\d\\d?\\d? .*") ||
                    keyStr.matches(".*show .* \\d\\d?\\d? .*") ||
                    keyStr.matches(".*показать .* \\d\\d?\\d? .*")){
                // show featured birthdays
                showBirthdays(keyStr);
                break;
            } else if ((keyStr.matches(".*підказки.*") || keyStr.matches(".*підказку.*") ||
                    keyStr.matches(".*допомогу.*") || keyStr.matches(".*подсказку.*") ||
                    keyStr.matches(".*help.*") || keyStr.matches(".*подсказки.*") ||
                    keyStr.matches(".*помощь.*")) && isWidget){
                ctx.startActivity(new Intent(ctx, VoiceHelp.class));
                break;
            } else if (keyStr.matches(".* tonight.*") ||
                    keyStr.matches(".* ввечері.*") ||
                    keyStr.matches(".* вечером.*")){
                eveningTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".* morning.*") ||
                    keyStr.matches(".* вранці.*") ||
                    keyStr.matches(".* утром?.*") || keyStr.matches(".* зранку.*")){
                morningTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".* afternoon.*") ||
                    keyStr.matches(".* вдень.*") ||
                    keyStr.matches(".* днем.*")){
                dayTask(keyStr, isWidget);
                break;
            } else if (keyStr.matches(".* night.*") ||
                    keyStr.matches(".* вночі.*") ||
                    keyStr.matches(".* ночью.*")){
                nightTask(keyStr, isWidget);
                break;
            }
        }
    }

    private void unknownTomorrow(String keyStr, boolean isWidget) {
        boolean export = RecognizerUtils.isCalendarExportable(keyStr);
        if (keyStr.matches("tomorrow .* [0-9][0-9]? .*")) {
            //english version
            String[] parts = keyStr.split(" \\d\\d?");
            int size = parts.length;
            if (size == 4) {
                String neilPart = parts[0];
                String hoursPart = parts[1];
                String taskPart = parts[2];
                String decrPart = parts[3];

                String hour = keyStr.substring(keyStr.lastIndexOf(neilPart) + neilPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();
                String minute = keyStr.substring(keyStr.lastIndexOf(hoursPart) + hoursPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String decr = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(decrPart)).trim();

                boolean isDecrement = false;
                if (taskPart.contains(" remind")) {
                    isDecrement = true;
                }

                boolean isMinutes = false;
                if (taskPart.contains("minute")) isMinutes = true;

                int indexEnd = -1;
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" remind");
                }

                int indexStart;
                int[] indexes = RecognizerUtils.getIndexes(taskPart);
                indexStart = indexes[0] + indexes[1];
                String task = null;
                if (indexEnd != -1 && indexStart != -1) {
                    if (isMinutes) task = hoursPart.substring(indexStart, indexEnd).trim();
                    else task = taskPart.substring(indexStart, indexEnd).trim();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = Integer.parseInt(minute);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);

                if (isTimeValid && task != null) {
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(hoursPart);

                    long currTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime)
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(decr) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 3) {
                String neilPart = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];

                String hour = keyStr.substring(keyStr.lastIndexOf(neilPart) + neilPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String minuteDecr = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();

                boolean isDecrement = false;
                if (taskPart.contains(" remind") || hoursPart.contains(" remind")) {
                    isDecrement = true;
                }
                boolean isMinutes = false;
                if (hoursPart.contains("minute")) isMinutes = true;

                int indexEnd = -1;
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" remind");
                }

                int indexStart;
                if (isMinutes) {
                    int[] indexes = RecognizerUtils.getIndexes(hoursPart);
                    indexStart = indexes[0] + indexes[1];
                } else {
                    int[] indexes = RecognizerUtils.getIndexes(taskPart);
                    indexStart = indexes[0] + indexes[1];
                }
                String task = null;
                if (indexEnd != -1 && indexStart != -1) {
                    if (isMinutes) task = hoursPart.substring(indexStart, indexEnd).trim();
                    else task = taskPart.substring(indexStart, indexEnd).trim();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = 0;
                if (!isDecrement) minuteOfHour = Integer.parseInt(minuteDecr);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);

                if (isTimeValid && task != null) {
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(hoursPart);

                    long currTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime)
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(minuteDecr) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 2) {
                String neilPart = parts[0];
                String taskPart = parts[1];

                String hour = keyStr.substring(keyStr.lastIndexOf(neilPart) + neilPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();

                int[] indexes = RecognizerUtils.getIndexes(taskPart);
                int indexStart = indexes[0] + indexes[1];

                String task = null;
                if (indexStart != -1) {
                    task = taskPart.substring(indexStart).trim();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = 0;

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);

                if (isTimeValid && task != null) {
                    long currTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime)
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            }
        } else {
            //ukrainian and russian
            String[] parts = keyStr.split(" \\d\\d?");
            int size = parts.length;
            if (size == 4) {
                String neilPart = parts[0];
                String hoursPart = parts[1];
                String taskPart = parts[2];
                String decrPart = parts[3];

                String hour = keyStr.substring(keyStr.lastIndexOf(neilPart) + neilPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();
                String minute = keyStr.substring(keyStr.lastIndexOf(hoursPart) + hoursPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String decr = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(decrPart)).trim();

                boolean isDecrement = false;
                if (taskPart.contains(" нагада") || taskPart.contains(" напомн") ||
                        hoursPart.contains(" нагада") || hoursPart.contains(" напомн")) {
                    isDecrement = true;
                }

                boolean isMinutes = false;
                if (taskPart.contains("минут") || taskPart.contains("хвилин")) isMinutes = true;

                int indexEnd = -1;
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" нагада");
                    if (indexEnd == -1) indexEnd = taskPart.indexOf(" напомн");
                }

                int indexStart;
                int[] indexes = RecognizerUtils.getIndexes(taskPart);
                indexStart = indexes[0] + indexes[1];
                String task = null;
                if (indexEnd != -1 && indexStart != -1) {
                    if (isMinutes) task = hoursPart.substring(indexStart, indexEnd).trim();
                    else task = taskPart.substring(indexStart, indexEnd).trim();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = Integer.parseInt(minute);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);

                if (isTimeValid && task != null) {
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(hoursPart);

                    long currTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime)
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(decr) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 3) {
                String neilPart = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];

                String hour = keyStr.substring(keyStr.lastIndexOf(neilPart) + neilPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String minuteDecr = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();

                boolean isDecrement = false;
                if (taskPart.contains(" нагада") || taskPart.contains(" напомн")) {
                    isDecrement = true;
                }
                boolean isMinutes = false;
                if (taskPart.contains("минут") || taskPart.contains("хвилин")) isMinutes = true;

                int indexEnd = -1;
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" нагада");
                    if (indexEnd == -1) indexEnd = taskPart.indexOf(" напомн");
                }

                int indexStart;
                if (isMinutes) {
                    int[] indexes = RecognizerUtils.getIndexes(hoursPart);
                    indexStart = indexes[0] + indexes[1];
                } else {
                    int[] indexes = RecognizerUtils.getIndexes(taskPart);
                    indexStart = indexes[0] + indexes[1];
                }
                String task = null;
                if (indexEnd != -1 && indexStart != -1) {
                    if (isMinutes) task = hoursPart.substring(indexStart, indexEnd).trim();
                    else task = taskPart.substring(indexStart, indexEnd).trim();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = 0;
                if (!isDecrement) minuteOfHour = Integer.parseInt(minuteDecr);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);

                if (isTimeValid && task != null) {
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(hoursPart);

                    long currTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime)
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(minuteDecr) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 2) {
                String neilPart = parts[0];
                String taskPart = parts[1];

                String hour = keyStr.substring(keyStr.lastIndexOf(neilPart) + neilPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();

                int[] indexes = RecognizerUtils.getIndexes(taskPart);
                int indexStart = indexes[0] + indexes[1];

                String task = null;
                if (indexStart != -1) {
                    task = taskPart.substring(indexStart).trim();
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = 0;

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);

                if (isTimeValid && task != null) {
                    long currTime = calendar.getTimeInMillis();
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + AlarmManager.INTERVAL_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime)
                        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            }
        }
    }

    private void unknownDate(String keyStr, boolean isWidget) {
        boolean export = RecognizerUtils.isCalendarExportable(keyStr);
        if (keyStr.matches("(jan|feb|mar|apr|ma|jun|jul|aug|sep|oct|nov|dec).* ([0-9][0-9]?) .*")){
            //english version
            String[] parts = keyStr.split(" \\d\\d?");
            int size = parts.length;
            if (size == 5){
                String monthPart = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];
                String minutesPart = parts[3];
                String decrementPart = parts[4];

                String date = keyStr.substring(keyStr.lastIndexOf(monthPart) + monthPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String hour = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();
                String minutes = keyStr.substring(keyStr.lastIndexOf(hoursPart) + hoursPart.length(),
                        keyStr.lastIndexOf(minutesPart)).trim();
                String decrement = keyStr.substring(keyStr.lastIndexOf(minutesPart) + minutesPart.length(),
                        keyStr.lastIndexOf(decrementPart)).trim();

                int month = RecognizerUtils.getMonthFromString(monthPart);

                int indexEnd = taskPart.lastIndexOf(" at ");
                String task;
                if (indexEnd != -1) task = taskPart.substring(0, indexEnd).trim();
                else task = taskPart.trim();

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = Integer.parseInt(minutes);
                int dayOfMonth = Integer.parseInt(date);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                boolean isDateValid = checkDate(dayOfMonth, month);

                if (isDateValid && isTimeValid){
                    long divider = RecognizerUtils.getLongIndexes(decrementPart);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    long currTime = calendar.getTimeInMillis();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(decrement) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 4){
                String monthPart = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];
                String minutesPart = parts[3];

                String date = keyStr.substring(keyStr.lastIndexOf(monthPart) + monthPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String hour = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();
                String minutesDecr = keyStr.substring(keyStr.lastIndexOf(hoursPart) + hoursPart.length(),
                        keyStr.lastIndexOf(minutesPart)).trim();

                int month = RecognizerUtils.getMonthFromString(monthPart);

                boolean isDecrement = false;
                if (minutesPart.contains(" remind") || hoursPart.contains(" remind")) {
                    isDecrement = true;
                }

                int indexEnd = taskPart.lastIndexOf(" at ");
                String task;
                if (indexEnd != -1) task = taskPart.substring(0, indexEnd).trim();
                else task = taskPart.trim();

                int hourOfDay = Integer.parseInt(hour);
                int minuteOfHour = 0;
                if (!isDecrement) minuteOfHour = Integer.parseInt(minutesDecr);
                int dayOfMonth = Integer.parseInt(date);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                boolean isDateValid = checkDate(dayOfMonth, month);

                if (isDateValid && isTimeValid){
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(minutesPart);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    long currTime = calendar.getTimeInMillis();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(minutesDecr) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 3){
                String monthPart = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];

                String date = keyStr.substring(keyStr.lastIndexOf(monthPart) + monthPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String hourDecr = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();

                int month = RecognizerUtils.getMonthFromString(monthPart);

                boolean isDecrement = false;
                if (taskPart.contains(" remind") || hoursPart.contains(" remind")) {
                    isDecrement = true;
                }

                int indexEnd = taskPart.lastIndexOf(" at ");
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" remind");
                }
                String task;
                if (indexEnd != -1) task = taskPart.substring(0, indexEnd).trim();
                else task = taskPart.trim();

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                if (!isDecrement) hourOfDay = Integer.parseInt(hourDecr);
                int minuteOfHour = calendar.get(Calendar.MINUTE);
                int dayOfMonth = Integer.parseInt(date);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                boolean isDateValid = checkDate(dayOfMonth, month);

                if (isDateValid && isTimeValid){
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(hoursPart);

                    long currTime = calendar.getTimeInMillis();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = Integer.parseInt(hourDecr) * divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            } else if (size == 2){
                String monthPart = parts[0];
                String taskPart = parts[1];

                String date = keyStr.substring(keyStr.lastIndexOf(monthPart) + monthPart.length(),
                        keyStr.lastIndexOf(taskPart)).trim();

                int month = RecognizerUtils.getMonthFromString(monthPart);

                boolean isDecrement = false;
                if (taskPart.contains(" remind")) {
                    isDecrement = true;
                }

                int indexEnd = taskPart.lastIndexOf(" at ");
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" remind");
                }
                String task;
                if (indexEnd != -1) task = taskPart.substring(0, indexEnd).trim();
                else task = taskPart.trim();

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minuteOfHour = calendar.get(Calendar.MINUTE);
                int dayOfMonth = Integer.parseInt(date);

                boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                boolean isDateValid = checkDate(dayOfMonth, month);

                if (isDateValid && isTimeValid){
                    long divider = 0;
                    if (isDecrement) divider = RecognizerUtils.getLongIndexes(taskPart);

                    long currTime = calendar.getTimeInMillis();
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    long taskDate = calendar.getTimeInMillis();
                    if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                    long minus = divider;
                    calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                    saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                }
            }
        } else {
            //ukrainian and russian
            keyStr = " tag " + keyStr;
            String[] parts = keyStr.split(" \\d\\d?");
            int size = parts.length;
            if (size == 5){
                String neil = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];
                String minutesPart = parts[3];
                String decrementPart = parts[4];

                String date = keyStr.substring(keyStr.lastIndexOf(neil) + neil.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String hour = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();
                String minutes = keyStr.substring(keyStr.lastIndexOf(hoursPart) + hoursPart.length(),
                        keyStr.lastIndexOf(minutesPart)).trim();
                String decrement = keyStr.substring(keyStr.lastIndexOf(minutesPart) + minutesPart.length(),
                        keyStr.lastIndexOf(decrementPart)).trim();

                int[] indexes = RecognizerUtils.getMonthIndexes(taskPart);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int month = indexes[2];

                int indexEnd = taskPart.lastIndexOf(" о ");
                if (indexEnd == -1) indexEnd = taskPart.lastIndexOf(" в ");
                if (indexStart != -1){
                    String task;
                    if (indexEnd != -1) task = taskPart.substring(indexStart + increment, indexEnd).trim();
                    else task = taskPart.substring(indexStart + increment).trim();

                    int hourOfDay = Integer.parseInt(hour);
                    int minuteOfHour = Integer.parseInt(minutes);
                    int dayOfMonth = Integer.parseInt(date);

                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, month);

                    if (isDateValid && isTimeValid){
                        long divider = RecognizerUtils.getLongIndexes(decrementPart);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        long currTime = calendar.getTimeInMillis();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minuteOfHour);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long taskDate = calendar.getTimeInMillis();
                        if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                        long minus = Integer.parseInt(decrement) * divider;
                        calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                        saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                    }
                }
            } else if (size == 4){
                String neil = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];
                String minutesPart = parts[3];

                String date = keyStr.substring(keyStr.lastIndexOf(neil) + neil.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String hour = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();
                String minutesDecr = keyStr.substring(keyStr.lastIndexOf(hoursPart) + hoursPart.length(),
                        keyStr.lastIndexOf(minutesPart)).trim();

                int[] indexes = RecognizerUtils.getMonthIndexes(taskPart);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int month = indexes[2];

                boolean isDecrement = false;
                if (minutesPart.contains(" нагада") || minutesPart.contains(" напомн") ||
                        hoursPart.contains(" нагада") || hoursPart.contains(" напомн")) {
                    isDecrement = true;
                }

                int indexEnd = taskPart.lastIndexOf(" о ");
                if (indexEnd == -1) indexEnd = taskPart.lastIndexOf(" в ");
                if (indexStart != -1){
                    String task;
                    if (indexEnd != -1) task = taskPart.substring(indexStart + increment, indexEnd).trim();
                    else task = taskPart.substring(indexStart + increment).trim();

                    int hourOfDay = Integer.parseInt(hour);
                    int minuteOfHour = 0;
                    if (!isDecrement) minuteOfHour = Integer.parseInt(minutesDecr);
                    int dayOfMonth = Integer.parseInt(date);

                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, month);

                    if (isDateValid && isTimeValid){
                        long divider = 0;
                        if (isDecrement) divider = RecognizerUtils.getLongIndexes(minutesPart);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        long currTime = calendar.getTimeInMillis();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minuteOfHour);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long taskDate = calendar.getTimeInMillis();
                        if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                        long minus = Integer.parseInt(minutesDecr) * divider;
                        calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                        saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                    }
                }
            } else if(size == 3){
                String neil = parts[0];
                String taskPart = parts[1];
                String hoursPart = parts[2];

                String date = keyStr.substring(keyStr.lastIndexOf(neil) + neil.length(),
                        keyStr.lastIndexOf(taskPart)).trim();
                String hourDecr = keyStr.substring(keyStr.lastIndexOf(taskPart) + taskPart.length(),
                        keyStr.lastIndexOf(hoursPart)).trim();

                int[] indexes = RecognizerUtils.getMonthIndexes(taskPart);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int month = indexes[2];

                boolean isDecrement = false;
                if (hoursPart.contains(" нагада") || hoursPart.contains(" напомн") ||
                        taskPart.contains(" нагада") || taskPart.contains(" напомн")) {
                    isDecrement = true;
                }

                int indexEnd = taskPart.lastIndexOf(" о ");
                if (indexEnd == -1) indexEnd = taskPart.lastIndexOf(" в ");
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" нагада");
                    if (indexEnd == -1) indexEnd = taskPart.indexOf(" напомн");
                }
                if (indexStart != -1){
                    String task;
                    if (indexEnd != -1) task = taskPart.substring(indexStart + increment, indexEnd).trim();
                    else task = taskPart.substring(indexStart + increment).trim();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    if (!isDecrement) hourOfDay = Integer.parseInt(hourDecr);
                    int minuteOfHour = calendar.get(Calendar.MINUTE);
                    int dayOfMonth = Integer.parseInt(date);

                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, month);

                    if (isDateValid && isTimeValid){
                        long divider = 0;
                        if (isDecrement) divider = RecognizerUtils.getLongIndexes(hoursPart);

                        long currTime = calendar.getTimeInMillis();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minuteOfHour);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long taskDate = calendar.getTimeInMillis();
                        if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                        long minus = Integer.parseInt(hourDecr) * divider;
                        calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                        saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                    }
                }
            } else if(size == 2){
                String neil = parts[0];
                String taskPart = parts[1];

                String date = keyStr.substring(keyStr.lastIndexOf(neil) + neil.length(),
                        keyStr.lastIndexOf(taskPart)).trim();

                int[] indexes = RecognizerUtils.getMonthIndexes(taskPart);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int month = indexes[2];

                boolean isDecrement = false;
                if (taskPart.contains(" нагада") || taskPart.contains(" напомн")) {
                    isDecrement = true;
                }

                int indexEnd = -1;
                if (isDecrement) {
                    indexEnd = taskPart.indexOf(" нагада");
                    if (indexEnd == -1) indexEnd = taskPart.indexOf(" напомн");
                }
                if (indexStart != -1){
                    String task;
                    if (indexEnd != -1) task = taskPart.substring(indexStart + increment, indexEnd).trim();
                    else task = taskPart.substring(indexStart + increment).trim();

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());

                    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                    int minuteOfHour = calendar.get(Calendar.MINUTE);
                    int dayOfMonth = Integer.parseInt(date);

                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, month);

                    if (isDateValid && isTimeValid){
                        long divider = 0;
                        if (isDecrement) divider = RecognizerUtils.getLongIndexes(taskPart);

                        long currTime = calendar.getTimeInMillis();
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minuteOfHour);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        long taskDate = calendar.getTimeInMillis();
                        if (taskDate < currTime) calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + 1);

                        long minus = divider;
                        calendar.setTimeInMillis(calendar.getTimeInMillis() - minus);

                        saveReminder(task, isWidget, export, calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                    }
                }
            }
        }
    }

    public static final long MINUTE = 1000 * 60;

    private void unknownAfter(String keyStr, boolean isWidget) {
        keyStr = RecognizerUtils.convertToDouble(keyStr);
        boolean export = RecognizerUtils.isCalendarExportable(keyStr);
        String[] parts = keyStr.split(" \\d.?\\d?");
        if (parts.length == 3){
            String tech = parts[0];
            String time = parts[1];
            String before = parts[2];
            if (keyStr.contains("годин") || keyStr.contains("хвил") ||
                    keyStr.contains("час") || keyStr.contains("минут") ||
                    keyStr.contains("hour") || keyStr.contains("minute")){
                String number = keyStr.substring(keyStr.lastIndexOf(tech) + tech.length(),
                        keyStr.lastIndexOf(time)).trim();
                int[] indexes = RecognizerUtils.getIndexes(time);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int multiplier = indexes[2];
                int indexEnd = time.indexOf(" нагада");
                if (indexEnd == -1) indexEnd = time.indexOf(" напомн");
                if (indexEnd == -1) indexEnd = time.indexOf(" remind");
                String numberBefore = keyStr.substring(keyStr.lastIndexOf(time) + time.length(),
                        keyStr.lastIndexOf(before)).trim();
                int[] minusInd = RecognizerUtils.getIndexes(before);
                int divider = minusInd[2];
                if (indexEnd != -1 && indexStart != -1){
                    indexStart = indexStart + increment;
                    String task = time.substring(indexStart, indexEnd).trim();
                    double minus = Double.parseDouble(numberBefore) * divider;
                    double after = Double.parseDouble(number) * multiplier;
                    if (after > minus) after = after - minus;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinute = calendar.get(Calendar.MINUTE);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int mYear = calendar.get(Calendar.YEAR);
                    saveTimeReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, Math.round(after));
                }
            } else {
                String number = keyStr.substring(keyStr.lastIndexOf(tech) + tech.length(),
                        keyStr.lastIndexOf(time)).trim();
                int[] indexes = RecognizerUtils.getIndexes(time);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int multiplier = indexes[2];
                int indexEnd = time.indexOf(" нагада");
                if (indexEnd == -1) indexEnd = time.indexOf(" напомн");
                if (indexEnd == -1) indexEnd = time.indexOf(" remind");
                String numberBefore = keyStr.substring(keyStr.lastIndexOf(time) + time.length(),
                        keyStr.lastIndexOf(before)).trim();
                int[] minusInd = RecognizerUtils.getIndexes(before);
                int divider = minusInd[2];
                if (indexEnd != -1 && indexStart != -1){
                    indexStart = indexStart + increment;
                    String task = time.substring(indexStart, indexEnd).trim();
                    double minus = Double.parseDouble(numberBefore) * divider;
                    double after = Double.parseDouble(number) * multiplier;
                    if (after > minus) after = after - minus;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis() +
                            Math.round(AlarmManager.INTERVAL_DAY * after));
                    int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinute = calendar.get(Calendar.MINUTE);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int mYear = calendar.get(Calendar.YEAR);
                    saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, 0);
                }
            }
        } else if (parts.length == 2){
            String tech = parts[0];
            String time = parts[1];
            if (keyStr.contains("годин") || keyStr.contains("хвил") ||
                    keyStr.contains("час") || keyStr.contains("минут") ||
                    keyStr.contains("hour") || keyStr.contains("minute")){
                String number = keyStr.substring(keyStr.lastIndexOf(tech) + tech.length(),
                        keyStr.lastIndexOf(time)).trim();
                int[] indexes = RecognizerUtils.getIndexes(time);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int multiplier = indexes[2];
                if (indexStart != -1){
                    indexStart = indexStart + increment;
                    String task = time.substring(indexStart).trim();
                    double after = Double.parseDouble(number) * multiplier;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinute = calendar.get(Calendar.MINUTE);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int mYear = calendar.get(Calendar.YEAR);
                    long afterTime = Math.round(after * MINUTE);
                    saveTimeReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, afterTime);
                }
            } else {
                String number = keyStr.substring(keyStr.lastIndexOf(tech) + tech.length(),
                        keyStr.lastIndexOf(time)).trim();
                int[] indexes = RecognizerUtils.getIndexes(time);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int multiplier = indexes[2];
                if (indexStart != -1){
                    indexStart = indexStart + increment;
                    String task = time.substring(indexStart).trim();
                    double after = Double.parseDouble(number) * multiplier;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis() +
                            Math.round(AlarmManager.INTERVAL_DAY * after));
                    int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinute = calendar.get(Calendar.MINUTE);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int mYear = calendar.get(Calendar.YEAR);
                    saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, 0);
                }
            }
        } else if (parts.length == 1){
            String time = parts[0];
            if (keyStr.contains("годин") || keyStr.contains("хвил") ||
                    keyStr.contains("час") || keyStr.contains("минут") ||
                    keyStr.contains("hour") || keyStr.contains("minute")){
                int[] indexes = RecognizerUtils.getIndexes(time);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int multiplier = indexes[2];
                if (indexStart != -1){
                    indexStart = indexStart + increment;
                    String task = time.substring(indexStart).trim();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinute = calendar.get(Calendar.MINUTE);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int mYear = calendar.get(Calendar.YEAR);
                    saveTimeReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                }
            } else {
                int[] indexes = RecognizerUtils.getIndexes(time);
                int indexStart = indexes[0];
                int increment = indexes[1];
                int multiplier = indexes[2];
                if (indexStart != -1){
                    indexStart = indexStart + increment;
                    String task = time.substring(indexStart).trim();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis() +
                            AlarmManager.INTERVAL_DAY * multiplier);
                    int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mMinute = calendar.get(Calendar.MINUTE);
                    int mMonth = calendar.get(Calendar.MONTH);
                    int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                    int mYear = calendar.get(Calendar.YEAR);
                    saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, 0);
                }
            }
        }
    }

    private void unknownRepeat(String keyStr, boolean isWidget) {
        boolean export = RecognizerUtils.isCalendarExportable(keyStr);
        if (keyStr.matches(".*кожн.* ([0-9]?[0-9]?) ?(міс|дн|рок|тиж|рік|ден).*я?у?і?и?в? .*") ||
                keyStr.matches("що.* ?([0-9]?[0-9]?) ?(міс|дн|рок|тиж|рік|ден).*я?у?і?и?в? .*")){
            //ukrainian
            if (keyStr.contains("щотижня") || keyStr.contains("щодня") ||
                    keyStr.contains("щомісяця") || keyStr.contains("щороку")){
                if (keyStr.contains(" год")){
                    String[] parts = keyStr.split(" \\d\\d?");
                    if (parts.length == 3) {
                        String rep = parts[0];
                        String res = parts[1];
                        String hr = parts[2];
                        int interval = 1;
                        String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                        int hours = Integer.parseInt(number);
                        number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                        int minutes = Integer.parseInt(number);
                        int[] indexes = RecognizerUtils.getIndexes(res);
                        int indexStart = indexes[0];
                        int increment = indexes[1];
                        int multiplier = indexes[2];

                        multiplier = multiplier * interval;
                        if (indexStart != -1 && RecognizerUtils.isCorrectTime(hours, minutes)){
                            indexStart = indexStart + increment;
                            String task = res.substring(indexStart).trim();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                            int mMinute = calendar.get(Calendar.MINUTE);
                            if (mHour > hours || mHour == hours && mMinute > minutes) {
                                calendar.setTimeInMillis(System.currentTimeMillis() +
                                        AlarmManager.INTERVAL_DAY * multiplier);
                                calendar.set(Calendar.HOUR_OF_DAY, hours);
                                calendar.set(Calendar.MINUTE, minutes);
                            } else {
                                calendar.set(Calendar.HOUR_OF_DAY, hours);
                                calendar.set(Calendar.MINUTE, minutes);
                            }
                            int mMonth = calendar.get(Calendar.MONTH);
                            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                            int mYear = calendar.get(Calendar.YEAR);
                            mHour = calendar.get(Calendar.HOUR_OF_DAY);
                            mMinute = calendar.get(Calendar.MINUTE);

                            saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                        }
                    } else if (parts.length == 2){
                        String rep = parts[0];
                        String res = parts[1];
                        int interval = 1;
                        String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                        int hours = Integer.parseInt(number);
                        int[] indexes = RecognizerUtils.getIndexes(res);
                        int indexStart = indexes[0];
                        int increment = indexes[1];
                        int multiplier = indexes[2];

                        multiplier = multiplier * interval;
                        if (indexStart != -1 && RecognizerUtils.isCorrectTime(hours, 0)){
                            indexStart = indexStart + increment;
                            String task = res.substring(indexStart).trim();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                            int mMinute = calendar.get(Calendar.MINUTE);
                            if (mHour > hours || mHour == hours && mMinute > 0) {
                                calendar.setTimeInMillis(System.currentTimeMillis() +
                                        AlarmManager.INTERVAL_DAY * multiplier);
                                calendar.set(Calendar.HOUR_OF_DAY, hours);
                                calendar.set(Calendar.MINUTE, 0);
                            } else {
                                calendar.set(Calendar.HOUR_OF_DAY, hours);
                                calendar.set(Calendar.MINUTE, 0);
                            }
                            int mMonth = calendar.get(Calendar.MONTH);
                            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                            int mYear = calendar.get(Calendar.YEAR);
                            mHour = calendar.get(Calendar.HOUR_OF_DAY);
                            mMinute = calendar.get(Calendar.MINUTE);

                            saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                        }
                    }
                } else {
                    int interval = 1;
                    int[] indexes = RecognizerUtils.getIndexes(keyStr);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];

                    multiplier = multiplier * interval;
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = keyStr.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis()+
                                AlarmManager.INTERVAL_DAY * multiplier);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            } else if (keyStr.contains(" год")){
                //with hours
                String[] parts = keyStr.split(" \\d\\d?");
                if (parts.length == 4) {
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String min = parts[3];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int hours = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(hr) + hr.length(), keyStr.lastIndexOf(min)).trim();
                    int minutes = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];

                    multiplier = multiplier * interval;
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hours, minutes)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hours || mHour == hours && mMinute > minutes) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hours);
                            calendar.set(Calendar.MINUTE, minutes);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hours);
                            calendar.set(Calendar.MINUTE, minutes);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 3 && !keyStr.contains(" хвили")){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int hour = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    multiplier = multiplier * interval;
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, 0)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > 0) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 3 && keyStr.contains(" хвили")){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int hour = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int minute = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, minute)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > minute) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 2){
                    String rep = parts[0];
                    String res = parts[1];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int hour = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, 0)){
                        indexStart = indexStart + increment;
                        String task = rep.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > 0) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            } else {
                String[] parts = keyStr.split(" \\d\\d?");
                if (parts.length == 2){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    multiplier = multiplier * interval;
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY * multiplier);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 1){
                    String res = parts[0];
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY * multiplier);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            }
        }
        if (keyStr.matches(".*кажд.* ([0-9]?[0-9]?) ?(мес|дн|год|нед|лет|ден).*а?ы?т?с?в?й?ь?я?и?ц?д? .*")){
            //russian
            if (keyStr.contains(" час")){
                //with hours
                String[] parts = keyStr.split(" \\d\\d?");
                if (parts.length == 4) {
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String min = parts[3];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int hours = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(hr) + hr.length(), keyStr.lastIndexOf(min)).trim();
                    int minutes = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];

                    multiplier = multiplier * interval;
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hours, minutes)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hours || mHour == hours && mMinute > minutes) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hours);
                            calendar.set(Calendar.MINUTE, minutes);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hours);
                            calendar.set(Calendar.MINUTE, minutes);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 3 && !keyStr.contains(" минут")){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int hour = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    multiplier = multiplier * interval;
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, 0)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > 0) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 3 && keyStr.contains(" минут")){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int hour = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int minute = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, minute)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > minute) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 2){
                    String rep = parts[0];
                    String res = parts[1];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int hour = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, 0)){
                        indexStart = indexStart + increment;
                        String task = rep.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > 0) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            } else {
                String[] parts = keyStr.split(" \\d\\d?");
                if (parts.length == 2){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    multiplier = multiplier * interval;
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis() +
                                AlarmManager.INTERVAL_DAY * multiplier);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 1){
                    String res = parts[0];
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis() +
                                AlarmManager.INTERVAL_DAY * multiplier);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            }
        }
        if (keyStr.matches(".*every.* ([0-9]?[0-9]?) ?(day|month|week|year).*s? .*")){
            //english
            if (keyStr.contains(" hour")){
                //with hours
                String[] parts = keyStr.split(" \\d\\d?");
                if (parts.length == 4) {
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String min = parts[3];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int hours = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(hr) + hr.length(), keyStr.lastIndexOf(min)).trim();
                    int minutes = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];

                    multiplier = multiplier * interval;
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hours, minutes)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hours || mHour == hours && mMinute > minutes) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hours);
                            calendar.set(Calendar.MINUTE, minutes);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hours);
                            calendar.set(Calendar.MINUTE, minutes);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 3 && !keyStr.contains(" minut")){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int hour = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    multiplier = multiplier * interval;
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, 0)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > 0) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 3 && keyStr.contains(" minut")){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String hr = parts[2];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int hour = Integer.parseInt(number);
                    number = keyStr.substring(keyStr.lastIndexOf(res) + res.length(), keyStr.lastIndexOf(hr)).trim();
                    int minute = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, minute)){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > minute) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, minute);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 2){
                    String rep = parts[0];
                    String res = parts[1];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int hour = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1 && RecognizerUtils.isCorrectTime(hour, 0)){
                        indexStart = indexStart + increment;
                        String task = rep.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (mHour > hour || mHour == hour && mMinute > 0) {
                            calendar.setTimeInMillis(System.currentTimeMillis() +
                                    AlarmManager.INTERVAL_DAY * multiplier);
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        } else {
                            calendar.set(Calendar.HOUR_OF_DAY, hour);
                            calendar.set(Calendar.MINUTE, 0);
                        }
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            } else {
                String[] parts = keyStr.split(" \\d\\d?");
                if (parts.length == 2){
                    //with number of days
                    String rep = parts[0];
                    String res = parts[1];
                    String number = keyStr.substring(keyStr.lastIndexOf(rep) + rep.length(), keyStr.lastIndexOf(res)).trim();
                    int interval = Integer.parseInt(number);
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    multiplier = multiplier * interval;
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis() +
                                AlarmManager.INTERVAL_DAY * multiplier);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                } else if (parts.length == 1){
                    String res = parts[0];
                    int[] indexes = RecognizerUtils.getIndexes(res);
                    int indexStart = indexes[0];
                    int increment = indexes[1];
                    int multiplier = indexes[2];
                    if (indexStart != -1){
                        indexStart = indexStart + increment;
                        String task = res.substring(indexStart).trim();
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis() +
                                AlarmManager.INTERVAL_DAY * multiplier);
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);

                        saveReminder(task, isWidget, export, mDay, mMonth, mYear, mHour, mMinute, multiplier);
                    }
                }
            }
        }
    }

    private void saveTimeReminder(String task, boolean isWidget, boolean export, int dayOfMonth,
                              int monthOfYear, int mYear, int hourOfDay, int minuteOfHour,
                              long after){
        DB = new DataBase(ctx);
        DB.open();
        sHelp = new SyncHelper(ctx);
        String uuID = sHelp.generateID();
        sPrefs = new SharedPrefs(ctx);
        long id;
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)) && export) {
            id = DB.insertReminder(task, Constants.TYPE_TIME,
                    dayOfMonth, monthOfYear, mYear, hourOfDay, minuteOfHour, 0, null, 0, after, 0,
                    0, 0, uuID, null, 1, null, 0, 0, 0, categoryId);
            exportToCalendar(task, ReminderUtils.getTime(dayOfMonth, monthOfYear, mYear, hourOfDay, minuteOfHour, after), id);
        } else {
            id = DB.insertReminder(task, Constants.TYPE_TIME,
                    dayOfMonth, monthOfYear, mYear, hourOfDay, minuteOfHour, 0, null, 0, after, 0,
                    0, 0, uuID, null, 0, null, 0, 0, 0, categoryId);
        }
        DB.updateReminderDateTime(id);
        alarm.setAlarm(ctx, id);
        updatesHelper = new UpdatesHelper(ctx);
        updatesHelper.updateWidget();
        showResult(id, isWidget);
    }

    private void saveReminder(String task, boolean isWidget, boolean export, int dayOfMonth,
                              int monthOfYear, int mYear, int hourOfDay, int minuteOfHour,
                              int repeat){
        DB = new DataBase(ctx);
        DB.open();
        sHelp = new SyncHelper(ctx);
        String uuID = sHelp.generateID();
        sPrefs = new SharedPrefs(ctx);
        long id;
        Cursor cf = DB.queryCategories();
        String categoryId = null;
        if (cf != null && cf.moveToFirst()) {
            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
        }
        if (cf != null) cf.close();
        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)) && export) {
            id = DB.insertReminder(task, Constants.TYPE_REMINDER,
                    dayOfMonth, monthOfYear, mYear, hourOfDay, minuteOfHour, 0, null, repeat, 0, 0,
                    0, 0, uuID, null, 1, null, 0, 0, 0, categoryId);
            exportToCalendar(task, ReminderUtils.getTime(dayOfMonth, monthOfYear, mYear, hourOfDay, minuteOfHour, 0), id);
        } else {
            id = DB.insertReminder(task, Constants.TYPE_REMINDER,
                    dayOfMonth, monthOfYear, mYear, hourOfDay, minuteOfHour, 0, null, repeat, 0, 0,
                    0, 0, uuID, null, 0, null, 0, 0, 0, categoryId);
        }
        DB.updateReminderDateTime(id);
        alarm.setAlarm(ctx, id);
        updatesHelper = new UpdatesHelper(ctx);
        updatesHelper.updateWidget();
        showResult(id, isWidget);
    }

    private void fullTextDateTask(String keyStr, boolean isWidget) {
        boolean export = RecognizerUtils.isCalendarExportable(keyStr);
        int indexStart;
        if (keyStr.matches(".* (jan|feb|mar|apr|ma|jun|jul|aug|sep|oct|nov|dec).* .*d?s?t?h? at \\d\\d? o'clock \\d\\d? minutes?.*")){
            String[] parts = keyStr.split(" \\d\\d?");
            String res = null;
            if (parts.length == 3) {
                String overall = parts[0].trim();
                String hourStr = parts[1].trim();
                String minuteStr = parts[2].trim();
                indexStart = RecognizerUtils.getIndexForMonth(overall);
                if (indexStart != -1){
                    res = overall.substring(0, indexStart).trim();
                }
                String date = null;
                if (res != null && !res.matches("")) {
                    int index = RecognizerUtils.getIndexFromDay(overall);
                    int dayOfMonth = RecognizerUtils.getDayFromString(overall);
                    int indexEnd;
                    int monthOfYear = RecognizerUtils.getMonthFromString(overall);

                    indexStart = index;
                    indexEnd = keyStr.lastIndexOf(hourStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + overall.length(), indexEnd).trim();
                    }
                    int hourOfDay = Integer.parseInt(date);

                    indexStart = keyStr.lastIndexOf(hourStr);
                    indexEnd = keyStr.lastIndexOf(minuteStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + hourStr.length(), indexEnd).trim();
                    }
                    int minuteOfHour = Integer.parseInt(date);
                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, monthOfYear);
                    if (isDateValid && isTimeValid){
                        Calendar calendar = Calendar.getInstance();
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (monthOfYear < mMonth ||
                                (monthOfYear == mMonth && mDay > dayOfMonth) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour > hourOfDay) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour == hourOfDay && mMinute > minuteOfHour)) {
                            mYear = mYear + 1;
                        }
                        saveReminder(res, isWidget, export, mDay, mMonth, mYear, hourOfDay, minuteOfHour, 0);
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.wrong_date_time_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (keyStr.matches(".* .*г?о? (січ|лют|бер|квіт|трав|черв|лип|серп|вер|жовт|лист|груд).* об? \\d\\d? годині? \\d\\d? хвилин.*")){
            String[] parts = keyStr.split(" \\d\\d? ");
            String res;
            if (parts.length == 3) {
                String month = parts[0].trim();
                String hourStr = parts[1].trim();
                String minuteStr = parts[2].trim();
                int index = RecognizerUtils.getIndexFromDay(month);
                int dayOfMonth = RecognizerUtils.getDayFromString(month);
                res = month.substring(0, index);
                String date = null;
                if (!res.matches("")) {
                    int indexEnd;
                    int monthOfYear = RecognizerUtils.getMonthFromString(month);

                    indexStart = keyStr.lastIndexOf(month);
                    indexEnd = keyStr.lastIndexOf(hourStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + month.length(), indexEnd).trim();
                    }
                    int hourOfDay = Integer.parseInt(date);

                    indexStart = keyStr.lastIndexOf(hourStr);
                    indexEnd = keyStr.lastIndexOf(minuteStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + hourStr.length(), indexEnd).trim();
                    }
                    int minuteOfHour = Integer.parseInt(date);
                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, monthOfYear);
                    if (isDateValid && isTimeValid){
                        Calendar calendar = Calendar.getInstance();
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (monthOfYear < mMonth ||
                                (monthOfYear == mMonth && mDay > dayOfMonth) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour > hourOfDay) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour == hourOfDay && mMinute > minuteOfHour)) {
                            mYear = mYear + 1;
                        }
                        saveReminder(res, isWidget, export, mDay, mMonth, mYear, hourOfDay, minuteOfHour, 0);
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.wrong_date_time_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (keyStr.matches(".* .*г?о? (янв|февр|мар|апр|ма|июн|июл|авг|сен|окт|ноя|дек).* в? ?\\d\\d? часов \\d\\d? минут.*")){
            String[] parts = keyStr.split(" \\d\\d? ");
            String res;
            if (parts.length == 3) {
                String month = parts[0].trim();
                String hourStr = parts[1].trim();
                String minuteStr = parts[2].trim();
                int index = RecognizerUtils.getIndexFromDay(month);
                int dayOfMonth = RecognizerUtils.getDayFromString(month);
                res = month.substring(0, index);
                String date = null;
                if (!res.matches("")) {
                    int indexEnd;
                    int monthOfYear = RecognizerUtils.getMonthFromString(month);

                    indexStart = keyStr.lastIndexOf(month);
                    indexEnd = keyStr.lastIndexOf(hourStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + month.length(), indexEnd).trim();
                    }
                    int hourOfDay = Integer.parseInt(date);

                    indexStart = keyStr.lastIndexOf(hourStr);
                    indexEnd = keyStr.lastIndexOf(minuteStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + hourStr.length(), indexEnd).trim();
                    }
                    int minuteOfHour = Integer.parseInt(date);
                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, monthOfYear);
                    if (isDateValid && isTimeValid){
                        Calendar calendar = Calendar.getInstance();
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (monthOfYear < mMonth ||
                                (monthOfYear == mMonth && mDay > dayOfMonth) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour > hourOfDay) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour == hourOfDay && mMinute > minuteOfHour)) {
                            mYear = mYear + 1;
                        }
                        saveReminder(res, isWidget, export, mDay, mMonth, mYear, hourOfDay, minuteOfHour, 0);
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.wrong_date_time_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void fullDateTask(String keyStr, boolean isWidget) {
        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        int indexStart;
        if (keyStr.matches(".* (jan|feb|mar|apr|ma|jun|jul|aug|sep|oct|nov|dec).* \\d\\d?t?h? at \\d\\d? o'clock \\d\\d? minutes?.*")){
            String[] parts = keyStr.split(" \\d\\d?");
            String res = null;
            if (parts.length > 0) {
                String overall = parts[0].trim();
                String month = parts[1].trim();
                String hourStr = parts[2].trim();
                String minuteStr = parts[3].trim();
                indexStart = RecognizerUtils.getIndexForMonth(overall);
                if (indexStart != -1){
                    res = overall.substring(0, indexStart).trim();
                }
                String date = null;
                if (res != null && !res.matches("")) {
                    indexStart = keyStr.lastIndexOf(overall);
                    int indexEnd = keyStr.lastIndexOf(month);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + overall.length(), indexEnd).trim();
                    }
                    int dayOfMonth = Integer.parseInt(date);
                    int monthOfYear = RecognizerUtils.getMonthFromString(overall);

                    indexStart = keyStr.lastIndexOf(month);
                    indexEnd = keyStr.lastIndexOf(hourStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + month.length(), indexEnd).trim();
                    }
                    int hourOfDay = Integer.parseInt(date);

                    indexStart = keyStr.lastIndexOf(hourStr);
                    indexEnd = keyStr.lastIndexOf(minuteStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + hourStr.length(), indexEnd).trim();
                    }
                    int minuteOfHour = Integer.parseInt(date);
                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, monthOfYear);
                    if (isDateValid && isTimeValid){
                        Calendar calendar = Calendar.getInstance();
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (monthOfYear < mMonth ||
                                (monthOfYear == mMonth && mDay > dayOfMonth) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour > hourOfDay) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour == hourOfDay && mMinute > minuteOfHour)) {
                            mYear = mYear + 1;
                        }
                        saveReminder(res, isWidget, export, mDay, mMonth, mYear, hourOfDay, minuteOfHour, 0);
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.wrong_date_time_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (keyStr.matches(".* \\d\\d? (січ|лют|бер|квіт|трав|черв|лип|серп|вер|жовт|лист|груд).* об? \\d\\d? годині? \\d\\d? хвилин.*")){
            String[] parts = keyStr.split(" \\d\\d? ");
            String res;
            if (parts.length > 0) {
                res = parts[0].trim();
                String month = parts[1].trim();
                String hourStr = parts[2].trim();
                String minuteStr = parts[3].trim();
                String date = null;
                if (!res.matches("")) {
                    indexStart = keyStr.lastIndexOf(res);
                    int indexEnd = keyStr.lastIndexOf(month);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + res.length(), indexEnd).trim();
                    }
                    int dayOfMonth = Integer.parseInt(date);
                    int monthOfYear = RecognizerUtils.getMonthFromString(month);

                    indexStart = keyStr.lastIndexOf(month);
                    indexEnd = keyStr.lastIndexOf(hourStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + month.length(), indexEnd).trim();
                    }
                    int hourOfDay = Integer.parseInt(date);

                    indexStart = keyStr.lastIndexOf(hourStr);
                    indexEnd = keyStr.lastIndexOf(minuteStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + hourStr.length(), indexEnd).trim();
                    }
                    int minuteOfHour = Integer.parseInt(date);
                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, monthOfYear);
                    if (isDateValid && isTimeValid){
                        Calendar calendar = Calendar.getInstance();
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (monthOfYear < mMonth ||
                                (monthOfYear == mMonth && mDay > dayOfMonth) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour > hourOfDay) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour == hourOfDay && mMinute > minuteOfHour)) {
                            mYear = mYear + 1;
                        }
                        saveReminder(res, isWidget, export, mDay, mMonth, mYear, hourOfDay, minuteOfHour, 0);
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.wrong_date_time_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (keyStr.matches(".* \\d\\d? (янв|февр|мар|апр|ма|июн|июл|авг|сен|окт|ноя|дек).* в? ?\\d\\d? часов \\d\\d? минут.*")){
            String[] parts = keyStr.split(" \\d\\d? ");
            String res;
            if (parts.length > 0) {
                res = parts[0].trim();
                String month = parts[1].trim();
                String hourStr = parts[2].trim();
                String minuteStr = parts[3].trim();
                String date = null;
                if (!res.matches("")) {
                    indexStart = keyStr.lastIndexOf(res);
                    int indexEnd = keyStr.lastIndexOf(month);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + res.length(), indexEnd).trim();
                    }
                    int dayOfMonth = Integer.parseInt(date);
                    int monthOfYear = RecognizerUtils.getMonthFromString(month);

                    indexStart = keyStr.lastIndexOf(month);
                    indexEnd = keyStr.lastIndexOf(hourStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + month.length(), indexEnd).trim();
                    }
                    int hourOfDay = Integer.parseInt(date);

                    indexStart = keyStr.lastIndexOf(hourStr);
                    indexEnd = keyStr.lastIndexOf(minuteStr);
                    if (indexStart != -1) {
                        date = keyStr.substring(indexStart + hourStr.length(), indexEnd).trim();
                    }
                    int minuteOfHour = Integer.parseInt(date);
                    boolean isTimeValid = RecognizerUtils.isCorrectTime(hourOfDay, minuteOfHour);
                    boolean isDateValid = checkDate(dayOfMonth, monthOfYear);
                    if (isDateValid && isTimeValid){
                        Calendar calendar = Calendar.getInstance();
                        int mMonth = calendar.get(Calendar.MONTH);
                        int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                        int mYear = calendar.get(Calendar.YEAR);
                        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int mMinute = calendar.get(Calendar.MINUTE);
                        if (monthOfYear < mMonth ||
                                (monthOfYear == mMonth && mDay > dayOfMonth) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour > hourOfDay) ||
                                (monthOfYear == mMonth && mDay == dayOfMonth && mHour == hourOfDay && mMinute > minuteOfHour)) {
                            mYear = mYear + 1;
                        }
                        saveReminder(res, isWidget, export, mDay, mMonth, mYear, hourOfDay, minuteOfHour, 0);
                    } else {
                        Toast.makeText(ctx, ctx.getString(R.string.wrong_date_time_message), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private boolean checkDate(int dayOfMonth, int monthOfYear) {
        boolean isValid = false;
        if (monthOfYear != -1) {
            Calendar calendar = Calendar.getInstance();
            int mMonth = calendar.get(Calendar.MONTH);
            int mYear = calendar.get(Calendar.YEAR);
            if (monthOfYear >= mMonth) {
                boolean isLeap = RecognizerUtils.isLeapYear(mYear);
                if (monthOfYear == 0 && dayOfMonth < 32){
                    isValid = true;
                } else if (monthOfYear == 1){
                    isValid = isLeap && dayOfMonth < 30 || !isLeap && dayOfMonth < 29;
                } else
                    isValid = monthOfYear == 2 && dayOfMonth < 32 ||
                            monthOfYear == 3 && dayOfMonth < 31 ||
                            monthOfYear == 4 && dayOfMonth < 32 ||
                            monthOfYear == 5 && dayOfMonth < 31 ||
                            monthOfYear == 6 && dayOfMonth < 32 ||
                            monthOfYear == 7 && dayOfMonth < 32 ||
                            monthOfYear == 8 && dayOfMonth < 31 ||
                            monthOfYear == 9 && dayOfMonth < 32 ||
                            monthOfYear == 10 && dayOfMonth < 31 ||
                            monthOfYear == 11 && dayOfMonth < 32;
            } else {
                boolean isLeap = RecognizerUtils.isLeapYear(mYear + 1);
                if (monthOfYear == 0 && dayOfMonth < 32){
                    isValid = true;
                } else if (monthOfYear == 1){
                    isValid = isLeap && dayOfMonth < 30 || !isLeap && dayOfMonth < 29;
                } else
                    isValid = monthOfYear == 2 && dayOfMonth < 32 ||
                            monthOfYear == 3 && dayOfMonth < 31 ||
                            monthOfYear == 4 && dayOfMonth < 32 ||
                            monthOfYear == 5 && dayOfMonth < 31 ||
                            monthOfYear == 6 && dayOfMonth < 32 ||
                            monthOfYear == 7 && dayOfMonth < 32 ||
                            monthOfYear == 8 && dayOfMonth < 31 ||
                            monthOfYear == 9 && dayOfMonth < 32 ||
                            monthOfYear == 10 && dayOfMonth < 31 ||
                            monthOfYear == 11 && dayOfMonth < 32;
            }
        }
        return isValid;
    }

    private void nightTask(String keyStr, boolean isWidget) {
        int indexStart;
        boolean isTomorrow;
        boolean isWeekDay;
        SharedPrefs prefs = new SharedPrefs(ctx);
        String mTime = prefs.loadPrefs(Prefs.TIME_NIGHT);
        Date date = null;
        try {
            date = mFormat.parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        if (keyStr.matches(".* night.*")){
            if (keyStr.matches(".* next.*")){
                indexStart = keyStr.lastIndexOf(" at next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" in next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" next");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* tomorrow.*")){
                indexStart = keyStr.lastIndexOf(" tomorrow");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" at night");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" night");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* вночі.*")) {
            if (keyStr.matches(".* (наступну|наступний|наступного).*")){
                indexStart = keyStr.lastIndexOf(" в наступн");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" наступн");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра.*")){
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" вночі");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* ночью.*")) {
            if (keyStr.matches(".* (следующую|следующий|следующее).*")){
                indexStart = keyStr.lastIndexOf(" в следую");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" следую");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра ввечері.*")){
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" ночью");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        }
    }

    private void dayTask(String keyStr, boolean isWidget) {
        int indexStart;
        boolean isTomorrow;
        boolean isWeekDay;
        SharedPrefs prefs = new SharedPrefs(ctx);
        String mTime = prefs.loadPrefs(Prefs.TIME_DAY);
        Date date = null;
        try {
            date = mFormat.parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        if (keyStr.matches(".* afternoon.*")){
            if (keyStr.matches(".* next.*")){
                indexStart = keyStr.lastIndexOf(" at next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" in next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" next");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* tomorrow afternoon.*")){
                indexStart = keyStr.lastIndexOf(" tomorrow");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" afternoon");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* вдень.*")) {
            if (keyStr.matches(".* (наступну|наступний|наступного).*")){
                indexStart = keyStr.lastIndexOf(" в наступн");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" наступн");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра вдень.*")){
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" вдень");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* днем.*")) {
            if (keyStr.matches(".* (следующую|следующий|следующее).*")){
                indexStart = keyStr.lastIndexOf(" в след");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" следую");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра ввечері.*")){
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" днем");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        }
    }

    private void morningTask(String keyStr, boolean isWidget) {
        int indexStart;
        boolean isTomorrow;
        boolean isWeekDay;
        SharedPrefs prefs = new SharedPrefs(ctx);
        String mTime = prefs.loadPrefs(Prefs.TIME_MORNING);
        Date date = null;
        try {
            date = mFormat.parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        if (keyStr.matches(".* morning.*")){
            if (keyStr.matches(".* next.*")){
                indexStart = keyStr.lastIndexOf(" at next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" in next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" next");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* tomorrow tonight.*")){
                indexStart = keyStr.lastIndexOf(" tomorrow");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" in the morning");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" at the morning");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" morning");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* вранці.*") || keyStr.matches(".* зранку.*")) {
            if (keyStr.matches(".* (наступну|наступний|наступного).*")){
                indexStart = keyStr.lastIndexOf(" в наступн");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" наступн");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра.*")){
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" вранці");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" зранку");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* утром?.*")) {
            if (keyStr.matches(".* (следующую|следующий|следующее).*")){
                indexStart = keyStr.lastIndexOf(" в следую");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" следую");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра утром.*")){
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" утром");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" утро");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow){
                delta = 1;
            } else if (isWeekDay){
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        }
    }

    private void eveningTask(String keyStr, boolean isWidget) {
        int indexStart;
        boolean isTomorrow;
        boolean isWeekDay;
        SharedPrefs prefs = new SharedPrefs(ctx);
        String mTime = prefs.loadPrefs(Prefs.TIME_EVENING);
        Date date = null;
        try {
            date = mFormat.parse(mTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        if (keyStr.matches(".* tonight.*")) {
            if (keyStr.matches(".* next.*")) {
                indexStart = keyStr.lastIndexOf(" at next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" in next");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" next");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* tomorrow.*")) {
                indexStart = keyStr.lastIndexOf(" tomorrow");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" tonight");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow) {
                delta = 1;
            } else if (isWeekDay) {
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* ввечері.*")) {
            if (keyStr.matches(".* (наступну|наступний|наступного).*")) {
                indexStart = keyStr.lastIndexOf(" в наступн");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" наступн");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра.*")) {
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" ввечері");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow) {
                delta = 1;
            } else if (isWeekDay) {
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        } else if (keyStr.matches(".* вечером.*")) {
            if (keyStr.matches(".* (следующую|следующий|следующее).*")) {
                indexStart = keyStr.lastIndexOf(" в следую");
                if (indexStart == -1) indexStart = keyStr.lastIndexOf(" следую");
                isWeekDay = true;
                isTomorrow = false;
            } else if (keyStr.matches(".* завтра.*")) {
                indexStart = keyStr.lastIndexOf(" завтра");
                isTomorrow = true;
                isWeekDay = false;
            } else {
                indexStart = keyStr.lastIndexOf(" вечером");
                isTomorrow = false;
                isWeekDay = false;
            }

            String res = null;
            if (indexStart != -1) {
                res = keyStr.substring(0, indexStart).trim();
            }

            Calendar calendar1 = Calendar.getInstance();
            int currentDayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK);

            int delta;
            if (isTomorrow) {
                delta = 1;
            } else if (isWeekDay) {
                int mDay = RecognizerUtils.getWeekDay(keyStr);
                delta = mDay - currentDayOfWeek;
                if (delta < 0) delta = 7 + delta;
            } else {
                delta = 0;
            }

            calendar1.setTimeInMillis(calendar1.getTimeInMillis() + delta * dayLong);

            int currentDay = calendar1.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar1.get(Calendar.MONTH);
            int currentYear = calendar1.get(Calendar.YEAR);

            saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, mHour, mMinute, 0);
        }
    }

    private void showBirthdays(String keyStr) {
        Pattern pattern = Pattern.compile("\\d\\d?\\d?");
        Matcher matcher = pattern.matcher(keyStr);
        if (matcher.find()) {
            String time = matcher.group().trim();

            new loadAsync(ctx).execute(time);
        }
    }

    public class loadAsync extends AsyncTask<String, Void, Void>{

        Context mContext;
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<Integer> years = new ArrayList<>();
        String days;

        public loadAsync(Context context){
            this.mContext = context;
        }

        @Override
        protected Void doInBackground(String... params) {
            String time = params[0];
            days = time;
            int days = Integer.parseInt(time);
            DataBase db = new DataBase(mContext);
            Calendar calendar = Calendar.getInstance();
            int mDay;
            int mMonth;

            names.clear();
            dates.clear();
            years.clear();

            int i = 0;
            do {
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mMonth = calendar.get(Calendar.MONTH);
                Cursor cursor = db.getBirthdays(mDay, mMonth);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String birthday = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                        String name = cursor.getString(cursor.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                        dates.add(birthday);
                        names.add(name);
                        years.add(getYears(birthday));
                    } while (cursor.moveToNext());
                }
                if (cursor != null) {
                    cursor.close();
                }
                calendar.setTimeInMillis(calendar.getTimeInMillis() + (1000 * 60 * 60 * 24));
                i++;
            } while (i <= days);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (names.size() > 0 && dates.size() > 0 && years.size() > 0) {
                ctx.startActivity(new Intent(mContext, BirthdaysVoiceList.class)
                        .putExtra("names", names)
                        .putExtra("dates", dates)
                        .putExtra("years", years));
            } else {
                Toast.makeText(mContext, mContext.getString(R.string.birthdays_string_first) + " " +
                        days + " " + mContext.getString(R.string.birthdays_string_second), Toast.LENGTH_SHORT);
            }
        }
    }

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private int getYears(String dateOfBirth){
        int years;
        Date date = null;
        try {
            date = format.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int yearOfBirth = calendar.get(Calendar.YEAR);

        Calendar calendar1 = Calendar.getInstance();
        calendar1.getTimeInMillis();
        int currentYear = calendar1.get(Calendar.YEAR);
        years = currentYear - yearOfBirth;
        return years;
    }

    private void saveNote(String keyStr) {
        int indexStart = 0;
        if (keyStr.matches(".*note .*")){
            indexStart = keyStr.lastIndexOf("note") + 4;
        } else if (keyStr.matches(".*нотатка .*")) {
            indexStart = keyStr.lastIndexOf("нотатка") + 7;
        } else if (keyStr.matches(".*заметка .*")) {
            indexStart = keyStr.lastIndexOf("заметка") + 7;
        }
        String res = null;
        if (indexStart != -1) {
            res = keyStr.substring(indexStart).trim();
        }

        sHelp = new SyncHelper(ctx);
        sPrefs = new SharedPrefs(ctx);
        ColorSetter cs = new ColorSetter(ctx);
        Calendar calendar1 = Calendar.getInstance();
        int day = calendar1.get(Calendar.DAY_OF_MONTH);
        int month = calendar1.get(Calendar.MONTH);
        int year = calendar1.get(Calendar.YEAR);
        int hour = calendar1.get(Calendar.HOUR_OF_DAY);
        int minute = calendar1.get(Calendar.MINUTE);
        String date = day + "-" + month + "-" + year;

        String uuID = sHelp.generateID();
        NotesBase db = new NotesBase(ctx);
        db.open();
        if (sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            db.saveNote(sHelp.encrypt(res), date, cs.getNoteColor(12), uuID, null, 5);
        } else {
            db.saveNote(res, date, cs.getNoteColor(12), uuID, null, 5);
        }

        if (sPrefs.loadBoolean(Prefs.QUICK_NOTE_REMINDER)){
            DB = new DataBase(ctx);
            DB.open();
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            long id = DB.insertReminder(res, Constants.TYPE_TIME, day, month, year, hour, minute, 0, null,
                    0, sPrefs.loadInt(Prefs.QUICK_NOTE_REMINDER_TIME),
                    0, 0, 0, SyncHelper.generateID(), null, 0, null, 0, 0, 0, categoryId);
            alarm.setAlarm(ctx, id);
            DB.updateReminderDateTime(id);
            new UpdatesHelper(ctx).updateWidget();
        }
        new UpdatesHelper(ctx).updateNotesWidget();
        Toast.makeText(ctx, ctx.getString(R.string.note_saved_toast), Toast.LENGTH_SHORT).show();
    }

    private void messageTask(String keyStr, boolean isWidget) {
        int indexStart = 0;
        int indexEnd = 0;
        if (keyStr.matches(".*send .* to .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("to ");
            indexEnd = keyStr.indexOf(" at");
        } else if (keyStr.matches(".*надіслати .* до .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("до ");
            indexEnd = keyStr.indexOf(" о");
        } else if (keyStr.matches(".*отправить .* до .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("до ");
            indexEnd = keyStr.indexOf(" в");
        }

        String user = null;
        if (indexStart != -1 && indexEnd != -1) {
            user = keyStr.substring(indexStart, indexEnd).trim();
        }

        Contacts contacts = new Contacts(ctx);
        String number = Contacts.get_Number(user, ctx);

        if (keyStr.matches(".*send .* to .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("send ");
            indexEnd = keyStr.indexOf(" to");
        } else if (keyStr.matches(".*надіслати .* до .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("надіслати ");
            indexEnd = keyStr.indexOf(" до");
        } else if (keyStr.matches(".*отправить .* до .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("отправить ");
            indexEnd = keyStr.indexOf(" до");
        }

        String text = null;
        if (indexStart != -1 && indexEnd != -1) {
            text = keyStr.substring(indexStart, indexEnd).trim();
        }

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        /*if (keyStr.matches(".*(p|a?m).*") && locale){
            pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        }*/
        Matcher matcher = pattern.matcher(keyStr);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format:dateTaskFormats){
                Date date;
                Calendar calendar = Calendar.getInstance();
                if (keyStr.matches(".*tomorrow.*") || keyStr.matches(".*завтра.*") || keyStr.matches(".*завтра.*")) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + dayLong);
                }
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                try {
                    date = format.parse(time);
                    if (date != null){
                        calendar.setTime(date);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        DB = new DataBase(ctx);
                        DB.open();
                        sHelp = new SyncHelper(ctx);
                        String uuID = SyncHelper.generateID();
                        sPrefs = new SharedPrefs(ctx);
                        long id;
                        Cursor cf = DB.queryCategories();
                        String categoryId = null;
                        if (cf != null && cf.moveToFirst()) {
                            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        }
                        if (cf != null) cf.close();
                        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)) && export) {
                            id = DB.insertReminder(text, Constants.TYPE_MESSAGE,
                                    currentDay, currentMonth, currentYear, hour, minute, 0, number, 0, 0, 0, 0, 0, uuID, null,
                                    1, null, 0, 0, 0, categoryId);
                            exportToCalendar(user, ReminderUtils.getTime(currentDay, currentMonth, currentYear, hour, minute, 0), id);
                        } else {
                            id = DB.insertReminder(text, Constants.TYPE_MESSAGE,
                                    currentDay, currentMonth, currentYear, hour, minute, 0, number, 0, 0, 0, 0, 0, uuID, null,
                                    0, null, 0, 0, 0, categoryId);
                        }
                        DB.updateReminderDateTime(id);
                        alarm.setAlarm(ctx, id);
                        DB.close();
                        updatesHelper = new UpdatesHelper(ctx);
                        updatesHelper.updateWidget();
                        showResult(id, isWidget);
                        break;
                    }
                } catch (NullPointerException | ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void callTask(String keyStr, boolean isWidget) {
        int indexStart = 0;
        int indexEnd = 0;
        if (keyStr.matches(".*call .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("call ");
            indexEnd = keyStr.indexOf(" at");
        } else if (keyStr.matches(".*подзвонит?и? .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("подзвони ");
            if (indexStart == -1) indexStart = keyStr.lastIndexOf("подзвонити ");
            indexEnd = keyStr.indexOf(" о");
        } else if (keyStr.matches(".*позвонит?ь? .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("позвони ");
            if (indexStart == -1) indexStart = keyStr.lastIndexOf("позвонить ");
            indexEnd = keyStr.indexOf(" в");
        }

        String user = null;
        if (indexStart != -1 && indexEnd != -1) {
            user = keyStr.substring(indexStart, indexEnd).trim();
        }

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        Contacts contacts = new Contacts(ctx);
        String number = Contacts.get_Number(user, ctx);

        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        /*if (keyStr.matches(".*(p|a?m).*") && locale){
            pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        }*/
        Matcher matcher = pattern.matcher(keyStr);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format:dateTaskFormats){
                Date date;
                Calendar calendar = Calendar.getInstance();
                if (keyStr.matches(".*tomorrow.*") || keyStr.matches(".*завтра.*") || keyStr.matches(".*завтра.*")) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + dayLong);
                }
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                try {
                    date = format.parse(time);
                    if (date != null){
                        calendar.setTime(date);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        DB = new DataBase(ctx);
                        DB.open();
                        sHelp = new SyncHelper(ctx);
                        String uuID = SyncHelper.generateID();
                        sPrefs = new SharedPrefs(ctx);
                        long id;
                        Cursor cf = DB.queryCategories();
                        String categoryId = null;
                        if (cf != null && cf.moveToFirst()) {
                            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        }
                        if (cf != null) cf.close();
                        if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                                sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)) && export) {
                            id = DB.insertReminder(user, Constants.TYPE_CALL,
                                    currentDay, currentMonth, currentYear, hour, minute, 0, number,
                                    0, 0, 0, 0, 0, uuID, null,
                                    1, null, 0, 0, 0, categoryId);
                            exportToCalendar(user, ReminderUtils.getTime(currentDay, currentMonth,
                                    currentYear, hour, minute, 0), id);
                        } else {
                            id = DB.insertReminder(user, Constants.TYPE_CALL,
                                    currentDay, currentMonth, currentYear, hour, minute, 0, number,
                                    0, 0, 0, 0, 0, uuID, null,
                                    0, null, 0, 0, 0, categoryId);
                        }
                        DB.updateReminderDateTime(id);
                        alarm.setAlarm(ctx, id);
                        DB.close();
                        updatesHelper = new UpdatesHelper(ctx);
                        updatesHelper.updateWidget();
                        showResult(id, isWidget);
                        break;
                    }
                } catch (NullPointerException | ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void weekTask(String keyStr, boolean isWidget) {
        int index = 0;
        String weekdays = null;
        if (keyStr.matches(".* in .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")){
            index = keyStr.indexOf(" in");
            weekdays = RecognizerUtils.getDays(keyStr, "en");
        } else if (keyStr.matches(".* у .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            index = keyStr.indexOf(" у");
            weekdays = RecognizerUtils.getDays(keyStr, "uk");
        } else if (keyStr.matches(".* во? .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            index = keyStr.indexOf(" в");
            weekdays = RecognizerUtils.getDays(keyStr, "ru");
        }
        String res = null;
        if (index != -1) {
            res = keyStr.substring(0, index).trim();
        }

        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        /*if (keyStr.matches(".*(p|a?m).*") && locale){
            pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        }*/
        Matcher matcher = pattern.matcher(keyStr);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format:dateTaskFormats){
                Date date;
                Calendar calendar = Calendar.getInstance();
                try {
                    date = format.parse(time);
                    if (date != null){
                        calendar.setTime(date);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        DB = new DataBase(ctx);
                        DB.open();
                        Cursor cf = DB.queryCategories();
                        String categoryId = null;
                        if (cf != null && cf.moveToFirst()) {
                            categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                        }
                        if (cf != null) cf.close();
                        sHelp = new SyncHelper(ctx);
                        String uuID = SyncHelper.generateID();
                        long id = DB.insertReminder(res, Constants.TYPE_WEEKDAY, 0, 0, 0,
                                hour, minute, 0, null, 0, 0, 0, 0, 0, uuID, weekdays, 0, null, 0, 0,
                                0, categoryId);
                        new WeekDayReceiver().setAlarm(ctx, id);
                        DB.close();
                        updatesHelper = new UpdatesHelper(ctx);
                        updatesHelper.updateWidget();
                        showResult(id, isWidget);
                        break;
                    }
                } catch (NullPointerException | ParseException e){
                    e.printStackTrace();
                }
            }

        }
    }

    private void timeTask(String keyStr, boolean isWidget) {
        int indexStart = 0;
        int indexEnd = 0;
        if (keyStr.matches(".*remind .* after \\d\\d?\\d? (hour|minute)s?.*")) {
            indexStart = keyStr.lastIndexOf("remind") + 6;
            indexEnd = keyStr.indexOf(" after");
        } else if (keyStr.matches(".*нагадай?т?и? .* через \\d\\d?\\d? (годин|хвилин)и?у?.*")) {
            indexStart = keyStr.lastIndexOf("нагадай") + 8;
            if (indexStart == -1) indexStart = keyStr.lastIndexOf("нагадати") + 9;
            indexEnd = keyStr.indexOf(" через");
        } else if (keyStr.matches(".*напомнит?ь? .* через \\d\\d?\\d? (час|минут)ы?о?в?у?.*")) {
            indexStart = keyStr.lastIndexOf("напомни") + 7;
            if (indexStart == -1) indexStart = keyStr.lastIndexOf("напомнить") + 9;
            indexEnd = keyStr.indexOf(" через");
        }

        String res = null;
        if (indexStart != -1 && indexEnd != -1) {
            res = keyStr.substring(indexStart, indexEnd).trim();
        }

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        Pattern pattern = Pattern.compile("\\d\\d?\\d?");
        Matcher matcher = pattern.matcher(keyStr);
        if (matcher.find()) {
            String time = matcher.group().trim();
            if (keyStr.matches(".* hours?.*") ||
                    keyStr.matches(".* години?у?.*") ||
                    keyStr.matches(".* часо?в?.*")){
                int hour = Integer.parseInt(time);
                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                int currentSeconds = calendar.get(Calendar.SECOND);
                long minute = hour * 60 * 1000;
                DB = new DataBase(ctx);
                DB.open();
                sHelp = new SyncHelper(ctx);
                String uuID = SyncHelper.generateID();
                sPrefs = new SharedPrefs(ctx);
                long id;
                Cursor cf = DB.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                        sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)) && export) {
                    id = DB.insertReminder(res, Constants.TYPE_TIME,
                            currentDay, currentMonth, currentYear, currentHour,
                            currentMinute, currentSeconds, null, 0, minute, 0, 0, 0, uuID, null, 1,
                            null, 0, 0, 0, categoryId);
                    exportToCalendar(res, ReminderUtils.getTime(currentDay, currentMonth, currentYear,
                            currentHour, currentMinute, minute), id);
                } else {
                    id = DB.insertReminder(res, Constants.TYPE_TIME,
                            currentDay, currentMonth, currentYear, currentHour,
                            currentMinute, currentSeconds, null, 0, minute, 0, 0, 0, uuID, null, 0,
                            null, 0, 0, 0, categoryId);
                }
                DB.updateReminderDateTime(id);
                alarm.setAlarm(ctx, id);
                DB.close();
                updatesHelper = new UpdatesHelper(ctx);
                updatesHelper.updateWidget();
                showResult(id, isWidget);
            } else if (keyStr.matches(".* minutes?.*") ||
                    keyStr.matches(".* хвилини?у?.*") ||
                    keyStr.matches(".* минуты?у?.*")){
                int hour = Integer.parseInt(time);
                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                int currentMinute = calendar.get(Calendar.MINUTE);
                int currentSeconds = calendar.get(Calendar.SECOND);
                long minute = hour * 1000;
                DB = new DataBase(ctx);
                DB.open();
                sHelp = new SyncHelper(ctx);
                String uuID = SyncHelper.generateID();
                sPrefs = new SharedPrefs(ctx);
                long id;
                Cursor cf = DB.queryCategories();
                String categoryId = null;
                if (cf != null && cf.moveToFirst()) {
                    categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
                }
                if (cf != null) cf.close();
                if ((sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR) ||
                        sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)) && export) {
                    id = DB.insertReminder(res, Constants.TYPE_TIME,
                            currentDay, currentMonth, currentYear, currentHour,
                            currentMinute, currentSeconds, null, 0, minute, 0, 0, 0, uuID, null,
                            1, null, 0, 0, 0, categoryId);
                    exportToCalendar(res, ReminderUtils.getTime(currentDay, currentMonth, currentYear,
                            currentHour, currentMinute, minute), id);
                } else {
                    id = DB.insertReminder(res, Constants.TYPE_TIME,
                            currentDay, currentMonth, currentYear, currentHour,
                            currentMinute, currentSeconds, null, 0, minute, 0, 0, 0, uuID, null,
                            0, null, 0, 0, 0, categoryId);
                }
                DB.updateReminderDateTime(id);
                alarm.setAlarm(ctx, id);
                DB.close();
                updatesHelper = new UpdatesHelper(ctx);
                updatesHelper.updateWidget();
                showResult(id, isWidget);
            }
        }
    }

    private void dateTask(String keyStr, boolean isWidget) {
        int indexStart = 0;
        int indexEnd = 0;
        if (keyStr.matches(".*remind .* at [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")){
            indexStart = keyStr.lastIndexOf("remind") + 6;
            indexEnd = keyStr.indexOf(" at");
        } else if (keyStr.matches(".*нагадай?т?и? .* об? [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("нагадай") + 7;
            if (indexStart == -1) indexStart = keyStr.lastIndexOf("нагадати") + 8;
            indexEnd = keyStr.indexOf(" о");
        } else if (keyStr.matches(".*напомнит?ь? .* в [0-9][0-9]?(:? ?)[0-9]?[0-9]? .*")) {
            indexStart = keyStr.lastIndexOf("напомни") + 7;
            if (indexStart == -1) indexStart = keyStr.lastIndexOf("напомнить") + 9;
            indexEnd = keyStr.indexOf(" в ");
        }
        String res = null;
        if (indexStart != -1 && indexEnd != -1) {
            res = keyStr.substring(indexStart, indexEnd).trim();
        }

        boolean export = RecognizerUtils.isCalendarExportable(keyStr);

        Pattern pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        /*if (keyStr.matches(".*(p|a?m).*") && locale){
            pattern = Pattern.compile("([01]?\\d|2[0-3]) ?(([0-5]?\\d?)?)");
        }*/
        Matcher matcher = pattern.matcher(keyStr);
        if (matcher.find()) {
            String time = matcher.group().trim();
            for (SimpleDateFormat format:dateTaskFormats){
                Date date;
                Calendar calendar = Calendar.getInstance();
                if (keyStr.matches(".*tomorrow.*") || keyStr.matches(".*завтра.*") || keyStr.matches(".*завтра.*")) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + dayLong);
                }
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH);
                int currentYear = calendar.get(Calendar.YEAR);
                try {
                    date = format.parse(time);
                    if (date != null){
                        calendar.setTime(date);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        saveReminder(res, isWidget, export, currentDay, currentMonth, currentYear, hour, minute, 0);
                        break;
                    }
                } catch (NullPointerException | ParseException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void showResult(long id, boolean isWidget) {
        if (isWidget) {
            ctx.startActivity(new Intent(ctx, VoiceResult.class)
                    .putExtra("ids", id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.reminder_saved_title), Toast.LENGTH_SHORT).show();
        }
    }

    private void exportToCalendar(String summary, long startTime, long id){
        sPrefs = new SharedPrefs(ctx);
        if (sPrefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR)){
            new CalendarManager(ctx).addEvent(summary, startTime, id);
        }
        if (sPrefs.loadBoolean(Prefs.EXPORT_TO_STOCK)){
            new CalendarManager(ctx).addEventToStock(summary, startTime);
        }
    }
}