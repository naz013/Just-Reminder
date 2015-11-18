package com.cray.software.justreminder.helpers;

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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileExplore extends AppCompatActivity {

	// Stores names of traversed directories
	ArrayList<String> str = new ArrayList<>();

	// Check if the first level of the directory structure is the one showing
	private Boolean firstLvl = true;
    private boolean isDark = false;

	private Item[] fileList;
	private File path = new File(Environment.getExternalStorageDirectory() + "");
	private String chosenFile;

	ListAdapter adapter;

    private ListView list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
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
                    String fileName = fileList[position].fileName;
                    if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg")) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.FILE_PICKED, fileList[position].filePath);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Messages.toast(FileExplore.this, getString(R.string.file_format_warming));
                    }
                }
            }
        });
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

			String[] fList = path.list(filter);
			fileList = new Item[fList.length];
			for (int i = 0; i < fList.length; i++) {
                String fileName = fList[i];
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
        else
            return isDark ? R.drawable.ic_insert_drive_file_white_24dp : R.drawable.ic_insert_drive_file_black_24dp;
    }

    private boolean isPicture(String file){
        return file.endsWith(".jpg") || file.endsWith(".jpeg") || file.endsWith(".png") || file.endsWith(".gif");
    }

    private boolean isMelody(String file){
        return file.endsWith(".mp3") || file.endsWith(".ogg") || file.endsWith(".m4a") || file.endsWith(".flac");
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
        setResult(RESULT_CANCELED);
        finish();
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