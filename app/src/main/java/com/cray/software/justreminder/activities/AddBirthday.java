package com.cray.software.justreminder.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Contacts;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
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
public class AddBirthday extends AppCompatActivity implements View.OnClickListener {

    /**
     * Edit text fields.
     */
    private EditText birthName, phone;

    /**
     * Container.
     */
    private LinearLayout container;

    /**
     * Date of birth text view.
     */
    private TextView birthDate;

    /**
     * Attack contact check.
     */
    private CheckBox contactCheck;

    /**
     * Contact phone number.
     */
    private String number = "";

    /**
     * Selected year, month and day of birth.
     */
    private int myYear = 0, myMonth = 0, myDay = 0;

    /**
     * Edited birthday identifier.
     */
    private long id = 0;

    /**
     * Simple date format field.
     */
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ColorSetter cs = new ColorSetter(AddBirthday.this);
        SharedPrefs sPrefs = new SharedPrefs(this);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ViewUtils.getColor(this, cs.colorPrimaryDark()));
        }
        setContentView(R.layout.add_birthday_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        birthName = (EditText) findViewById(R.id.birthName);
        birthDate = (TextView) findViewById(R.id.birthDate);
        phone = (EditText) findViewById(R.id.phone);
        birthDate.setOnClickListener(this);

        ImageView dateIcon = (ImageView) findViewById(R.id.dateIcon);
        if (sPrefs.loadBoolean(Prefs.USE_DARK_THEME)){
            dateIcon.setImageResource(R.drawable.ic_event_white_24dp);
        } else {
            dateIcon.setImageResource(R.drawable.ic_event_black_24dp);
        }

        container = (LinearLayout) findViewById(R.id.container);
        container.setVisibility(View.GONE);

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
        long receivedDate = i.getLongExtra("date", 0);
        if (id != 0) {
            DataBase db = new DataBase(AddBirthday.this);
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
            db.close();

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
        } else if (receivedDate != 0) {
            calendar.setTimeInMillis(receivedDate);
        }

        myYear = calendar.get(Calendar.YEAR);
        myMonth = calendar.get(Calendar.MONTH);
        myDay = calendar.get(Calendar.DAY_OF_MONTH);

        birthDate.setText(format.format(calendar.getTime()));

        ImageButton pickContact = (ImageButton) findViewById(R.id.pickContact);
        ViewUtils.setImage(pickContact, sPrefs.loadBoolean(Prefs.USE_DARK_THEME));
        pickContact.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.birthDate:
                dateDialog();
                break;
            case R.id.pickContact:
                SuperUtil.selectContact(AddBirthday.this, Constants.REQUEST_CODE_CONTACTS);
                break;
            default:
                break;
        }
    }

    /**
     * Save birthday to database.
     */
    private void saveBirthday() {
        DataBase db = new DataBase(AddBirthday.this);
        db.open();
        if (id != 0) {
            String contact = birthName.getText().toString();
            if (contact.matches("")) {
                birthName.setError(getString(R.string.empty_field_error));
            } else {
                int contactId = Contacts.getContactIDFromNumber(number, AddBirthday.this);
                if (number != null) {
                    db.updateFullEvent(id, contact, contactId, birthDate.getText().toString(), myDay, myMonth, number);
                } else
                    db.updateFullEvent(id, contact, contactId, birthDate.getText().toString(), myDay, myMonth, null);

                db.close();
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

                    db.close();
                    finish();
                }
            } else {
                String contact = birthName.getText().toString();
                if (!contact.matches("")) {
                    db.addBirthday(contact, 0, birthDate.getText().toString(), myDay, myMonth, null, uuId);
                    db.close();
                    finish();
                } else {
                    birthName.setError(getString(R.string.empty_field_error));
                }
            }
        }
        db.close();
        new SharedPrefs(this).saveBoolean(Prefs.REMINDER_CHANGED, true);
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
