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

package com.cray.software.justreminder.settings.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.notes.NoteHelper;
import com.cray.software.justreminder.notes.NoteItem;
import com.cray.software.justreminder.views.PrefsView;

import java.util.List;

public class NotesSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private TextView noteReminderTime;
    private SharedPrefs sPrefs;
    private ActionBar ab;
    private PrefsView encryptNotePrefs, backupNotePrefs, noteReminderPrefs, deleteFilePrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.settings_note, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.notes);
        }
        sPrefs = SharedPrefs.getInstance(getActivity());

        encryptNotePrefs = (PrefsView) rootView.findViewById(R.id.encryptNotePrefs);
        encryptNotePrefs.setChecked(sPrefs.getBoolean(Prefs.NOTE_ENCRYPT));
        encryptNotePrefs.setOnClickListener(this);

        backupNotePrefs = (PrefsView) rootView.findViewById(R.id.backupNotePrefs);
        backupNotePrefs.setChecked(sPrefs.getBoolean(Prefs.SYNC_NOTES));
        backupNotePrefs.setOnClickListener(this);

        noteReminderPrefs = (PrefsView) rootView.findViewById(R.id.noteReminderPrefs);
        noteReminderPrefs.setChecked(sPrefs.getBoolean(Prefs.QUICK_NOTE_REMINDER));
        noteReminderPrefs.setOnClickListener(this);

        deleteFilePrefs = (PrefsView) rootView.findViewById(R.id.deleteFilePrefs);
        deleteFilePrefs.setChecked(sPrefs.getBoolean(Prefs.DELETE_NOTE_FILE));
        deleteFilePrefs.setOnClickListener(this);

        noteReminderTime = (TextView) rootView.findViewById(R.id.noteReminderTime);
        noteReminderTime.setOnClickListener(this);

        TextView textSize = (TextView) rootView.findViewById(R.id.textSize);
        textSize.setOnClickListener(this);

        checkEnables();
        return rootView;
    }

    private void checkEnables(){
        if (noteReminderPrefs.isChecked()){
            noteReminderTime.setEnabled(true);
        } else {
            noteReminderTime.setEnabled(false);
        }
    }

    private void encryptNoteChange (){
        if (encryptNotePrefs.isChecked()){
            sPrefs.putBoolean(Prefs.NOTE_ENCRYPT, false);
            encryptNotePrefs.setChecked(false);
            new EncryptNotes(getActivity(), false).execute();
        } else {
            sPrefs.putBoolean(Prefs.NOTE_ENCRYPT, true);
            encryptNotePrefs.setChecked(true);
            new EncryptNotes(getActivity(), true).execute();
        }
    }

    private void backupChange (){
        if (backupNotePrefs.isChecked()){
            sPrefs.putBoolean(Prefs.SYNC_NOTES, false);
            backupNotePrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.SYNC_NOTES, true);
            backupNotePrefs.setChecked(true);
        }
    }

    private void deleteChange (){
        if (deleteFilePrefs.isChecked()){
            sPrefs.putBoolean(Prefs.DELETE_NOTE_FILE, false);
            deleteFilePrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.DELETE_NOTE_FILE, true);
            deleteFilePrefs.setChecked(true);
        }
    }

    private void noteReminderChange (){
        if (noteReminderPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.QUICK_NOTE_REMINDER, false);
            noteReminderPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.QUICK_NOTE_REMINDER, true);
            noteReminderPrefs.setChecked(true);
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
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.encryptNotePrefs:
                encryptNoteChange();
                break;
            case R.id.backupNotePrefs:
                backupChange();
                break;
            case R.id.noteReminderPrefs:
                noteReminderChange();
                break;
            case R.id.deleteFilePrefs:
                deleteChange();
                break;
            case R.id.noteReminderTime:
                Dialogues.dialogWithSeek(getActivity(), 120, Prefs.QUICK_NOTE_REMINDER_TIME, getString(R.string.time), this);
                break;
            case R.id.textSize:
                Dialogues.dialogWithSeek(getActivity(), 18, Prefs.TEXT_SIZE, getString(R.string.text_size), this);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    class EncryptNotes extends AsyncTask<Void, Void, Integer> {
        private ProgressDialog pd;
        private Context mContext;
        private boolean mEncrypt;

        public EncryptNotes(Context context, boolean encrypt) {
            this.mContext = context;
            this.mEncrypt = encrypt;
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            if (encrypt) pd.setMessage(context.getString(R.string.encrypting));
            else pd.setMessage(context.getString(R.string.decrypting));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int i = 0;
            List<NoteItem> list = NoteHelper.getInstance(mContext).getAll();
            for (NoteItem item : list) {
                String note = item.getNote();
                String converted = mEncrypt ? SyncHelper.encrypt(note) : SyncHelper.decrypt(note);
                item.setNote(converted);
                NoteHelper.getInstance(mContext).saveNote(item);
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
