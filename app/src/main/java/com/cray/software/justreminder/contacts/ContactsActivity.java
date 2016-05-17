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

package com.cray.software.justreminder.contacts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements LoadListener, RecyclerClickListener {

    private ContactsRecyclerAdapter mAdapter;
    private List<ContactData> mData;
    private String name = "";

    private RoboEditText searchField;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.activity_contacts_list);
        setRequestedOrientation(cs.getRequestOrientation());
        initActionBar();
        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());
        initSearchView();
        initRecyclerView();
        new ContactsAsync(this, this).execute();
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.contactsList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
    }

    private void initSearchView() {
        searchField = (RoboEditText) findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void filterContacts(String q) {
        List<ContactData> res = filter(mData, q);
        mAdapter.animateTo(res);
        mRecyclerView.scrollToPosition(0);
    }

    private List<ContactData> filter(List<ContactData> mData, String q) {
        q = q.toLowerCase();

        List<ContactData> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<ContactData> getFiltered(List<ContactData> models, String query) {
        List<ContactData> list = new ArrayList<>();
        for (ContactData model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    private void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.contacts));
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager)getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
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
                            getResources(), c.getInt(phoneType), "");
                    String number = type + ": " + c.getString(phoneIdx);
                    numbers[i++] = number;
                    c.moveToNext();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(ContactsActivity.this);
                builder.setItems(numbers, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String number = (String) numbers[which];
                        int index = number.indexOf(":");
                        number = number.substring(index + 2);
                        Intent intent = new Intent();
                        intent.putExtra(Constants.SELECTED_CONTACT_NUMBER, number);
                        intent.putExtra(Constants.SELECTED_CONTACT_NAME, name);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setOwnerActivity(ContactsActivity.this);
                alert.show();

            }
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

    @Override
    public void onLoaded(List<ContactData> list) {
        this.mData = list;
        mAdapter = new ContactsRecyclerAdapter(this, mData, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(int position) {
        if (position != -1) {
            name = mAdapter.getItem(position).getName();
            selectNumber();
        }
    }
}
