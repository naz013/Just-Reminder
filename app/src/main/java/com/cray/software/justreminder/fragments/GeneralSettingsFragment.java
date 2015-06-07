package com.cray.software.justreminder.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.dialogs.ThemerDialog;
import com.cray.software.justreminder.dialogs.utils.ScreenOrientation;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.widgets.UpdatesHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

public class GeneralSettingsFragment extends Fragment implements View.OnClickListener {

    RelativeLayout themeColor, useDarkStyle, smartFold, wearEnable, animations, use24Time,
            wearable, extendedButton, itemPreview;
    CheckBox useDarkStyleCheck, smartFoldCheck, wearEnableCheck, animationsCheck, use24TimeCheck,
            wearableCheck, extendedButtonCheck, itemPreviewCheck;
    TextView screenOrientation;
    View themeColorSwitcher;
    SharedPrefs sPrefs;
    ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.general_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.interface_block);
        }

        getActivity().getIntent().setAction("General attached");

        themeColor = (RelativeLayout) rootView.findViewById(R.id.themeColor);
        themeColorSwitcher = rootView.findViewById(R.id.themeColorSwitcher);

        themeView();
        themeColor.setOnClickListener(this);

        useDarkStyle = (RelativeLayout) rootView.findViewById(R.id.useDarkStyle);
        useDarkStyle.setOnClickListener(this);

        useDarkStyleCheck = (CheckBox) rootView.findViewById(R.id.useDarkStyleCheck);
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        useDarkStyleCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME));

        use24Time = (RelativeLayout) rootView.findViewById(R.id.use24Time);
        use24Time.setOnClickListener(this);

        use24TimeCheck = (CheckBox) rootView.findViewById(R.id.use24TimeCheck);
        use24TimeCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT));

        smartFold = (RelativeLayout) rootView.findViewById(R.id.smartFold);
        smartFold.setOnClickListener(this);

        smartFoldCheck = (CheckBox) rootView.findViewById(R.id.smartFoldCheck);
        smartFoldCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD));

        screenOrientation = (TextView) rootView.findViewById(R.id.screenOrientation);
        screenOrientation.setOnClickListener(this);

        wearEnable = (RelativeLayout) rootView.findViewById(R.id.wearEnable);
        wearEnable.setOnClickListener(this);

        wearEnableCheck = (CheckBox) rootView.findViewById(R.id.wearEnableCheck);
        wearEnableCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WEAR_NOTIFICATION));

        animations = (RelativeLayout) rootView.findViewById(R.id.animations);
        animations.setOnClickListener(this);

        animationsCheck = (CheckBox) rootView.findViewById(R.id.animationsCheck);
        animationsCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS));

        wearable = (RelativeLayout) rootView.findViewById(R.id.wearable);
        wearable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wearableChange();
            }
        });

        wearableCheck = (CheckBox) rootView.findViewById(R.id.wearableCheck);
        wearableCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_WEARABLE));

        extendedButton = (RelativeLayout) rootView.findViewById(R.id.extendedButton);
        extendedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extendedChange();
            }
        });

        extendedButtonCheck = (CheckBox) rootView.findViewById(R.id.extendedButtonCheck);
        extendedButtonCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON));

        itemPreview = (RelativeLayout) rootView.findViewById(R.id.itemPreview);
        itemPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemPreviewChange();
            }
        });

        itemPreviewCheck = (CheckBox) rootView.findViewById(R.id.itemPreviewCheck);
        itemPreviewCheck.setChecked(sPrefs.loadBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW));
        return rootView;
    }

    private void itemPreviewChange() {
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (itemPreviewCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW, false);
            itemPreviewCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_ITEM_PREVIEW, true);
            itemPreviewCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED, true);
    }

    private void extendedChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (extendedButtonCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON, false);
            extendedButtonCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_EXTENDED_BUTTON, true);
            extendedButtonCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED, true);
    }

    private void useDarkStyleChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (useDarkStyleCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME, false);
            useDarkStyleCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME, true);
            useDarkStyleCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED, true);
        getActivity().recreate();
    }

    private void _24Change (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (use24TimeCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT, false);
            use24TimeCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_IS_24_TIME_FORMAT, true);
            use24TimeCheck.setChecked(true);
        }

        new UpdatesHelper(getActivity()).updateWidget();
    }

    private void wearChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wearEnableCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WEAR_NOTIFICATION, false);
            wearEnableCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WEAR_NOTIFICATION, true);
            wearEnableCheck.setChecked(true);
        }
    }

    private void wearableChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (wearableCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WEARABLE, false);
            wearableCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_WEARABLE, true);
            wearableCheck.setChecked(true);
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            Log.d(Constants.LOG_TAG, "---------------------connected");
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Log.d(Constants.LOG_TAG, "---------------------suspended");
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {
                            Log.d(Constants.LOG_TAG, "---------------------failed");
                        }
                    })
                    .build();
            mGoogleApiClient.connect();
        }
    }

    private void animationChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (animationsCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS, false);
            animationsCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_ANIMATIONS, true);
            animationsCheck.setChecked(true);
        }
        sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_UI_CHANGED, true);
    }

    private void smartFoldChange (){
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        if (smartFoldCheck.isChecked()){
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD, false);
            smartFoldCheck.setChecked(false);
        } else {
            sPrefs.saveBoolean(Constants.APP_UI_PREFERENCES_SMART_FOLD, true);
            smartFoldCheck.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String action = getActivity().getIntent().getAction();
        if(action == null || !action.equals("General attached")) {
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
        } else {
            getActivity().getIntent().setAction(null);
        }
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
        sPrefs = new SharedPrefs(getActivity().getApplicationContext());
        String loadedColor = sPrefs.loadPrefs(Constants.APP_UI_PREFERENCES_THEME);
        switch (loadedColor) {
            case "1":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_red);
                break;
            case "2":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_violet);
                break;
            case "3":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_green_light);
                break;
            case "4":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_green);
                break;
            case "5":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_blue_light);
                break;
            case "6":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
            case "7":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_yellow);
                break;
            case "8":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_orange);
                break;
            case "9":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_grey);
                break;
            case "10":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_pink);
                break;
            case "11":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_teal);
                break;
            case "12":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_brown);
                break;
            case "13":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_deep_purple);
                break;
            case "14":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_deep_orange);
                break;
            case "15":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_lime);
                break;
            case "16":
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_indigo);
                break;
            default:
                themeColorSwitcher.setBackgroundResource(R.drawable.drawable_blue);
                break;
        }
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
                getActivity().getApplicationContext().startActivity(
                        new Intent(getActivity().getApplicationContext(), ScreenOrientation.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }
}
