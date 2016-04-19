package com.cray.software.justreminder.tests;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.activities.AddBirthday;
import com.cray.software.justreminder.activities.AddPlace;
import com.cray.software.justreminder.calls.FollowReminder;
import com.cray.software.justreminder.calls.MissedCallDialog;
import com.cray.software.justreminder.activities.QuickAddReminder;
import com.cray.software.justreminder.activities.QuickSMS;
import com.cray.software.justreminder.activities.ShowBirthday;
import com.cray.software.justreminder.contacts.ContactsActivity;
import com.cray.software.justreminder.feedback.SendReportActivity;
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
        list.add(new ActionItem("Run application", ScreenManager.class));
        list.add(new ActionItem("Contact picker", ContactsActivity.class));
        list.add(new ActionItem("Feedback screen", SendReportActivity.class));
        list.add(new ActionItem("After call screen", FollowReminder.class));
        list.add(new ActionItem("Quick message", QuickSMS.class));
        list.add(new ActionItem("Quick reminder", QuickAddReminder.class));
        list.add(new ActionItem("Add place", AddPlace.class));
        list.add(new ActionItem("Add birthday", AddBirthday.class));
        list.add(new ActionItem("Missed call screen", MissedCallDialog.class));
        list.add(new ActionItem("Birthday dialog", ShowBirthday.class));
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
