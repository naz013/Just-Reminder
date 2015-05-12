package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SelectMelody extends Activity{

    MediaPlayer mMediaPlayer;
    ArrayList<File> fileList = new ArrayList<>();
    ColorSetter cs;
    ListView musicList;
    TextView musicDialogOk;
    int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(SelectMelody.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        id = intent.getIntExtra(Constants.BIRTHDAY_INTENT_ID, 0);
        ArrayList<String> names = intent.getStringArrayListExtra("names");
        ArrayList<String> folders = intent.getStringArrayListExtra("folders");
        for (String folder : folders) {
            File musicFile = new File(folder);
            fileList.add(musicFile);
        }

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    File path = fileList.get(position);
                    Uri soundUri = Uri.fromFile(path);
                    if (mMediaPlayer != null) {
                        mMediaPlayer.stop();
                    }
                    mMediaPlayer = new MediaPlayer();
                    try {
                        mMediaPlayer.setDataSource(SelectMelody.this, soundUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setLooping(false);
                    try {
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.start();
                }
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectMelody.this,
                android.R.layout.simple_list_item_single_choice, names);
        musicList.setAdapter(adapter);

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    if (mMediaPlayer != null) {
                        mMediaPlayer.stop();
                    }
                    File path = fileList.get(selectedPosition);
                    Intent intent = new Intent();
                    intent.putExtra(Constants.SELECTED_MELODY, path.toString());
                    if (getParent() == null) {
                        setResult(Activity.RESULT_OK, intent);
                    } else {
                        getParent().setResult(Activity.RESULT_OK, intent);
                    }
                    finish();
                } else {
                    Toast.makeText(SelectMelody.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
