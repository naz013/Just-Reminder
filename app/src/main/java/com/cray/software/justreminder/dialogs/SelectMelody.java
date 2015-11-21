package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Intent;
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
import com.cray.software.justreminder.helpers.Sound;
import com.cray.software.justreminder.constants.Constants;

import java.io.File;
import java.util.ArrayList;

public class SelectMelody extends Activity{

    private Sound sound = new Sound(this);
    private ArrayList<File> fileList = new ArrayList<>();
    private ListView musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(SelectMelody.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent intent = getIntent();
        int id = intent.getIntExtra(Constants.BIRTHDAY_INTENT_ID, 0);
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
                    sound.play(path.toString());
                }
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectMelody.this,
                android.R.layout.simple_list_item_single_choice, names);
        musicList.setAdapter(adapter);

        TextView musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    sound.stop();
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
