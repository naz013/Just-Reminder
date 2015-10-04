package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.cray.software.justreminder.interfaces.Prefs;

import java.io.File;
import java.io.IOException;

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
public class Sound {
    private Context mContext;
    private MediaPlayer mMediaPlayer;

    public Sound(Context context){
        this.mContext = context;
    }

    public void stop(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void play(String path){
        int maxVolume = 26;
        int currVolume = new SharedPrefs(mContext).loadInt(Prefs.VOLUME);
        float log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
        File file = new File(path);
        Uri soundUri = Uri.fromFile(file);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, soundUri);
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

    public void playAlarm(Uri path, boolean looping){
        int maxVolume = 26;
        int currVolume = new SharedPrefs(mContext).loadInt(Prefs.VOLUME);
        float log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setVolume(1 - log1, 1 - log1);
        mMediaPlayer.setLooping(looping);
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

    public void playAlarm(AssetFileDescriptor afd, boolean looping){
        int maxVolume = 26;
        int currVolume = new SharedPrefs(mContext).loadInt(Prefs.VOLUME);
        float log1 = (float)(Math.log(maxVolume-currVolume)/Math.log(maxVolume));
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        mMediaPlayer.setVolume(1 - log1, 1 - log1);
        mMediaPlayer.setLooping(looping);
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
