package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.constants.Constants;

import java.util.ArrayList;

public class FontStyleDialog extends Activity{

    private ListView musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(FontStyleDialog.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ArrayList<String> contacts = new ArrayList<>();
        contacts.clear();
        contacts.add("Black");
        contacts.add("Black Italic");
        contacts.add("Bold");
        contacts.add("Bold Italic");
        contacts.add("Italic");
        contacts.add("Light");
        contacts.add("Light Italic");
        contacts.add("Medium");
        contacts.add("Medium Italic");
        contacts.add("Regular");
        contacts.add("Thin");
        contacts.add("Thin Italic");

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        FontAdapter adapter = new FontAdapter(FontStyleDialog.this, contacts);
        musicList.setAdapter(adapter);

        TextView dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.font_style_dialog_title));

        TextView musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                int selected = musicList.getCheckedItemPosition();
                if (selected != -1) {
                    intent.putExtra(Constants.SELECTED_FONT_STYLE, selected);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Messages.toast(FontStyleDialog.this, getString(R.string.select_item_warming));
                }
            }
        });
    }

    public class FontAdapter extends BaseAdapter{

        Context cContext;
        LayoutInflater inflater;
        ArrayList<String> list;
        Typeface typeface, typeface1, typeface2, typeface3, typeface4,
            typeface5, typeface6, typeface7, typeface8, typeface9, typeface10, typeface11;

        public FontAdapter(Context context, ArrayList<String> fonts){
            this.cContext = context;
            this.list = fonts;
            loadTypeFace();
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private void loadTypeFace(){
            ColorSetter cs = new ColorSetter(cContext);
            typeface = cs.getTypeface(0);
            typeface1 = cs.getTypeface(1);
            typeface2 = cs.getTypeface(2);
            typeface3 = cs.getTypeface(3);
            typeface4 = cs.getTypeface(4);
            typeface5 = cs.getTypeface(5);
            typeface6 = cs.getTypeface(6);
            typeface7 = cs.getTypeface(7);
            typeface8 = cs.getTypeface(8);
            typeface9 = cs.getTypeface(9);
            typeface10 = cs.getTypeface(10);
            typeface11 = cs.getTypeface(11);
        }

        private Typeface getTypeface(int position){
            if (position == 0) {
                return typeface;
            } else if (position == 1) {
                return typeface1;
            } else if (position == 2) {
                return typeface2;
            } else if (position == 3) {
                return typeface3;
            } else if (position == 4) {
                return typeface4;
            } else if (position == 5) {
                return typeface5;
            } else if (position == 6) {
                return typeface6;
            } else if (position == 7) {
                return typeface7;
            } else if (position == 8) {
                return typeface8;
            } else if (position == 9) {
                return typeface9;
            } else if (position == 10) {
                return typeface10;
            } else if (position == 11) {
                return typeface11;
            } else {
                return typeface;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                inflater = (LayoutInflater) cContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, null);
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setTypeface(getTypeface(position));
            textView.setText(list.get(position));

            return convertView;
        }
    }
}
