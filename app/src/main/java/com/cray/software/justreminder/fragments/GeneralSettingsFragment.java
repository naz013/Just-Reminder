package com.cray.software.justreminder.fragments;


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
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.dialogs.ThemerDialog;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.widgets.UpdatesHelper;

public class GeneralSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {

    private CheckBox useDarkStyleCheck, smartFoldCheck, wearEnableCheck, animationsCheck,
            use24TimeCheck, extendedButtonCheck, itemPreviewCheck;
    private View themeColorSwitcher;
    private SharedPrefs sPrefs;
    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_general, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.interface_block);
        }

        getActivity().getIntent().setAction("General attached");

        RelativeLayout themeColor = (RelativeLayout) rootView.findViewById(R.id.themeColor);
        themeColorSwitcher = rootView.findViewById(R.id.themeColorSwitcher);

        themeView();
        themeColor.setOnClickListener(this);

        RelativeLayout useDarkStyle = (RelativeLayout) rootView.findViewById(R.id.useDarkStyle);
        useDarkStyle.setOnClickListener(this);

        useDarkStyleCheck = (CheckBox) rootView.findViewById(R.id.useDarkStyleCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        useDarkStyleCheck.setChecked(sPrefs.loadBoolean(Prefs.USE_DARK_THEME));

        RelativeLayout use24Time = (RelativeLayout) rootView.findViewById(R.id.use24Time);
        use24Time.setOnClickListener(this);

        use24TimeCheck = (CheckBox) rootView.findViewById(R.id.use24TimeCheck);
        use24TimeCheck.setChecked(sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));

        RelativeLayout smartFold = (RelativeLayout) rootView.findViewById(R.id.smartFold);
        smartFold.setOnClickListener(this);

        smartFoldCheck = (CheckBox) rootView.findViewById(R.id.smartFoldCheck);
        smartFoldCheck.setChecked(sPrefs.loadBoolean(Prefs.SMART_FOLD));

        TextView screenOrientation = (TextView) rootView.findViewById(R.id.screenOrientation);
        screenOrientation.setOnClickListener(this);

        RelativeLayout wearEnable = (RelativeLayout) rootView.findViewById(R.id.wearEnable);
        wearEnable.setOnClickListener(this);

        wearEnableCheck = (CheckBox) rootView.findViewById(R.id.wearEnableCheck);
        wearEnableCheck.setChecked(sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION));

        RelativeLayout animations = (RelativeLayout) rootView.findViewById(R.id.animations);
        animations.setOnClickListener(this);

        animationsCheck = (CheckBox) rootView.findViewById(R.id.animationsCheck);
        animationsCheck.setChecked(sPrefs.loadBoolean(Prefs.ANIMATIONS));

        RelativeLayout extendedButton = (RelativeLayout) rootView.findViewById(R.id.extendedButton);
        extendedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extendedChange();
            }
        });

        extendedButtonCheck = (CheckBox) rootView.findViewById(R.id.extendedButtonCheck);
        extendedButtonCheck.setChecked(sPrefs.loadBoolean(Prefs.EXTENDED_BUTTON));

        RelativeLayout itemPreview = (RelativeLayout) rootView.findViewById(R.id.itemPreview);
        itemPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemPreviewChange();
            }
        });

        itemPreviewCheck = (CheckBox) rootView.findViewById(R.id.itemPreviewCheck);
        itemPreviewCheck.setChecked(sPrefs.loadBoolean(Prefs.ITEM_PREVIEW));
        return rootView;
    }

    private void itemPreviewChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (itemPreviewCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.ITEM_PREVIEW, false);
            itemPreviewCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.ITEM_PREVIEW, true);
            itemPreviewCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    private void extendedChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (extendedButtonCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.EXTENDED_BUTTON, false);
            extendedButtonCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.EXTENDED_BUTTON, true);
            extendedButtonCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    private void useDarkStyleChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (useDarkStyleCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.USE_DARK_THEME, false);
            useDarkStyleCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.USE_DARK_THEME, true);
            useDarkStyleCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
        getActivity().recreate();
    }

    private void _24Change (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (use24TimeCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, false);
            use24TimeCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.IS_24_TIME_FORMAT, true);
            use24TimeCheck.setChecked(true);
        }

        new UpdatesHelper(getActivity()).updateWidget();
    }

    private void wearChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wearEnableCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, false);
            wearEnableCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WEAR_NOTIFICATION, true);
            wearEnableCheck.setChecked(true);
        }
    }

    private void animationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (animationsCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.ANIMATIONS, false);
            animationsCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.ANIMATIONS, true);
            animationsCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Prefs.UI_CHANGED, true);
    }

    private void smartFoldChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (smartFoldCheck.isChecked()){
            sPrefs.saveBoolean(Prefs.SMART_FOLD, false);
            smartFoldCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.SMART_FOLD, true);
            smartFoldCheck.setChecked(true);
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
        themeColorSwitcher.setBackgroundResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.themeColor:
                Intent i = new Intent(getActivity().getApplicationContext(), ThemerDialog.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(i);
                break;
            case R.id.useDarkStyle:
                useDarkStyleChange();
                break;
            case R.id.use24Time:
                _24Change();
                break;
            case R.id.wearEnable:
                wearChange();
                break;
            case R.id.animations:
                animationChange();
                break;
            case R.id.smartFold:
                smartFoldChange();
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
