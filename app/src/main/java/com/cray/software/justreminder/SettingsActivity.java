package com.cray.software.justreminder;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.cray.software.justreminder.fragments.BirthdaysSettingsFragment;
import com.cray.software.justreminder.fragments.CalendarSettingsFragment;
import com.cray.software.justreminder.fragments.ExportSettingsFragment;
import com.cray.software.justreminder.fragments.ExtraSettingsFragment;
import com.cray.software.justreminder.fragments.GeneralSettingsFragment;
import com.cray.software.justreminder.fragments.LocationSettingsFragment;
import com.cray.software.justreminder.fragments.NotesSettingsFragment;
import com.cray.software.justreminder.fragments.NotificationSettingsFragment;
import com.cray.software.justreminder.fragments.OtherSettingsFragment;
import com.cray.software.justreminder.fragments.SettingsFragment;
import com.cray.software.justreminder.fragments.TimePickerFragment;
import com.cray.software.justreminder.fragments.VoiceSettingsFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;

import java.util.Calendar;

/**
 * Custom setting activity.
 */
public class SettingsActivity extends AppCompatActivity implements SettingsFragment.OnHeadlineSelectedListener,
        TimePickerFragment.TimePickedListener{

    private ColorSetter cSetter = new ColorSetter(SettingsActivity.this);
    private boolean isCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(SettingsActivity.this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorStatus());
        }
        setContentView(R.layout.category_layout);
        setRequestedOrientation(cSetter.getRequestOrientation());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.action_settings);

        findViewById(R.id.fragment_container).setBackgroundColor(cSetter.getBackgroundStyle());

        isCreate = true;

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            SettingsFragment firstFragment = new SettingsFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, firstFragment).commit();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(cSetter.getRequestOrientation());
        if (new SharedPrefs(this).loadBoolean(Prefs.UI_CHANGED) && !isCreate) recreate();
        isCreate = false;
        //new SharedPrefs(this).saveBoolean(Prefs.UI_CHANGED, false);
    }

    /**
     * Attach settings fragment.
     * @param position list position.
     */
    public void onArticleSelected(int position) {
        if (position == 0){
            GeneralSettingsFragment newFragment = new GeneralSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 1){
            ExportSettingsFragment newFragment = new ExportSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack("export");
            transaction.commit();
        } else if (position == 2){
            CalendarSettingsFragment newFragment = new CalendarSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 3){
            BirthdaysSettingsFragment newFragment = new BirthdaysSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack("birth");
            transaction.commit();
        } else if (position == 4){
            NotificationSettingsFragment newFragment = new NotificationSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 5){
            ExtraSettingsFragment newFragment = new ExtraSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 6){
            LocationSettingsFragment newFragment = new LocationSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 7){
            NotesSettingsFragment newFragment = new NotesSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 8){
            VoiceSettingsFragment newFragment = new VoiceSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (position == 9){
            OtherSettingsFragment newFragment = new OtherSettingsFragment();
            Bundle args = new Bundle();
            newFragment.setArguments(args);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onTimePicked(Calendar time) {
        BirthdaysSettingsFragment newFragment = new BirthdaysSettingsFragment();
        Bundle args = new Bundle();
        newFragment.setArguments(args);
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack("birth", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack("birth");
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 107:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new SharedPrefs(this).saveBoolean(Prefs.MISSED_CALL_REMINDER, true);
                } else {
                    new Permissions(SettingsActivity.this).showInfo(SettingsActivity.this, Permissions.READ_PHONE_STATE);
                }
                break;
            case 108:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new SharedPrefs(this).saveBoolean(Prefs.QUICK_SMS, true);
                } else {
                    new Permissions(SettingsActivity.this).showInfo(SettingsActivity.this, Permissions.READ_PHONE_STATE);
                }
                break;
            case 109:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    new SharedPrefs(this).saveBoolean(Prefs.FOLLOW_REMINDER, true);
                } else {
                    new Permissions(SettingsActivity.this).showInfo(SettingsActivity.this, Permissions.READ_PHONE_STATE);
                }
                break;
        }
    }
}