package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SoundType extends Activity{

    SharedPrefs sPrefs;
    ListView musicList;
    TextView musicDialogOk;
    TextView dialogTitle;
    ColorSetter cs;
    int id, dirId;
    ArrayList<String> names, foldersFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(SoundType.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        id = intent.getIntExtra(Constants.BIRTHDAY_INTENT_ID, 0);

        sPrefs = new SharedPrefs(SoundType.this);

        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(R.string.sound_type_dialog_title);

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        String[] types = new String[]{getString(R.string.sound_default), getString(R.string.sound_custom)};

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(SoundType.this,
                android.R.layout.simple_list_item_single_choice, types);
        musicList.setAdapter(adapter);

        if (id == 3){
            if (sPrefs.loadBoolean(Constants.BIRTHDAY_CUSTOM_SOUND)) {
                musicList.setItemChecked(1, true);
            } else {
                musicList.setItemChecked(0, true);
            }
        } else {
            if (sPrefs.loadBoolean(Constants.CUSTOM_SOUND)) {
                musicList.setItemChecked(1, true);
            } else {
                musicList.setItemChecked(0, true);
            }
        }

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    sPrefs = new SharedPrefs(SoundType.this);
                    String prefs;
                    if (id == 3) prefs = Constants.BIRTHDAY_CUSTOM_SOUND;
                    else prefs = Constants.CUSTOM_SOUND;
                    if (selectedPosition == 0) {
                        sPrefs.saveBoolean(prefs, false);
                        finish();
                    } else {
                        sPrefs.saveBoolean(prefs, true);
                        new loadSounds(SoundType.this).execute();
                    }
                } else {
                    Toast.makeText(SoundType.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public static boolean isSdPresent() {
        return Environment.getExternalStorageState() != null;
    }

    class loadSounds extends AsyncTask<Void, Void, ArrayList<File>>{

        ProgressDialog pd;
        Context tContext;

        public loadSounds(Context context){
            this.tContext = context;
            pd = new ProgressDialog(context);
            pd.setCancelable(true);
            pd.setMessage(getString(R.string.sounds_loading_text));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected ArrayList<File> doInBackground(Void... params) {
            ArrayList<File> fileList = new ArrayList<>();
            File dir;
            if (isSdPresent()) {
                dir = new File(Environment.getExternalStorageDirectory()
                        .toString());
                dirId = Constants.DIR_ID_EXTERNAL;
                listf(dir.toString(), fileList);
            } else {
                dir = new File(Environment.getDataDirectory().toString());
                dirId = Constants.DIR_ID_DATA;
                listf(dir.toString(), fileList);
            }
            Collections.sort(fileList);
            names = new ArrayList<>();
            foldersFile = new ArrayList<>();
            names.clear();
            foldersFile.clear();
            for (File aFile : fileList) {
                names.add(aFile.getName());
                String folder = aFile.toString();
                foldersFile.add(folder);
            }
            return fileList;
        }

        @Override
        protected void onPostExecute(ArrayList<File> files) {
            try {
                if ((pd != null) && pd.isShowing()) {
                    pd.dismiss();
                }
            } catch (final Exception e) {
                // Handle or log or ignore
            }
            if (files != null){
                showDialog();
            } else {
                Toast.makeText(SoundType.this, getString(R.string.no_music), Toast.LENGTH_SHORT).show();
            }
        }

        void showDialog() {
            Intent i = new Intent(SoundType.this, ListSounds.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putStringArrayListExtra("names", names);
            i.putStringArrayListExtra("folders", foldersFile);
            i.putExtra("dir", dirId);
            if (id == 3) {
                i.putExtra(Constants.BIRTHDAY_INTENT_ID, id);
            }
            startActivity(i);
            finish();
        }
    }

    public void listf(String directoryName, ArrayList<File> files) {
        File directory = new File(directoryName);

        File[] fList = directory.listFiles();
        if (fList != null) {
            for (File file : fList) {
                if (file.canRead()) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".mp3") || file.getName().endsWith(".ogg")) {
                            files.add(file);
                        }
                    } else if (file.isDirectory()) {
                        listf(file.toString(), files);
                    }
                } else {
                    Log.d(Constants.LOG_TAG, "secure file");
                }
            }
        } else Log.i(Constants.LOG_TAG, "No files");
    }
}
