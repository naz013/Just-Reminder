package com.cray.software.justreminder.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.Sound;
import com.cray.software.justreminder.utils.ViewUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileExplore extends AppCompatActivity implements View.OnClickListener {

	// Stores names of traversed directories
    private ArrayList<String> str = new ArrayList<>();

	// Check if the first level of the directory structure is the one showing
	private Boolean firstLvl = true;
    private boolean isDark = false;

	private Item[] fileList;
	private File path = new File(Environment.getExternalStorageDirectory() + "");
	private String chosenFile;
	private String chosenPath;

	private ListAdapter adapter;
    private Sound sound;

    private ListView list;
    private LinearLayout buttonContainer;
    private TextView title, currentMelody;
    private ImageButton playButton;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
		setContentView(R.layout.activity_file_chooser);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        list = (ListView) findViewById(R.id.list);

        isDark = new SharedPrefs(this).loadBoolean(Prefs.USE_DARK_THEME);

        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);
        buttonContainer.setVisibility(View.GONE);

        title = (TextView) findViewById(R.id.title);
        currentMelody = (TextView) findViewById(R.id.currentMelody);

        playButton = (ImageButton) findViewById(R.id.playButton);
        ImageButton stopButton = (ImageButton) findViewById(R.id.stopButton);
        ImageButton selectButton = (ImageButton) findViewById(R.id.selectButton);
        selectButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        
        sound = new Sound(this);

		loadFileList();

		loadList();

	}

    private void loadList() {
        if (fileList == null) {
            Messages.toast(this, getString(R.string.no_files));
            finish();
        }

        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenFile = fileList[position].fileName;
                chosenPath = fileList[position].filePath;
                File sel = new File(path + "/" + chosenFile);
                if (sel.isDirectory()) {
                    firstLvl = false;

                    // Adds chosen directory to list
                    str.add(chosenFile);
                    fileList = null;
                    path = new File(sel + "");

                    loadFileList();

                    loadList();
                }

                // Checks if 'up' was clicked
                else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                    // present directory removed from list
                    String s = str.remove(str.size() - 1);

                    // path modified to exclude present directory
                    path = new File(path.toString().substring(0,
                            path.toString().lastIndexOf(s)));
                    fileList = null;

                    // if there are no more directories in the list, then
                    // its the first level
                    if (str.isEmpty()) {
                        firstLvl = true;
                    }
                    loadFileList();

                    loadList();

                }
                // File picked
                else {
                    if (isMelody(chosenFile)) {
                        if (title.getVisibility() == View.VISIBLE){
                            ViewUtils.hideFull(title);
                            ViewUtils.show(buttonContainer);
                        }
                        play(false);
                    } else {
                        Messages.toast(FileExplore.this, getString(R.string.not_music_file));
                    }
                }
            }
        });
    }
    
    private void play(boolean button){
        if (sound.isPlaying()){
            if (button) {
                pause();
            } else {
                if (sound.isSameFile(chosenPath)) {
                    pause();
                } else {
                    sound.play(chosenPath);
                    currentMelody.setText(chosenFile);
                }
            }
        } else {
            if (button){
                if (sound.isPaused()){
                    sound.resume();
                }
            } else {
                if (sound.isPaused() && sound.isSameFile(chosenPath)) {
                    sound.resume();
                } else {
                    sound.play(chosenPath);
                    currentMelody.setText(chosenFile);
                }
            }
            playButton.setImageResource(R.drawable.ic_pause_black_24dp);
        }
    }

    private void pause(){
        sound.pause();
        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    private void stop(){
        sound.stop();
        playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
    }

    private void loadFileList() {
		try {
			path.mkdirs();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		// Checks whether path exists
		if (path.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					// Filters based on whether the file is hidden or not
					return (sel.isFile() || sel.isDirectory())
							&& !sel.isHidden();

				}
			};

            List<String> list = Arrays.asList(path.list(filter));
            Collections.sort(list);
			fileList = new Item[list.size()];
			for (int i = 0; i < list.size(); i++) {
                String fileName = list.get(i);
                // Convert into file path
                File sel = new File(path, fileName);
				fileList[i] = new Item(fileName, getFileIcon(fileName), sel.toString());

				// Set drawables
				if (sel.isDirectory()) {
					fileList[i].icon = getDirectoryIcon();
				}
			}

			if (!firstLvl) {
				Item temp[] = new Item[fileList.length + 1];
                System.arraycopy(fileList, 0, temp, 1, fileList.length);
				temp[0] = new Item("Up", getUndoIcon(), null);
				fileList = temp;
			}
		}

		adapter = new ArrayAdapter<Item>(this,
				android.R.layout.select_dialog_item, android.R.id.text1,
				fileList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// creates view
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view
						.findViewById(android.R.id.text1);

				// put the image on the text view
				textView.setCompoundDrawablesWithIntrinsicBounds(
						fileList[position].icon, 0, 0, 0);

				// add margin between image and text (support various screen
				// densities)
				int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
				textView.setCompoundDrawablePadding(dp5);

				return view;
			}
		};
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
        return file.endsWith(".mp3") || file.endsWith(".ogg") 
                || file.endsWith(".m4a") || file.endsWith(".flac");
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
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isMelody(chosenFile)) {
            stop();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.selectButton:
                if (isMelody(chosenFile)) {
                    stop();
                    Intent intent = new Intent();
                    intent.putExtra(Constants.FILE_PICKED, chosenPath);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Messages.toast(FileExplore.this, getString(R.string.not_music_file));
                }
                break;
            case R.id.playButton:
                play(true);
                break;
            case R.id.stopButton:
                stop();
                break;
        }
    }

    private class Item {
		public String fileName;
		public String filePath;
		public int icon;

		public Item(String fileName, Integer icon, String filePath) {
			this.fileName = fileName;
			this.filePath = filePath;
			this.icon = icon;
		}

		@Override
		public String toString() {
			return fileName;
		}
	}
}