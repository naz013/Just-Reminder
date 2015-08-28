package com.cray.software.justreminder.dialogs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.utils.ContactsList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.utils.ViewUtils;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class AddBirthday extends AppCompatActivity implements View.OnClickListener,
        DatePickerDialog.OnDateSetListener {

    EditText birthName, phone;
    Button pickContact;
    RelativeLayout contactLayout;
    LinearLayout container;
    TextView birthDate;
    CheckBox contactCheck;
    String number = "";
    int myYear = 0, myMonth = 0, myDay = 0;
    long id = 0;
    ProgressDialog pd;
    DataBase db;
    ColorSetter cs;
    SharedPrefs sPrefs;
    Toolbar toolbar;
    FloatingActionButton mFab;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cs = new ColorSetter(AddBirthday.this);
        sPrefs = new SharedPrefs(this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.add_birthday_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.add_birthday_button));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        birthName = (EditText) findViewById(R.id.birthName);
        birthDate = (TextView) findViewById(R.id.birthDate);
        phone = (EditText) findViewById(R.id.phone);
        birthDate.setOnClickListener(this);

        container = (LinearLayout) findViewById(R.id.container);

        contactLayout = (RelativeLayout) findViewById(R.id.contactLayout);

        contactCheck = (CheckBox) findViewById(R.id.contactCheck);
        contactCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) container.setVisibility(View.VISIBLE);
                else container.setVisibility(View.GONE);
            }
        });

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        Intent i = getIntent();
        id = i.getLongExtra("BDid", 0);
        if (id != 0) {
            db = new DataBase(AddBirthday.this);
            db.open();
            Cursor c = db.getBirthday(id);
            String dateStr = null;
            String name = null;
            if (c != null && c.moveToFirst()) {
                dateStr = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_BIRTHDAY));
                name = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NAME));
                number = c.getString(c.getColumnIndex(Constants.ContactConstants.COLUMN_CONTACT_NUMBER));
            }
            if (c != null) c.close();

            Date date = null;
            try {
                date = format.parse(dateStr);
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

            birthName.setText(name);
            if (number != null) phone.setText(number);

            toolbar.setTitle(getString(R.string.edit_birthday_title));
            if (number != null) contactCheck.setChecked(true);
        }

        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);

        birthDate.setText(format.format(calendar.getTime()));

        pickContact = (Button) findViewById(R.id.pickContact);
        ViewUtils.setImage(pickContact, sPrefs.loadBoolean(Prefs.USE_DARK_THEME));
        contactLayout.setOnClickListener(this);

        mFab = new FloatingActionButton(AddBirthday.this);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBirthday();
            }
        });
        mFab.setColorNormal(cs.colorSetter());
        mFab.setColorPressed(cs.colorChooser());
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);
        mFab.setIcon(R.drawable.ic_done_white_24dp);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.birthDate:
                dateDialog();
                break;
            case R.id.contactLayout:
                pd = ProgressDialog.show(AddBirthday.this, getString(R.string.load_contats), getString(R.string.loading_wait), true);
                pickContacts(pd);
                break;
        }
    }

    private void saveBirthday(){
        db = new DataBase(AddBirthday.this);
        db.open();
        if (id != 0){
            String contact = birthName.getText().toString();
            if (contact.matches("")){
                birthName.setError(getString(R.string.empty_field_error));
            } else {
                int contactId = Contacts.getContactIDFromNumber(number, AddBirthday.this);
                if (number != null) {
                    db.updateFullEvent(id, contact, contactId, birthDate.getText().toString(), myDay, myMonth, number);
                } else
                    db.updateFullEvent(id, contact, contactId, birthDate.getText().toString(), myDay, myMonth, null);

                finish();
            }
        } else {
            String uuId = SyncHelper.generateID();
            if (contactCheck.isChecked()) {
                String contact = birthName.getText().toString();
                if (contact.matches("")) {
                    birthName.setError(getString(R.string.empty_field_error));
                } else {
                    int id = Contacts.getContactIDFromNumber(number, AddBirthday.this);
                    if (number != null) {
                        db.addBirthday(contact, id, birthDate.getText().toString(), myDay, myMonth, number, uuId);
                    } else
                        db.addBirthday(contact, id, birthDate.getText().toString(), myDay, myMonth, null, uuId);

                    finish();
                }
            } else {
                String contact = birthName.getText().toString();
                if (!contact.matches("")) {
                    db.addBirthday(contact, 0, birthDate.getText().toString(), myDay, myMonth, null, uuId);
                    finish();
                } else {
                    birthName.setError(getString(R.string.empty_field_error));
                }
            }
        }
        db.close();
    }

    private void pickContacts(final ProgressDialog pd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                final ArrayList<String> contactsArray = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (name != null) {
                        contactsArray.add(name);
                    }
                }
                cursor.close();
                try {
                    Collections.sort(contactsArray, new Comparator<String>() {
                        @Override
                        public int compare(String e1, String e2) {
                            return e1.compareToIgnoreCase(e2);
                        }
                    });
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if ((pd != null) && pd.isShowing()) {
                                pd.dismiss();
                            }
                        } catch (final Exception e) {
                            // Handle or log or ignore
                        }
                        Intent i = new Intent(AddBirthday.this, ContactsList.class);
                        i.putStringArrayListExtra(Constants.SELECTED_CONTACT_ARRAY, contactsArray);
                        startActivityForResult(i, Constants.REQUEST_CODE_CONTACTS);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    protected void dateDialog() {
        final DatePickerDialog datePickerDialog =
                DatePickerDialog.newInstance(this, myYear, myMonth, myDay, false);
        datePickerDialog.setCloseOnSingleTapDay(false);
        datePickerDialog.show(getSupportFragmentManager(), "taa");
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        myYear = year;
        myMonth = monthOfYear;
        myDay = dayOfMonth;
        String monthStr;
        if (myMonth < 9){
            monthStr = "0" + (myMonth + 1);
        } else monthStr = String.valueOf((myMonth + 1));
        String dayStr;
        if (myDay < 10){
            dayStr = "0" + myDay;
        } else dayStr = String.valueOf(myDay);
        birthDate.setText(myYear + "-" + monthStr + "-" + dayStr);
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
}
