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

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.activities.HelpActivity;
import com.cray.software.justreminder.activities.ThanksActivity;
import com.cray.software.justreminder.dialogs.AboutDialog;
import com.cray.software.justreminder.dialogs.ChangeDialog;
import com.cray.software.justreminder.dialogs.PermissionsList;
import com.cray.software.justreminder.feedback.SendReportActivity;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.Module;

public class OtherSettingsFragment extends Fragment {

    private ActionBar ab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.settings_other, container, false);
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.other);
        }

        TextView about = (TextView) rootView.findViewById(R.id.about);
        about.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        AboutDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView changes = (TextView) rootView.findViewById(R.id.changes);
        changes.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        ChangeDialog.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView rateApp = (TextView) rootView.findViewById(R.id.rateApp);
        rateApp.setOnClickListener(v -> Dialogues.rateDialog(getActivity()));

        TextView thanks = (TextView) rootView.findViewById(R.id.thanks);
        thanks.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        ThanksActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView help = (TextView) rootView.findViewById(R.id.help);
        help.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        HelpActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView menuFeedback = (TextView) rootView.findViewById(R.id.menuFeedback);
        menuFeedback.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(),
                        SendReportActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView menuShare = (TextView) rootView.findViewById(R.id.menuShare);
        menuShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.cray.software.justreminderpro");
            getActivity().startActivity(Intent.createChooser(shareIntent, "Share..."));
        });

        LinearLayout permissionBlock = (LinearLayout) rootView.findViewById(R.id.permissionBlock);
        if (Module.isMarshmallow()) permissionBlock.setVisibility(View.VISIBLE);
        else permissionBlock.setVisibility(View.GONE);

        TextView permissionExplain = (TextView) rootView.findViewById(R.id.permissionExplain);
        permissionExplain.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(), ThanksActivity.class)
                        .putExtra("int", 1).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        TextView permissionEnable = (TextView) rootView.findViewById(R.id.permissionEnable);
        permissionEnable.setOnClickListener(v -> getActivity().getApplicationContext()
                .startActivity(new Intent(getActivity().getApplicationContext(), PermissionsList.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null){
            ab.setTitle(R.string.action_settings);
        }
    }
}
