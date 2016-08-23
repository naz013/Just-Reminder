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

package com.cray.software.justreminder.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.roboto_views.RoboButton;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;

public class PermissionsList extends Activity{

    private ArrayList<Item> mDataList = new ArrayList<>();
    private ListView mPermissionsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = ColorSetter.getInstance(PermissionsList.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        initDialogTitle();
        initPermissionsList();
        initOkButton();
        loadDataToList();
    }

    private void initDialogTitle() {
        RoboTextView dialogTitle = (RoboTextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.allow_permission));
    }

    private void initOkButton() {
        RoboButton musicDialogOk = (RoboButton) findViewById(R.id.musicDialogOk);
        musicDialogOk.setVisibility(View.INVISIBLE);
    }

    private void initPermissionsList() {
        mPermissionsList = (ListView) findViewById(R.id.musicList);
        mPermissionsList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mPermissionsList.setOnItemClickListener((parent, view, position, id) -> requestPermission(position));
    }

    private void requestPermission(int position) {
        Permissions.requestPermission(PermissionsList.this, position, mDataList.get(position).getPermission());
    }

    private void loadDataToList(){
        mDataList.clear();
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.ACCESS_COARSE_LOCATION)) {
            mDataList.add(new Item(getString(R.string.course_location), Permissions.ACCESS_COARSE_LOCATION));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.ACCESS_FINE_LOCATION)) {
            mDataList.add(new Item(getString(R.string.fine_location), Permissions.ACCESS_FINE_LOCATION));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.CALL_PHONE)) {
            mDataList.add(new Item(getString(R.string.call_phone), Permissions.CALL_PHONE));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.GET_ACCOUNTS)) {
            mDataList.add(new Item(getString(R.string.get_accounts), Permissions.GET_ACCOUNTS));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.READ_PHONE_STATE)) {
            mDataList.add(new Item(getString(R.string.read_phone_state), Permissions.READ_PHONE_STATE));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.READ_CALENDAR)) {
            mDataList.add(new Item(getString(R.string.read_calendar), Permissions.READ_CALENDAR));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.WRITE_CALENDAR)) {
            mDataList.add(new Item(getString(R.string.write_calendar), Permissions.WRITE_CALENDAR));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.READ_CONTACTS)) {
            mDataList.add(new Item(getString(R.string.read_contacts), Permissions.READ_CONTACTS));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.READ_CALLS)) {
            mDataList.add(new Item(getString(R.string.call_history), Permissions.READ_CALLS));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.READ_EXTERNAL)) {
            mDataList.add(new Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.WRITE_EXTERNAL)) {
            mDataList.add(new Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL));
        }
        if (!Permissions.checkPermission(PermissionsList.this, Permissions.SEND_SMS)) {
            mDataList.add(new Item(getString(R.string.send_sms), Permissions.SEND_SMS));
        }
        if (mDataList.size() == 0) {
            Toast.makeText(PermissionsList.this, R.string.all_permissions_are_enabled, Toast.LENGTH_SHORT).show();
            finish();
        }
        Adapter adapter = new Adapter(mDataList, this);
        mPermissionsList.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (requestCode < mDataList.size()) mDataList.remove(requestCode);
            loadDataToList();
        }
    }

    class Adapter extends BaseAdapter{

        private ArrayList<Item> list;
        private LayoutInflater inflater;

        Adapter(ArrayList<Item> list, Context context){
            this.list = list;
            inflater = LayoutInflater.from(context);
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(list.get(position).getTitle());
            return convertView;
        }
    }

    class Item {
        private String title, permission;

        Item(String title, String permission){
            this.permission = permission;
            this.title = title;
        }

        public String getTitle(){
            return title;
        }

        public String getPermission(){
            return permission;
        }
    }
}
