package com.cray.software.justreminder.settings.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.ThemerDialog;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.views.PrefsView;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class GeneralSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {
    
    private SharedPrefs sPrefs;
    private ActionBar ab;
    
    private PrefsView use24TimePrefs, useDarkStylePrefs, animationsPrefs, themeColorPrefs, 
            smartFoldPrefs, wearEnablePrefs, extendedButtonPrefs, itemPreviewPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_general, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.interface_block);
        }

        getActivity().getIntent().setAction("General attached");
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        use24TimePrefs = (PrefsView) rootView.findViewById(R.id.use24TimePrefs);
        use24TimePrefs.setChecked(sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
        use24TimePrefs.setOnClickListener(this);

        useDarkStylePrefs = (PrefsView) rootView.findViewById(R.id.useDarkStylePrefs);
        useDarkStylePrefs.setChecked(sPrefs.loadBoolean(Prefs.USE_DARK_THEME));
        useDarkStylePrefs.setOnClickListener(this);

        animationsPrefs = (PrefsView) rootView.findViewById(R.id.animationsPrefs);
        animationsPrefs.setChecked(sPrefs.loadBoolean(Prefs.ANIMATIONS));
        animationsPrefs.setOnClickListener(this);

        themeColorPrefs = (PrefsView) rootView.findViewById(R.id.themeColorPrefs);
        themeColorPrefs.setOnClickListener(this);

        smartFoldPrefs = (PrefsView) rootView.findViewById(R.id.smartFoldPrefs);
        smartFoldPrefs.setChecked(sPrefs.loadBoolean(Prefs.SMART_FOLD));
        smartFoldPrefs.setOnClickListener(this);

        wearEnablePrefs = (PrefsView) rootView.findViewById(R.id.wearEnablePrefs);
        wearEnablePrefs.setChecked(sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION));
        wearEnablePrefs.setOnClickListener(this);

        extendedButtonPrefs = (PrefsView) rootView.findViewById(R.id.extendedButtonPrefs);
        extendedButtonPrefs.setChecked(sPrefs.loadBoolean(Prefs.EXTENDED_BUTTON));
        extendedButtonPrefs.setOnClickListener(this);

        itemPreviewPrefs = (PrefsView) rootView.findViewById(R.id.itemPreviewPrefs);
        itemPreviewPrefs.setChecked(sPrefs.loadBoolean(Prefs.ITEM_PREVIEW));
        itemPreviewPrefs.setOnClickListener(this);

        TextView screenOrientation = (TextView) rootView.findViewById(R.id.screenOrientation);
        screenOrientation.setOnClickListener(this);

        themeView();
        
        return rootView;
    }

    private void itemPreviewChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (itemPreviewPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.ITEM_PREVIEW, false);
            itemPreviewPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.ITEM_PREVIEW, true);
            itemPreviewPrefs.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    private void extendedChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (extendedButtonPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.EXTENDED_BUTTON, false);
            extendedButtonPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.EXTENDED_BUTTON, true);
            extendedButtonPrefs.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    private void useDarkStyleChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (useDarkStylePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.USE_DARK_THEME, false);
            useDarkStylePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.USE_DARK_THEME, true);
            useDarkStylePrefs.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
        getActivity().recreate();
    }

    private void _24Change (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (use24TimePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, false);
            use24TimePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true);
            use24TimePrefs.setChecked(true);
        }

        new UpdatesHelper(getActivity()).updateWidget();
    }

    private void wearChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wearEnablePrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false);
            wearEnablePrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, true);
            wearEnablePrefs.setChecked(true);
        }
    }

    private void animationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (animationsPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.ANIMATIONS, false);
            animationsPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.ANIMATIONS, true);
            animationsPrefs.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    private void smartFoldChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (smartFoldPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.SMART_FOLD, false);
            smartFoldPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SMART_FOLD, true);
            smartFoldPrefs.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        themeView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    private void themeView(){
        sPrefs = new SharedPrefs(getActivity());
        String loadedColor = sPrefs.loadPrefs(Prefs.THEME);
        themeColorPrefs.setViewResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.themeColorPrefs:
                Intent i = new Intent(getActivity().getApplicationContext(), ThemerDialog.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(i);
                break;
            case R.id.useDarkStylePrefs:
                useDarkStyleChange();
                break;
            case R.id.use24TimePrefs:
                _24Change();
                break;
            case R.id.wearEnablePrefs:
                wearChange();
                break;
            case R.id.animationsPrefs:
                animationChange();
                break;
            case R.id.smartFoldPrefs:
                smartFoldChange();
                break;
            case R.id.extendedButtonPrefs:
                extendedChange();
                break;
            case R.id.itemPreviewPrefs:
                itemPreviewChange();
                break;
            case R.id.screenOrientation:
                Dialogues.orientationDialog(getActivity(), this);
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    getActivity().recreate();
                } catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        });
    }
}
