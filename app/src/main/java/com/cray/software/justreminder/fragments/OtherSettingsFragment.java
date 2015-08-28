package com.cray.software.justreminder.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cray.software.justreminder.Help;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ThanksDialog;
import com.cray.software.justreminder.dialogs.AboutDialog;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.dialogs.RateDialog;
import com.cray.software.justreminder.modules.Module;

public class OtherSettingsFragment extends Fragment {

    ActionBar ab;
    TextView about, rateApp, thanks, help, menuShare, menuFeedback, changes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.other_settings_layout, container, false);

        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.other_settings);
        }

        about = (TextView) rootView.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                AboutDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        changes = (TextView) rootView.findViewById(R.id.changes);
        changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                ChangeDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        rateApp = (TextView) rootView.findViewById(R.id.rateApp);
        rateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                RateDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        thanks = (TextView) rootView.findViewById(R.id.thanks);
        thanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                ThanksDialog.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        help = (TextView) rootView.findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getApplicationContext()
                        .startActivity(new Intent(getActivity().getApplicationContext(),
                                Help.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

        menuFeedback = (TextView) rootView.findViewById(R.id.menuFeedback);
        menuFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent emailIntent = new Intent( Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "feedback.cray@gmail.com" });
                if (Module.isPro()){
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder PRO");
                } else {
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Reminder");
                }
                getActivity().startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        });

        menuShare = (TextView) rootView.findViewById(R.id.menuShare);
        menuShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.cray.software.justreminderpro");
                getActivity().startActivity(Intent.createChooser(shareIntent, "Share..."));
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }
}
