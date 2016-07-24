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
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.services.WearService;
import com.cray.software.justreminder.views.PrefsView;

public class GeneralSettingsFragment extends Fragment implements View.OnClickListener, DialogInterface.OnDismissListener {
    
    private SharedPrefs sPrefs;
    private ActionBar ab;
    private PrefsView use24TimePrefs, useDarkStylePrefs, themeColorPrefs,
            smartFoldPrefs, wearEnablePrefs, itemPreviewPrefs, wearPrefs, dayNightPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.settings_general, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.general);
        }
        getActivity().getIntent().setAction("General attached");
        sPrefs = SharedPrefs.getInstance(getActivity());

        use24TimePrefs = (PrefsView) rootView.findViewById(R.id.use24TimePrefs);
        use24TimePrefs.setChecked(sPrefs.getBoolean(Prefs.IS_24_TIME_FORMAT));
        use24TimePrefs.setOnClickListener(this);

        useDarkStylePrefs = (PrefsView) rootView.findViewById(R.id.useDarkStylePrefs);
        useDarkStylePrefs.setChecked(sPrefs.getBoolean(Prefs.USE_DARK_THEME));
        useDarkStylePrefs.setOnClickListener(this);

        dayNightPrefs = (PrefsView) rootView.findViewById(R.id.dayNightPrefs);
        dayNightPrefs.setChecked(sPrefs.getBoolean(Prefs.DAY_NIGHT));
        dayNightPrefs.setOnClickListener(this);

        themeColorPrefs = (PrefsView) rootView.findViewById(R.id.themeColorPrefs);
        themeColorPrefs.setOnClickListener(this);

        smartFoldPrefs = (PrefsView) rootView.findViewById(R.id.smartFoldPrefs);
        smartFoldPrefs.setChecked(sPrefs.getBoolean(Prefs.SMART_FOLD));
        smartFoldPrefs.setOnClickListener(this);

        wearEnablePrefs = (PrefsView) rootView.findViewById(R.id.wearEnablePrefs);
        wearEnablePrefs.setChecked(sPrefs.getBoolean(Prefs.WEAR_NOTIFICATION));
        wearEnablePrefs.setOnClickListener(this);

        if (Module.isJellyMR2()) wearEnablePrefs.setVisibility(View.VISIBLE);
        else wearEnablePrefs.setVisibility(View.GONE);

        itemPreviewPrefs = (PrefsView) rootView.findViewById(R.id.itemPreviewPrefs);
        itemPreviewPrefs.setChecked(sPrefs.getBoolean(Prefs.ITEM_PREVIEW));
        itemPreviewPrefs.setOnClickListener(this);

        TextView screenOrientation = (TextView) rootView.findViewById(R.id.screenOrientation);
        screenOrientation.setOnClickListener(this);

        wearPrefs = (PrefsView) rootView.findViewById(R.id.wearPrefs);
        wearPrefs.setChecked(sPrefs.getBoolean(Prefs.WEAR_SERVICE));
        wearPrefs.setOnClickListener(this);
        if (Module.isJellyMR2())
            wearPrefs.setVisibility(View.VISIBLE);
        else
            wearPrefs.setVisibility(View.GONE);

        themeView();
        
        return rootView;
    }

    private void wearServiceChange() {
        if (wearPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.WEAR_SERVICE, false);
            wearPrefs.setChecked(false);
            getActivity().stopService(new Intent(getActivity(), WearService.class));
        } else {
            sPrefs.putBoolean(Prefs.WEAR_SERVICE, true);
            wearPrefs.setChecked(true);
            getActivity().startService(new Intent(getActivity(), WearService.class));
        }
    }

    private void itemPreviewChange() {
        if (itemPreviewPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.ITEM_PREVIEW, false);
            itemPreviewPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.ITEM_PREVIEW, true);
            itemPreviewPrefs.setChecked(true);
        }
        sPrefs.putBoolean(Prefs.UI_CHANGED, true);
    }

    private void useDarkStyleChange (){
        if (useDarkStylePrefs.isChecked()){
            sPrefs.putBoolean(Prefs.USE_DARK_THEME, false);
            useDarkStylePrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.USE_DARK_THEME, true);
            sPrefs.putBoolean(Prefs.DAY_NIGHT, false);
            useDarkStylePrefs.setChecked(true);
            dayNightPrefs.setChecked(false);
        }
        sPrefs.putBoolean(Prefs.UI_CHANGED, true);
        getActivity().recreate();
    }

    private void dayNightChange (){
        if (dayNightPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.DAY_NIGHT, false);
            dayNightPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.DAY_NIGHT, true);
            sPrefs.putBoolean(Prefs.USE_DARK_THEME, false);
            dayNightPrefs.setChecked(true);
            useDarkStylePrefs.setChecked(false);
        }
        sPrefs.putBoolean(Prefs.UI_CHANGED, true);
        getActivity().recreate();
    }

    private void _24Change (){
        if (use24TimePrefs.isChecked()){
            sPrefs.putBoolean(Prefs.IS_24_TIME_FORMAT, false);
            use24TimePrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.IS_24_TIME_FORMAT, true);
            use24TimePrefs.setChecked(true);
        }
        UpdatesHelper.getInstance(getActivity()).updateWidget();
    }

    private void wearChange (){
        if (wearEnablePrefs.isChecked()){
            sPrefs.putBoolean(Prefs.WEAR_NOTIFICATION, false);
            wearEnablePrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.WEAR_NOTIFICATION, true);
            wearEnablePrefs.setChecked(true);
        }
    }

    private void smartFoldChange (){
        if (smartFoldPrefs.isChecked()){
            sPrefs.putBoolean(Prefs.SMART_FOLD, false);
            smartFoldPrefs.setChecked(false);
        } else {
            sPrefs.putBoolean(Prefs.SMART_FOLD, true);
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
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }

    private void themeView(){
        int loadedColor = sPrefs.getInt(Prefs.APP_THEME);
        themeColorPrefs.setViewResource(new ColorSetter(getActivity()).getIndicator(loadedColor));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.themeColorPrefs: {
                Intent intent = new Intent(getActivity().getApplicationContext(), ThemerDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
                break;
            case R.id.useDarkStylePrefs:
                useDarkStyleChange();
                break;
            case R.id.dayNightPrefs:
                dayNightChange();
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
        new Handler().post(() -> {
            try {
                getActivity().recreate();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
        });
    }
}
