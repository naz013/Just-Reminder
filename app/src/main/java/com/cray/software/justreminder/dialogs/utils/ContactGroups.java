package com.cray.software.justreminder.dialogs.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;

import java.util.ArrayList;

public class ContactGroups extends AppCompatActivity {

    RecyclerView currentList;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(ContactGroups.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }

        setContentView(R.layout.contact_group_dialog);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.string_contact_group));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        currentList = (RecyclerView) findViewById(R.id.currentList);
        currentList.setLayoutManager(new LinearLayoutManager(this));
        currentList.setAdapter(new SimpleRecyclerAdapter(this, loadGroups()));
    }

    private ArrayList<GroupItem> loadGroups(){
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        ArrayList<GroupItem> list = new ArrayList<>();
        SharedPrefs prefs = new SharedPrefs(this);
        if (cursor != null && cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.Data._ID));

                if (hasPhone.equalsIgnoreCase("1"))
                    hasPhone = "true";
                else
                    hasPhone = "false";
                if (name != null) {
                    if (Boolean.parseBoolean(hasPhone)) {
                        boolean is = prefs.loadBoolean(contactId);
                        list.add(new GroupItem(name, contactId, is ? Status.enabled : Status.disabled));
                    }
                }
            }
            cursor.close();
        }
        return list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public enum Status {
        enabled,
        disabled
    }

    public class GroupItem {
        private String title, id;
        private Status status;

        public GroupItem(String title, String id, Status status){
            this.id = id;
            this.title = title;
            this.status = status;
        }

        public Status getStatus(){
            return status;
        }

        public void setStatus(Status status){
            this.status = status;
        }

        public String getId(){
            return id;
        }

        public void setId(String id){
            this.id = id;
        }

        public String getTitle(){
            return title;
        }

        public void setTitle(String title){
            this.title = title;
        }
    }

    public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.ViewHolder> {

        ArrayList<GroupItem> data;
        Context cContext;
        ColorSetter cs;
        SharedPrefs prefs;

        public SimpleRecyclerAdapter(Context context, ArrayList<GroupItem> data) {
            this.cContext = context;
            this.data = data;
            cs = new ColorSetter(context);
            prefs = new SharedPrefs(context);
        }

        @Override
        public SimpleRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
            // create a new view
            View itemLayoutView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_simple_with_check, parent, false);

            // create ViewHolder

            return new ViewHolder(itemLayoutView);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
            GroupItem item = data.get(position);
            String title = item.getTitle();
            Status status = item.getStatus();

            viewHolder.card.setBackgroundColor(cs.getCardStyle());
            viewHolder.textView.setText(title);
            if (status == Status.enabled) viewHolder.check.setChecked(true);
            else viewHolder.check.setChecked(false);

            viewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
            viewHolder.textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });

            viewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onItemCheck(position, isChecked);
                }
            });
        }

        private void onItemCheck(int position, boolean isChecked){
            GroupItem item = data.get(position);
            String id = item.getTitle();
            if (isChecked) {
                prefs.saveBoolean(id, true);
                item.setStatus(Status.enabled);
            } else {
                prefs.saveBoolean(id, false);
                item.setStatus(Status.disabled);
            }
            try {
                notifyItemChanged(position);
            } catch (IllegalStateException e){
                e.printStackTrace();
            }
        }

        private void onItemClick(int position){
            GroupItem item = data.get(position);
            String id = item.getTitle();
            Status status = item.getStatus();
            if (status == Status.enabled) {
                prefs.saveBoolean(id, false);
                item.setStatus(Status.disabled);
            } else {
                prefs.saveBoolean(id, true);
                item.setStatus(Status.enabled);
            }
            try {
                notifyItemChanged(position);
            } catch (IllegalStateException e){
                e.printStackTrace();
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView textView;
            CheckBox check;
            RelativeLayout card;

            public ViewHolder(View itemLayoutView) {
                super(itemLayoutView);
                textView = (TextView) itemLayoutView.findViewById(R.id.textView);
                check = (CheckBox) itemLayoutView.findViewById(R.id.check);
                card = (RelativeLayout) itemLayoutView.findViewById(R.id.card);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}