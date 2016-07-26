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

package com.cray.software.justreminder.file_explorer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.contacts.RecyclerClickListener;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Sound;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.ViewUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileExploreActivity extends AppCompatActivity {

    private ArrayList<String> str = new ArrayList<>();
	private Boolean firstLvl = true;
    private boolean isDark = false;
    private boolean mFilter;

	private ArrayList<FileDataItem> mDataList;
	private File path = new File(Environment.getExternalStorageDirectory() + "");
	private String mFileName;
	private String mFilePath;

    private String filType;

	private FilesRecyclerAdapter mAdapter;
    private Sound mSound;

    private RecyclerView mFilesList;
    private LinearLayout mPlayerLayout;
    private RoboTextView mMelodyTitle;
    private RoboEditText mSearchView;
    private RecyclerClickListener recyclerClick = this::selectFile;

    private void selectFile(int position) {
        FileDataItem item = mAdapter.getItem(position);
        mFileName = item.getFileName();
        mFilePath = item.getFilePath();
        File sel = new File(path + "/" + mFileName);
        if (sel.isDirectory()) {
            firstLvl = false;
            str.add(mFileName);
            mDataList = null;
            path = new File(sel + "");
            loadFileList();
            loadList();
        } else if (mFileName.equalsIgnoreCase("up") && !sel.exists()) {
            moveUp();
        } else {
            if (filType.matches("any")) {
                sendFile();
            } else {
                if (isMelody(mFileName)) {
                    play();
                } else {
                    Messages.toast(FileExploreActivity.this, getString(R.string.not_music_file));
                }
            }
        }
    }

    private void moveUp() {
        String s = str.remove(str.size() - 1);
        path = new File(path.toString().substring(0,
                path.toString().lastIndexOf(s)));
        mDataList = null;
        if (str.isEmpty()) {
            firstLvl = true;
        }
        loadFileList();
        loadList();
    }

    private void sendFile() {
        Intent intent = new Intent();
        intent.putExtra(Constants.FILE_PICKED, mFilePath);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mSound = new Sound(this);
        ColorSetter cs = new ColorSetter(this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
		setContentView(R.layout.activity_file_chooser);
        setRequestedOrientation(cs.getRequestOrientation());
        filType = getIntent().getStringExtra(Constants.FILE_TYPE);
        if (filType == null) filType = "music";
        isDark = cs.isDark();
        initActionBar();
        initRecyclerView();
        initPlayer();
        initSearch();
        initButtons();
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        mFilesList.setBackgroundColor(cs.getBackgroundStyle());
		loadFileList();
		loadList();
	}

    private void initPlayer() {
        mPlayerLayout = (LinearLayout) findViewById(R.id.playerLayout);
        mPlayerLayout.setVisibility(View.GONE);
        mMelodyTitle = (RoboTextView) findViewById(R.id.currentMelody);
    }

    private void initRecyclerView() {
        mFilesList = (RecyclerView) findViewById(R.id.mDataList);
        mFilesList.setHasFixedSize(true);
        mFilesList.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
    }

    private void initSearch() {
        mSearchView = (RoboEditText) findViewById(R.id.searchField);
        mSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mFilter) filterFiles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initButtons() {
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.pauseButton);
        ImageButton clearButton = (ImageButton) findViewById(R.id.clearButton);
        if (isDark) {
            playButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
            stopButton.setImageResource(R.drawable.ic_stop_white_24dp);
            pauseButton.setImageResource(R.drawable.ic_pause_white_24dp);
        } else {
            playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            stopButton.setImageResource(R.drawable.ic_stop_black_24dp);
            pauseButton.setImageResource(R.drawable.ic_pause_black_24dp);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(mListener);
        pauseButton.setOnClickListener(mListener);
        stopButton.setOnClickListener(mListener);
        playButton.setOnClickListener(mListener);
        clearButton.setOnClickListener(mListener);
    }

    private void filterFiles(String q) {
        List<FileDataItem> res = filter(mDataList, q);
        mAdapter.animateTo(res);
        mFilesList.scrollToPosition(0);
    }

    private List<FileDataItem> filter(List<FileDataItem> mData, String q) {
        q = q.toLowerCase();
        if (mData == null) mData = new ArrayList<>();
        List<FileDataItem> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<FileDataItem> getFiltered(List<FileDataItem> models, String query) {
        List<FileDataItem> list = new ArrayList<>();
        for (FileDataItem model : models) {
            final String text = model.getFileName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    private void loadList() {
        if (mDataList == null) {
            Messages.toast(this, getString(R.string.no_files));
            finish();
        }
        mFilesList.setAdapter(mAdapter);
    }
    
    private void play(){
        if (!mSound.isPlaying()){
            if (mPlayerLayout.getVisibility() == View.GONE) {
                ViewUtils.expand(mPlayerLayout);
            }
            if (mSound.isPaused() && mSound.isSameFile(mFilePath)) {
                mSound.resume();
            } else {
                mSound.play(mFilePath);
                mMelodyTitle.setText(mFileName);
            }
        } else {
            if (mSound.isSameFile(mFilePath)) {
                return;
            }
            mSound.play(mFilePath);
            mMelodyTitle.setText(mFileName);
        }
    }

    private void pause(){
        if (mSound.isPlaying()) {
            mSound.pause();
        }
    }

    private void stop(){
        if (mSound.isPlaying()) {
            mSound.stop();
        }
        ViewUtils.collapse(mPlayerLayout);
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        mFilter = false;
        mSearchView.setText("");
        mFilter = true;
        if (path.exists()) {
            createFilteredFileList();
        }
        mAdapter = new FilesRecyclerAdapter(this, mDataList, recyclerClick);
    }

    private void createFilteredFileList() {
        FilenameFilter filter = (dir, filename) -> {
            File sel = new File(dir, filename);
            return (sel.isFile() || sel.isDirectory())
                    && !sel.isHidden();
        };

        List<String> list;
        try {
            list = Arrays.asList(path.list(filter));
        } catch (NullPointerException e) {
            list = new ArrayList<>();
        }
        Collections.sort(list);
        mDataList = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            String fileName = list.get(i);
            File sel = new File(path, fileName);
            mDataList.add(i, new FileDataItem(fileName, getFileIcon(fileName), sel.toString()));

            if (sel.isDirectory()) {
                mDataList.get(i).setIcon(getDirectoryIcon());
            }
        }

        if (!firstLvl) {
            addUpItem();
        }
    }

    private void addUpItem() {
        ArrayList<FileDataItem> temp = new ArrayList<>(mDataList.size() + 1);
        temp.add(0, new FileDataItem(getString(R.string.up), getUndoIcon(), null));
        temp.addAll(mDataList);
        mDataList = temp;
    }

    private int getFileIcon(String file){
        if (isMelody(file))
            return isDark ? R.drawable.ic_music_note_white_24dp : R.drawable.ic_music_note_black_24dp;
        else if (isPicture(file))
            return isDark ? R.drawable.ic_image_white_24dp : R.drawable.ic_image_black_24dp;
        else if (isMovie(file))
            return isDark ? R.drawable.ic_movie_white_24dp : R.drawable.ic_movie_black_24dp;
        else if (isGif(file))
            return isDark ? R.drawable.ic_gif_white_24dp : R.drawable.ic_gif_black_24dp;
        else if (isArchive(file))
            return isDark ? R.drawable.ic_storage_white_24dp : R.drawable.ic_storage_black_24dp;
        else if (isAndroid(file))
            return isDark ? R.drawable.ic_android_white_24dp : R.drawable.ic_android_black_24dp;
        else
            return isDark ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_insert_drive_file_black_24dp;
    }

    private boolean isPicture(String file){
        return file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith(".png");
    }

    private boolean isArchive(String file){
        return file.endsWith(".zip") || file.endsWith(".rar") || file.endsWith(".tar.gz");
    }

    private boolean isMovie(String file){
        return file.endsWith(".mov") || file.endsWith(".3gp") || file.endsWith(".avi") ||
                file.endsWith(".mkv") || file.endsWith(".vob") || file.endsWith(".divx") ||
                file.endsWith(".mp4") || file.endsWith(".flv");
    }

    private boolean isGif(String file){
        return file.endsWith(".gif");
    }

    private boolean isAndroid(String file){
        return file.endsWith(".apk");
    }

    private boolean isMelody(String file){
        return file != null && (file.endsWith(".mp3") || file.endsWith(".ogg")
                || file.endsWith(".m4a") || file.endsWith(".flac"));
    }

    private int getDirectoryIcon(){
        return isDark ? R.drawable.ic_folder_white_24dp : R.drawable.ic_folder_black_24dp;
    }

    private int getUndoIcon(){
        return isDark ? R.drawable.ic_undo_white_24dp : R.drawable.ic_undo_black_24dp;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (!firstLvl) {
            moveUp();
        } else {
            exit();
        }
    }

    private void exit() {
        if (isMelody(mFileName)) {
            stop();
        }
        setResult(RESULT_CANCELED);
        finish();
    }

    private View.OnClickListener mListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.fab:
                    saveChoice();
                    break;
                case R.id.playButton:
                    play();
                    break;
                case R.id.stopButton:
                    stop();
                    break;
                case R.id.pauseButton:
                    pause();
                    break;
                case R.id.clearButton:
                    mSearchView.setText("");
                    break;
            }
        }
    };

    private void saveChoice() {
        if (isMelody(mFileName)) {
            stop();
            sendFile();
        } else {
            Messages.toast(FileExploreActivity.this, getString(R.string.not_music_file));
        }
    }
}