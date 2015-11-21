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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;

import java.util.ArrayList;

public class PermissionsList extends Activity{

    private Permissions permissions;
    private ArrayList<Item> list = new ArrayList<>();
    private ListView musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(PermissionsList.this);
        setTheme(cs.getDialogStyle());
        setContentView(R.layout.music_list_dilog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView dialogTitle = (TextView) findViewById(R.id.dialogTitle);
        dialogTitle.setText(getString(R.string.add_permission));

        musicList = (ListView) findViewById(R.id.musicList);
        musicList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        permissions = new Permissions(this);
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                permissions.requestPermission(PermissionsList.this, new String[]{list.get(position).getPermission()}, position);
            }
        });

        TextView musicDialogOk = (TextView) findViewById(R.id.musicDialogOk);
        musicDialogOk.setVisibility(View.INVISIBLE);

        load();
    }

    private void load(){
        list.clear();
        if (!permissions.checkPermission(Permissions.ACCESS_COURSE_LOCATION))
            list.add(new Item(getString(R.string.course_location), Permissions.ACCESS_COURSE_LOCATION));

        if (!permissions.checkPermission(Permissions.ACCESS_FINE_LOCATION))
            list.add(new Item(getString(R.string.fine_location), Permissions.ACCESS_FINE_LOCATION));

        if (!permissions.checkPermission(Permissions.CALL_PHONE))
            list.add(new Item(getString(R.string.call_phone), Permissions.CALL_PHONE));

        if (!permissions.checkPermission(Permissions.GET_ACCOUNTS))
            list.add(new Item(getString(R.string.get_accounts), Permissions.GET_ACCOUNTS));

        if (!permissions.checkPermission(Permissions.READ_PHONE_STATE))
            list.add(new Item(getString(R.string.read_phone_state), Permissions.READ_PHONE_STATE));

        if (!permissions.checkPermission(Permissions.READ_CALENDAR))
            list.add(new Item(getString(R.string.read_calendar), Permissions.READ_CALENDAR));

        if (!permissions.checkPermission(Permissions.WRITE_CALENDAR))
            list.add(new Item(getString(R.string.write_calendar), Permissions.WRITE_CALENDAR));

        if (!permissions.checkPermission(Permissions.READ_CONTACTS))
            list.add(new Item(getString(R.string.read_contacts), Permissions.READ_CONTACTS));

        if (!permissions.checkPermission(Permissions.READ_EXTERNAL))
            list.add(new Item(getString(R.string.read_external_storage), Permissions.READ_EXTERNAL));

        if (!permissions.checkPermission(Permissions.WRITE_EXTERNAL))
            list.add(new Item(getString(R.string.write_external_storage), Permissions.WRITE_EXTERNAL));

        if (!permissions.checkPermission(Permissions.SEND_SMS))
            list.add(new Item(getString(R.string.send_sms), Permissions.SEND_SMS));

        Adapter adapter = new Adapter(list, this);
        musicList.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (requestCode < list.size()) list.remove(requestCode);
            load();
        }
    }

    class Adapter extends BaseAdapter{

        private ArrayList<Item> list;
        private LayoutInflater inflater;
        private Context context;

        Adapter(ArrayList<Item> list, Context context){
            this.list = list;
            this.context = context;
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
