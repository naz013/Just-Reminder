package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.backdoor.simpleai.Model;
import com.backdoor.simpleai.RecUtils;
import com.backdoor.simpleai.Recognizer;
import com.backdoor.simpleai.Types;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.AddBirthday;
import com.cray.software.justreminder.activities.QuickAddReminder;
import com.cray.software.justreminder.activities.SplashScreen;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Language;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.notes.NotesBase;
import com.cray.software.justreminder.datas.models.CategoryModel;
import com.cray.software.justreminder.dialogs.SelectVolume;
import com.cray.software.justreminder.dialogs.VoiceHelp;
import com.cray.software.justreminder.dialogs.VoiceResult;
import com.cray.software.justreminder.feedback.SendReportActivity;
import com.cray.software.justreminder.json.JAction;
import com.cray.software.justreminder.json.JExport;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.json.JRecurrence;
import com.cray.software.justreminder.reminder.DateType;
import com.cray.software.justreminder.reminder.ReminderUtils;
import com.cray.software.justreminder.settings.SettingsActivity;
import com.cray.software.justreminder.utils.TimeUtil;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Recognize {

    private Context mContext;
    private boolean isWear;

    public Recognize(Context context){
        this.mContext = context;
    }

    public void parseResults(ArrayList matches, boolean isWidget, boolean isWear){
        this.isWear = isWear;
        this.parseResults(matches, isWidget);
    }

    public void parseResults(ArrayList matches, boolean isWidget){
        SharedPrefs prefs = new SharedPrefs(mContext);
        String language = Language.getLanguage(prefs.loadInt(Prefs.VOICE_LOCALE));
        for (Object key : matches){
            String keyStr = key.toString();
            Log.d(Constants.LOG_TAG, "Key " + keyStr);
            String morning = prefs.loadPrefs(Prefs.TIME_MORNING);
            String day = prefs.loadPrefs(Prefs.TIME_DAY);
            String evening = prefs.loadPrefs(Prefs.TIME_EVENING);
            String night = prefs.loadPrefs(Prefs.TIME_NIGHT);

            Model model = new Recognizer(mContext, new String[]{morning, day, evening, night}).parseResults(keyStr, language);
            if (model != null){
                Types types = model.getTypes();
                if (types == Types.ACTION && isWidget) {
                    int action = model.getActivity();
                    if (action == RecUtils.APP)
                        mContext.startActivity(new Intent(mContext, SplashScreen.class));
                    else if (action == RecUtils.SETTINGS)
                        mContext.startActivity(new Intent(mContext, SettingsActivity.class));
                    else if (action == RecUtils.REPORT)
                        mContext.startActivity(new Intent(mContext, SendReportActivity.class));
                    else if (action == RecUtils.HELP)
                        mContext.startActivity(new Intent(mContext, VoiceHelp.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                    else if (action == RecUtils.BIRTHDAY)
                        mContext.startActivity(new Intent(mContext, AddBirthday.class));
                    else if (action == RecUtils.REMINDER)
                        mContext.startActivity(new Intent(mContext, QuickAddReminder.class));
                    else mContext.startActivity(new Intent(mContext, SelectVolume.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
                } else if (types == Types.NOTE) {
                    saveNote(model.getSummary());
                } else if (types == Types.REMINDER) {
                    saveReminder(model, isWidget);
                }
                break;
            }
        }
    }

    private void saveReminder(Model model, boolean widget) {
        String type = model.getType();
        String number = model.getNumber();
        String summary = model.getSummary();
        long repeat = model.getRepeat();
        int telephony = model.getAction();
        ArrayList<Integer> weekdays = model.getWeekdays();
        boolean isCalendar = model.getCalendar();
        long startTime = model.getDateTime();

        if (type.matches(Recognizer.WEEK)) {
            startTime = TimeCount.getNextWeekdayTime(startTime, weekdays, 0);
            if (number != null && !number.matches("")) {
                if (telephony == 1) type = Constants.TYPE_WEEKDAY_CALL;
                else type = Constants.TYPE_WEEKDAY_MESSAGE;
            }
        }

        String categoryId = CategoryModel.getDefault(mContext);
        JRecurrence jRecurrence = new JRecurrence(0, repeat, -1, weekdays, 0);
        JAction jAction = new JAction(type, number, -1, "", null);

        SharedPrefs prefs = new SharedPrefs(mContext);
        boolean isCal = prefs.loadBoolean(Prefs.EXPORT_TO_CALENDAR);
        boolean isStock = prefs.loadBoolean(Prefs.EXPORT_TO_STOCK);
        int exp = (isCalendar && (isCal || isStock)) ? 1 : 0;
        JExport jExport = new JExport(0, exp, null);

        Log.d("----RECORD_TIME-----", TimeUtil.getFullDateTime(System.currentTimeMillis(), true));
        Log.d("----EVENT_TIME-----", TimeUtil.getFullDateTime(startTime, true));

        JModel jModel = new JModel(summary, type, categoryId,
                SyncHelper.generateID(), startTime, startTime, jRecurrence, jAction, jExport);
        long remId = new DateType(mContext, Constants.TYPE_REMINDER).save(jModel);
        if (isCalendar || isStock) {
            ReminderUtils.exportToCalendar(mContext, summary, startTime, remId, isCalendar, isStock);
        }

        if (widget && !isWear) {
            mContext.startActivity(new Intent(mContext, VoiceResult.class)
                    .putExtra("ids", remId)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote(String note) {
        SharedPrefs prefs = new SharedPrefs(mContext);
        Calendar calendar1 = Calendar.getInstance();
        int day = calendar1.get(Calendar.DAY_OF_MONTH);
        int month = calendar1.get(Calendar.MONTH);
        int year = calendar1.get(Calendar.YEAR);
        String date = day + "-" + month + "-" + year;

        String uuID = SyncHelper.generateID();
        NotesBase db = new NotesBase(mContext);
        int color = new Random().nextInt(15);
        db.open();
        long id;
        if (prefs.loadBoolean(Prefs.NOTE_ENCRYPT)){
            id = db.saveNote(SyncHelper.encrypt(note), date, color, uuID, null, 5);
        } else {
            id = db.saveNote(note, date, color, uuID, null, 5);
        }

        long remId = 0;
        if (prefs.loadBoolean(Prefs.QUICK_NOTE_REMINDER)){
            DataBase DB = new DataBase(mContext);
            DB.open();
            Cursor cf = DB.queryCategories();
            String categoryId = null;
            if (cf != null && cf.moveToFirst()) {
                categoryId = cf.getString(cf.getColumnIndex(Constants.COLUMN_TECH_VAR));
            }
            if (cf != null) cf.close();
            DB.close();

            long after = prefs.loadInt(Prefs.QUICK_NOTE_REMINDER_TIME) * 1000 * 60;
            long due = calendar1.getTimeInMillis() + after;
            JRecurrence jRecurrence = new JRecurrence(0, 0, -1, null, after);
            JModel jModel = new JModel(note, Constants.TYPE_REMINDER, categoryId,
                    SyncHelper.generateID(), due, due, jRecurrence, null, null);
            remId = new DateType(mContext, Constants.TYPE_REMINDER).save(jModel);
        }
        db.linkToReminder(id, remId);
        db.close();
        new UpdatesHelper(mContext).updateNotesWidget();
        if (!isWear) Toast.makeText(mContext, mContext.getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }
}