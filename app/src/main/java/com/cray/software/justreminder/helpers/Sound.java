package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.cray.software.justreminder.constants.Prefs;

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
    private boolean isPaused;
    private String lastFile;

    public Sound(Context context){
        this.mContext = context;
    }

    /**
     * Stop playing melody.
     */
    public void stop(){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            isPaused = false;
        }
    }

    /**
     * Pause playing melody.
     */
    public void pause(){
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            isPaused = true;
        }
    }

    /**
     * Resume playing melody.
     */
    public void resume(){
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            isPaused = false;
        }
    }

    /**
     * Check if media player is paused.
     * @return boolean
     */
    public boolean isPaused(){
        return isPaused;
    }

    /**
     * Check if media player is playing.
     * @return boolean
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * Check if media player already play this file.
     * @return boolean
     */
    public boolean isSameFile(String path) {
        return lastFile != null && path.equalsIgnoreCase(lastFile);
    }

    /**
     * Play melody file.
     * @param path path to file.
     */
    public void play(String path){
        lastFile = path;
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

    /**
     * Play melody for reminder.
     * @param path Uri path for melody file.
     * @param looping flag for media player looping.
     */
    public void playAlarm(Uri path, boolean looping){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mContext, path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SharedPrefs prefs = new SharedPrefs(mContext);
        boolean isSystem = prefs.loadBoolean(Prefs.SYSTEM_VOLUME);
        if (isSystem) {
            int stream = prefs.loadInt(Prefs.SOUND_STREAM);
            mMediaPlayer.setAudioStreamType(stream);
        } else mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

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

    /**
     * Play built-in reminder melody from assets.
     * @param afd file descriptor for built-in melody.
     * @param looping flag for media player looping.
     */
    public void playAlarm(AssetFileDescriptor afd, boolean looping){
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPrefs prefs = new SharedPrefs(mContext);
        boolean isSystem = prefs.loadBoolean(Prefs.SYSTEM_VOLUME);
        if (isSystem) {
            int stream = prefs.loadInt(Prefs.SOUND_STREAM);
            mMediaPlayer.setAudioStreamType(stream);
        } else mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
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
