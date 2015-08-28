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
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ListSounds extends Activity{

    MediaPlayer mMediaPlayer;
    ArrayList<File> fileList = new ArrayList<>();
    SharedPrefs sPrefs;
    ColorSetter cs;
    ListView musicList;
    TextView musicDialogOk;
    int id;
    float log1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(ListSounds.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        sPrefs = new SharedPrefs(ListSounds.this);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        id = intent.getIntExtra(Constants.BIRTHDAY_INTENT_ID, 0);
        ArrayList<String> names = intent.getStringArrayListExtra("names");
        ArrayList<String> folders = intent.getStringArrayListExtra("folders");
        for (String folder : folders) {
            File musicFile = new File(folder);
            fileList.add(musicFile);
        }

        int maxVolume = 26;
        int currVolume = sPrefs.loadInt(Prefs.VOLUME);
        log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));

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
                        mMediaPlayer.setDataSource(ListSounds.this, soundUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.setVolume(1 - log1, 1 - log1);
                    mMediaPlayer.setLooping(false);
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.start();
                        }
                    });
                    try {
                        mMediaPlayer.prepareAsync();
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(ListSounds.this,
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
                    sPrefs = new SharedPrefs(ListSounds.this);
                    String prefs;
                    if (id == 3) {
                        prefs = Prefs.BIRTHDAY_CUSTOM_SOUND_FILE;
                    } else {
                        prefs = Constants.CUSTOM_SOUND_FILE;
                    }
                    sPrefs.savePrefs(prefs, path.toString());
                    finish();
                } else {
                    Toast.makeText(ListSounds.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        super.onDestroy();
    }
}
