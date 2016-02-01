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
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.views.PrefsView;
import com.cray.software.justreminder.widgets.utils.UpdatesHelper;

public class GeneralSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {
    
    private SharedPrefs sPrefs;
    private ActionBar ab;
    
    private PrefsView use24TimePrefs, useDarkStylePrefs, themeColorPrefs,
            smartFoldPrefs, wearEnablePrefs, itemPreviewPrefs, wearPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.settings_general, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.general);
        }

        getActivity().getIntent().setAction("General attached");
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());

        use24TimePrefs = (PrefsView) rootView.findViewById(R.id.use24TimePrefs);
        use24TimePrefs.setChecked(sPrefs.loadBoolean(Prefs.IS_24_TIME_FORMAT));
        use24TimePrefs.setOnClickListener(this);

        useDarkStylePrefs = (PrefsView) rootView.findViewById(R.id.useDarkStylePrefs);
        useDarkStylePrefs.setChecked(sPrefs.loadBoolean(Prefs.USE_DARK_THEME));
        useDarkStylePrefs.setOnClickListener(this);

        themeColorPrefs = (PrefsView) rootView.findViewById(R.id.themeColorPrefs);
        themeColorPrefs.setOnClickListener(this);

        smartFoldPrefs = (PrefsView) rootView.findViewById(R.id.smartFoldPrefs);
        smartFoldPrefs.setChecked(sPrefs.loadBoolean(Prefs.SMART_FOLD));
        smartFoldPrefs.setOnClickListener(this);

        wearEnablePrefs = (PrefsView) rootView.findViewById(R.id.wearEnablePrefs);
        wearEnablePrefs.setChecked(sPrefs.loadBoolean(Prefs.WEAR_NOTIFICATION));
        wearEnablePrefs.setOnClickListener(this);

        if (Module.isJellyMR2()) wearEnablePrefs.setVisibility(View.VISIBLE);
        else wearEnablePrefs.setVisibility(View.GONE);

        itemPreviewPrefs = (PrefsView) rootView.findViewById(R.id.itemPreviewPrefs);
        itemPreviewPrefs.setChecked(sPrefs.loadBoolean(Prefs.ITEM_PREVIEW));
        itemPreviewPrefs.setOnClickListener(this);

        TextView screenOrientation = (TextView) rootView.findViewById(R.id.screenOrientation);
        screenOrientation.setOnClickListener(this);

        wearPrefs = (PrefsView) rootView.findViewById(R.id.wearPrefs);
        wearPrefs.setChecked(sPrefs.loadBoolean(Prefs.WEAR_SERVICE));
        wearPrefs.setOnClickListener(this);
        if (Module.isJellyMR2()) wearPrefs.setVisibility(View.VISIBLE);
        //else
            wearPrefs.setVisibility(View.GONE);

        themeView();
        
        return rootView;
    }

    private void wearServiceChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wearPrefs.isChecked()){
            sPrefs.saveBoolean(Prefs.WEAR_SERVICE, false);
            wearPrefs.setChecked(false);
        } else {
            sPrefs.saveBoolean(Prefs.WEAR_SERVICE, true);
            wearPrefs.setChecked(true);
        }
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
        int loadedColor = sPrefs.loadInt(Prefs.APP_THEME);
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
            case R.id.smartFoldPrefs:
                smartFoldChange();
                break;
            case R.id.itemPreviewPrefs:
                itemPreviewChange();
                break;
            case R.id.wearPrefs:
                wearServiceChange();
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
