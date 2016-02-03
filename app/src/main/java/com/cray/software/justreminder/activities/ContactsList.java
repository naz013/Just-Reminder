package com.cray.software.justreminder.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
import java.util.Collections;
import java.util.Comparator;

public class ContactsList extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private String name = "";

    private FloatingEditText searchField;
    private ListView contactsList;

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

        contactsList = (ListView) findViewById(R.id.contactsList);
        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    name = (String) parent.getItemAtPosition(position);
                    selectNumber();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Load().execute();
    }

    @Override
    protected void onPause() {
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
        super.onPause();
    }

    private void selectNumber() {
        Cursor c = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                new String[]{name}, null);

        int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

        if (c.getCount() > 1) {
            final CharSequence[] numbers = new CharSequence[c.getCount()];
            int i = 0;
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
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

    class Load extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;
        private ArrayList<String> contacts;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = ProgressDialog.show(ContactsList.this, null, getString(R.string.please_wait), true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
            contacts = new ArrayList<>();
            contacts.clear();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1"))
                        hasPhone = "true";
                    else
                        hasPhone = "false";
                    if (name != null) {
                        if (Boolean.parseBoolean(hasPhone)) {
                            contacts.add(name);
                        }
                    }
                }
                cursor.close();
            }
            try {
                Collections.sort(contacts, new Comparator<String>() {
                    @Override
                    public int compare(String e1, String e2) {
                        return e1.compareToIgnoreCase(e2);
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd != null && pd.isShowing()) pd.dismiss();

            adapter = new ArrayAdapter<>(ContactsList.this,
                    android.R.layout.simple_list_item_1, contacts);
            contactsList.setAdapter(adapter);
        }
    }
}
