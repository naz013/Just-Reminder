package com.cray.software.justreminder.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.note.NotesBase;
import com.cray.software.justreminder.dialogs.utils.NoteReminderTime;
import com.cray.software.justreminder.dialogs.utils.TextSize;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;

public class NotesSettingsFragment extends Fragment implements View.OnClickListener {

    private CheckBox encryptNoteCheck, noteReminderCheck, backupNoteCheck, deleteFileCheck;
    private TextView noteReminderTime;
    private SharedPrefs sPrefs;
    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.notes_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.fragment_notes);
        }

        RelativeLayout encryptNote = (RelativeLayout) rootView.findViewById(R.id.encryptNote);
        encryptNote.setOnClickListener(this);

        encryptNoteCheck = (CheckBox) rootView.findViewById(R.id.encryptNoteCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        encryptNoteCheck.setChecked(sPrefs.loadBoolean(Prefs.NOTE_ENCRYPT));

        RelativeLayout noteReminder = (RelativeLayout) rootView.findViewById(R.id.noteReminder);
        noteReminder.setOnClickListener(this);

        noteReminderCheck = (CheckBox) rootView.findViewById(R.id.noteReminderCheck);
        noteReminderCheck.setChecked(sPrefs.loadBoolean(Prefs.QUICK_NOTE_REMINDER));

        RelativeLayout backupNote = (RelativeLayout) rootView.findViewById(R.id.backupNote);
        backupNote.setOnClickListener(this);

        backupNoteCheck = (CheckBox) rootView.findViewById(R.id.backupNoteCheck);
        backupNoteCheck.setChecked(sPrefs.loadBoolean(Prefs.SYNC_NOTES));

        RelativeLayout deleteFile = (RelativeLayout) rootView.findViewById(R.id.deleteFile);
        deleteFile.setOnClickListener(this);

        deleteFileCheck = (CheckBox) rootView.findViewById(R.id.deleteFileCheck);
        deleteFileCheck.setChecked(sPrefs.loadBoolean(Prefs.DELETE_NOTE_FILE));

        noteReminderTime = (TextView) rootView.findViewById(R.id.noteReminderTime);
        noteReminderTime.setOnClickListener(this);

        TextView textSize = (TextView) rootView.findViewById(R.id.textSize);
        textSize.setOnClickListener(this);

        checkEnables();
        return rootView;
    }

    private void checkEnables(){
        if (noteReminderCheck.isChecked()){
            noteReminderTime.setEnabled(true);
        } else {
            noteReminderTime.setEnabled(false);
        }
    }

    private void encryptNoteChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (encryptNoteCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.NOTE_ENCRYPT, false);
            encryptNoteCheck.setChecked(false);
            new decryptNotes(getActivity()).execute();
        } else {
            sPrefs.saveBoolean(Prefs.NOTE_ENCRYPT, true);
            encryptNoteCheck.setChecked(true);
            new encryptNotes(getActivity()).execute();
        }
    }

    private void backupChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (backupNoteCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.SYNC_NOTES, false);
            backupNoteCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SYNC_NOTES, true);
            backupNoteCheck.setChecked(true);
        }
    }

    private void deleteChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (deleteFileCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.DELETE_NOTE_FILE, false);
            deleteFileCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.DELETE_NOTE_FILE, true);
            deleteFileCheck.setChecked(true);
        }
    }

    private void noteReminderChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (noteReminderCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.QUICK_NOTE_REMINDER, false);
            noteReminderCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.QUICK_NOTE_REMINDER, true);
            noteReminderCheck.setChecked(true);
        }
        checkEnables();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.encryptNote:
                encryptNoteChange();
                break;
            case R.id.backupNote:
                backupChange();
                break;
            case R.id.noteReminder:
                noteReminderChange();
                break;
            case R.id.deleteFile:
                deleteChange();
                break;
            case R.id.noteReminderTime:
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), NoteReminderTime.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
            case R.id.textSize:
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), TextSize.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }

    class encryptNotes extends AsyncTask<Void, Void, Integer> {

        ProgressDialog pd;
        Context tContext;
        NotesBase db;

        public encryptNotes(Context context) {
            this.tContext = context;
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.setMessage(tContext.getString(R.string.encrypting_notes));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int i = 0;
            db = new NotesBase(tContext);
            SyncHelper helper = new SyncHelper(tContext);
            db.open();
            Cursor c = db.getNotes();
            if (c != null && c.moveToFirst()){
                do {
                    String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    String encrypted = helper.encrypt(note);
                    db.updateNote(id, encrypted);
                } while (c.moveToNext());
            }
            return i;
        }

        @Override
        protected void onPostExecute(Integer files) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    class decryptNotes extends AsyncTask<Void, Void, Integer> {

        ProgressDialog pd;
        Context tContext;
        NotesBase db;

        public decryptNotes(Context context) {
            this.tContext = context;
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.setMessage(tContext.getString(R.string.decrypting_notes_title));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int i = 0;
            db = new NotesBase(tContext);
            SyncHelper helper = new SyncHelper(tContext);
            db.open();
            Cursor c = db.getNotes();
            if (c != null && c.moveToFirst()){
                do {
                    String note = c.getString(c.getColumnIndex(Constants.COLUMN_NOTE));
                    long id = c.getLong(c.getColumnIndex(Constants.COLUMN_ID));
                    String decrypted = helper.decrypt(note);
                    db.updateNote(id, decrypted);
                } while (c.moveToNext());
            }

            return i;
        }

        @Override
        protected void onPostExecute(Integer files) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
