package com.cray.software.justreminder.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.FloatingEditText;

import java.util.ArrayList;

public class ContactsList extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private String name = "";

    private FloatingEditText searchField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(ContactsList.this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.contact_picker_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setTitle(getString(R.string.contacts));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        Intent intent = getIntent();
        final ArrayList<String> contacts = intent.getStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY);

        searchField = (FloatingEditText) findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ContactsList.this.adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ListView contactsList = (ListView) findViewById(R.id.contactsList);
        adapter = new ArrayAdapter<>(ContactsList.this,
                android.R.layout.simple_list_item_1, contacts);
        contactsList.setAdapter(adapter);

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    name = (String) parent.getItemAtPosition(position);
                    Cursor c;
                    c = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                            new String[]{name}, null);

                    int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

                    if (c.getCount() > 1) { // contact has multiple phone numbers
                        final CharSequence[] numbers = new CharSequence[c.getCount()];
                        int i = 0;
                        if (c.moveToFirst()) {
                            while (!c.isAfterLast()) { // for each phone number, add it to the numbers array
                                String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                        getResources(), c.getInt(phoneType), ""); // insert a type string in front of the number
                                String number = type + ": " + c.getString(phoneIdx);
                                numbers[i++] = number;
                                c.moveToNext();
                            }
                            // build and show a simple dialog that allows the user to select a number
                            AlertDialog.Builder builder = new AlertDialog.Builder(ContactsList.this);
                            builder.setItems(numbers, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    String number = (String) numbers[item];
                                    int index = number.indexOf(":");
                                    number = number.substring(index + 2);
                                    //selectedNumber.setText(number);
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number);
                                    intent.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.setOwnerActivity(ContactsList.this);
                            alert.show();

                        } else Log.w(Constants.LOG_TAG, "No results");
                    } else if (c.getCount() == 1) {
                        if (c.moveToFirst()) {
                            String number = c.getString(phoneIdx);
                            //selectedNumber.setText(number);
                            Intent intent = new Intent();
                            intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number);
                            intent.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    } else if (c.getCount() == 0) {
                        Intent intent = new Intent();
                        intent.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
        super.onPause();
    }
}
