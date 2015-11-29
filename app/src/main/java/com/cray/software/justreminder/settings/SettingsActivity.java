package com.cray.software.justreminder.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.fragments.helpers.TimePickerFragment;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.settings.fragments.BirthdaysSettingsFragment;
import com.cray.software.justreminder.settings.fragments.CalendarSettingsFragment;
import com.cray.software.justreminder.settings.fragments.ExportSettingsFragment;
import com.cray.software.justreminder.settings.fragments.ExtraSettingsFragment;
import com.cray.software.justreminder.settings.fragments.GeneralSettingsFragment;
import com.cray.software.justreminder.settings.fragments.LocationSettingsFragment;
import com.cray.software.justreminder.settings.fragments.NotesSettingsFragment;
import com.cray.software.justreminder.settings.fragments.NotificationSettingsFragment;
import com.cray.software.justreminder.settings.fragments.OtherSettingsFragment;
import com.cray.software.justreminder.settings.fragments.SettingsFragment;
import com.cray.software.justreminder.settings.fragments.VoiceSettingsFragment;

import java.io.File;
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
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 200:
                if (resultCode == RESULT_OK) {
                    new SharedPrefs(this).saveBoolean(Prefs.BIRTHDAY_CUSTOM_SOUND, true);
                    String pathC = data.getStringExtra(Constants.FILE_PICKED);
                    if (pathC != null) {
                        File fileC = new File(pathC);
                        if (fileC.exists()) {
                            String fileName = fileC.getName();
                            if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg")) {
                                new SharedPrefs(this).savePrefs(Prefs.BIRTHDAY_CUSTOM_SOUND_FILE, fileC.toString());
                            }
                        }
                    }
                }
                break;
            case 201:
                if (resultCode == RESULT_OK) {
                    new SharedPrefs(this).saveBoolean(Prefs.CUSTOM_SOUND, true);
                    String pathStr = data.getStringExtra(Constants.FILE_PICKED);
                    if (pathStr != null) {
                        File fileC = new File(pathStr);
                        if (fileC.exists()) {
                            String fileName = fileC.getName();
                            if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg")) {
                                new SharedPrefs(this).savePrefs(Prefs.CUSTOM_SOUND_FILE, fileC.toString());
                            }
                        }
                    }
                }
                break;
            case Constants.ACTION_REQUEST_GALLERY:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    new SharedPrefs(this).savePrefs(Prefs.REMINDER_IMAGE, selectedImage.toString());
                }
                break;
        }
    }
}