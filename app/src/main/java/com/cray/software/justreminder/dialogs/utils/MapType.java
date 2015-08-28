package com.cray.software.justreminder.dialogs.utils;

import android.app.Activity;
import android.os.Bundle;
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
import com.cray.software.justreminder.interfaces.Prefs;

public class MapType extends Activity{

    SharedPrefs sPrefs;
    ListView musicList;
    TextView musicDialogOk;
    TextView dialogTitle;
    ColorSetter cs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(MapType.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(R.string.map_type_title);

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        ArrayAdapter<CharSequence> aa = ArrayAdapter.createFromResource(this, R.array.map_types,
                android.R.layout.select_dialog_singlechoice);
        musicList.setAdapter(aa);

        sPrefs = new SharedPrefs(MapType.this);
        String type = sPrefs.loadPrefs(Prefs.MAP_TYPE);
        int position;
        if (type.matches(Constants.MAP_TYPE_NORMAL)){
            position = 0;
        } else if (type.matches(Constants.MAP_TYPE_SATELLITE)){
            position = 1;
        } else if (type.matches(Constants.MAP_TYPE_HYBRID)){
            position = 2;
        } else if (type.matches(Constants.MAP_TYPE_TERRAIN)){
            position = 3;
        } else {
            position = 0;
        }

        musicList.setItemChecked(position, true);

        musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPosition = musicList.getCheckedItemPosition();
                if (selectedPosition != -1) {
                    sPrefs = new SharedPrefs(MapType.this);
                    String prefs = Prefs.MAP_TYPE;
                    if (selectedPosition == 0){
                        sPrefs.savePrefs(prefs, Constants.MAP_TYPE_NORMAL);
                    } else if (selectedPosition == 1){
                        sPrefs.savePrefs(prefs, Constants.MAP_TYPE_SATELLITE);
                    } else if (selectedPosition == 2){
                        sPrefs.savePrefs(prefs, Constants.MAP_TYPE_HYBRID);
                    } else if (selectedPosition == 3){
                        sPrefs.savePrefs(prefs, Constants.MAP_TYPE_TERRAIN);
                    } else {
                        sPrefs.savePrefs(prefs, Constants.MAP_TYPE_NORMAL);
                    }
                    finish();
                } else {
                    Toast.makeText(MapType.this, getString(R.string.select_item_warming), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
