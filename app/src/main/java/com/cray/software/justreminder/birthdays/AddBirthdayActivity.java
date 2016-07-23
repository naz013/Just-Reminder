/**
 * Copyright 2015 Nazar Suhovich
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

package com.cray.software.justreminder.birthdays;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.roboto_views.RoboCheckBox;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Add new or edit birthday activity.
 */
public class AddBirthdayActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Edit text fields.
     */
    private RoboEditText birthName, phone;

    /**
     * Container.
     */
    private LinearLayout container;

    /**
     * Date of birth text view.
     */
    private RoboTextView birthDate;

    /**
     * Attack contact check.
     */
    private RoboCheckBox contactCheck;

    /**
     * Contact phone number.
     */
    private String number = "";

    /**
     * Selected year, month and day of birth.
     */
    private int myYear = 0, myMonth = 0, myDay = 0;

    /**
     * Edited birthday item.
     */
    private BirthdayItem mItem;

    /**
     * Simple date format field.
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(AddBirthdayActivity.this);
        setTheme(cs.getStyle());
        if (Module.isLollipop()) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.add_birthday_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        birthName = (RoboEditText) findViewById(R.id.birthName);
        birthDate = (RoboTextView) findViewById(R.id.birthDate);
        phone = (RoboEditText) findViewById(R.id.phone);
        birthDate.setOnClickListener(this);

        ImageView dateIcon = (ImageView) findViewById(R.id.dateIcon);
        if (cs.isDark()){
            dateIcon.setImageResource(R.drawable.ic_event_white_24dp);
        } else {
            dateIcon.setImageResource(R.drawable.ic_event_black_24dp);
        }

        container = (LinearLayout) findViewById(R.id.container);
        container.setVisibility(View.GONE);

        contactCheck = (RoboCheckBox) findViewById(R.id.contactCheck);
        contactCheck.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) container.setVisibility(View.VISIBLE);
            else container.setVisibility(View.GONE);
        });

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent intent = getIntent();
        long id = intent.getLongExtra("BDid", 0);
        String filePath = intent.getStringExtra(Constants.EDIT_PATH);
        long receivedDate = intent.getLongExtra("date", 0);
        mItem = BirthdayHelper.getInstance(this).getBirthday(id);
        if (mItem != null) {
            String name = mItem.getName();
            try {
                Date date = format.parse(mItem.getDate());
                if (date != null) calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            birthName.setText(name);
            if (number != null) phone.setText(number);
            toolbar.setTitle(R.string.edit_birthday);
            if (number != null) contactCheck.setChecked(true);
        } else if (receivedDate != 0) {
            calendar.setTimeInMillis(receivedDate);
        } else if (filePath != null) {
            mItem = SyncHelper.getBirthday(filePath);
            if (mItem != null) {
                number = mItem.getNumber();
                Date date = null;
                try {
                    date = format.parse(mItem.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null) {
                    try {
                        calendar.setTime(date);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }

                birthName.setText(mItem.getName());
                if (number != null) phone.setText(number);
                toolbar.setTitle(R.string.edit_birthday);
                if (number != null) contactCheck.setChecked(true);
            } else finish();
        }

        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);
        birthDate.setText(format.format(calendar.getTime()));
        ImageButton pickContact = (ImageButton) findViewById(R.id.pickContact);
        ViewUtils.setImage(pickContact, cs.isDark());
        pickContact.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.birthDate:
                dateDialog();
                break;
            case R.id.pickContact:
                if (Permissions.checkPermission(AddBirthdayActivity.this, Permissions.READ_CONTACTS, Permissions.READ_CALLS)) {
                    SuperUtil.selectContact(AddBirthdayActivity.this, Constants.REQUEST_CODE_CONTACTS);
                } else {
                    Permissions.requestPermission(AddBirthdayActivity.this, 101, Permissions.READ_CONTACTS, Permissions.READ_CALLS);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Save birthday to database.
     */
    private void saveBirthday() {
        String contact = birthName.getText().toString();
        if (contact.matches("")) {
            birthName.setError(getString(R.string.must_be_not_empty));
            return;
        }
        int contactId = com.cray.software.justreminder.contacts.Contacts.getIdFromNumber(number, AddBirthdayActivity.this);
        if (mItem != null) {
            if (mItem.getUuId() == null) {
                String uuId = SyncHelper.generateID();
                mItem.setUuId(uuId);
            }
            mItem.setName(contact);
            mItem.setContactId(contactId);
            mItem.setDate(birthDate.getText().toString());
            mItem.setNumber(number);
            mItem.setDay(myDay);
            mItem.setMonth(myMonth);
        } else {
            String uuId = SyncHelper.generateID();
            mItem = new BirthdayItem(0, contact, birthDate.getText().toString(), number, null, uuId, contactId, myDay, myMonth);
        }
        BirthdayHelper.getInstance(this).saveBirthday(mItem);
        finish();
        SharedPrefs.getInstance(this).putBoolean(Prefs.REMINDER_CHANGED, true);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONTACTS) {
            if (resultCode == RESULT_OK) {
                //Use Data to get string
                String name = data.getStringExtra(Constants.SELECTED_CONTACT_NAME);
                number = data.getStringExtra(Constants.SELECTED_CONTACT_NUMBER);
                if (birthName.getText().toString().matches("")) {
                    birthName.setText(name);
                }
                phone.setText(number);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SuperUtil.selectContact(AddBirthdayActivity.this, Constants.REQUEST_CODE_CONTACTS);
                }
                break;
        }
    }

    /**
     * Show date picker dialog.
     */
    protected void dateDialog() {
        new DatePickerDialog(this, myDateCallBack, myYear, myMonth, myDay).show();
    }

    /**
     * Date selection callback.
     */
    DatePickerDialog.OnDateSetListener myDateCallBack = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            myYear = year;
            myMonth = monthOfYear;
            myDay = dayOfMonth;
            String monthStr;
            if (myMonth < 9) {
                monthStr = "0" + (myMonth + 1);
            } else monthStr = String.valueOf((myMonth + 1));
            String dayStr;
            if (myDay < 10) {
                dayStr = "0" + myDay;
            } else dayStr = String.valueOf(myDay);
            birthDate.setText(SuperUtil.appendString(String.valueOf(myYear), "-", monthStr, "-", dayStr));
        }
    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                saveBirthday();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
