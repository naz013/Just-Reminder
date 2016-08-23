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

package com.cray.software.justreminder.tests;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.StartActivity;
import com.cray.software.justreminder.birthdays.AddBirthdayActivity;
import com.cray.software.justreminder.places.AddPlaceActivity;
import com.cray.software.justreminder.calls.FollowReminderActivity;
import com.cray.software.justreminder.calls.MissedCallActivity;
import com.cray.software.justreminder.reminder.AddReminderActivity;
import com.cray.software.justreminder.templates.QuickSMSActivity;
import com.cray.software.justreminder.birthdays.ShowBirthdayActivity;
import com.cray.software.justreminder.contacts.ContactsActivity;
import com.cray.software.justreminder.feedback.SendReportActivity;
import com.cray.software.justreminder.file_explorer.FileExploreActivity;
import com.cray.software.justreminder.interfaces.SimpleListener;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity implements SimpleListener {

    ArrayList<ActionItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.actionList);

        ActionsAdapter adapter = new ActionsAdapter(getList());
        adapter.setEventListener(this);

        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
    }

    private ArrayList<ActionItem> getList() {
        list = new ArrayList<>();
        list.add(new ActionItem("Run application", StartActivity.class));
        list.add(new ActionItem("Contact picker", ContactsActivity.class));
        list.add(new ActionItem("File picker", FileExploreActivity.class));
        list.add(new ActionItem("Feedback screen", SendReportActivity.class));
        list.add(new ActionItem("After call screen", FollowReminderActivity.class));
        list.add(new ActionItem("Quick message", QuickSMSActivity.class));
        list.add(new ActionItem("Quick reminder", AddReminderActivity.class));
        list.add(new ActionItem("Add place", AddPlaceActivity.class));
        list.add(new ActionItem("Add birthday", AddBirthdayActivity.class));
        list.add(new ActionItem("Missed call screen", MissedCallActivity.class));
        list.add(new ActionItem("Birthday dialog", ShowBirthdayActivity.class));
        return list;
    }

    @Override
    public void onItemClicked(int position, View view) {
        startActivity(new Intent(this, list.get(position).getActionClass()));
    }

    @Override
    public void onItemLongClicked(int position, View view) {

    }
}
