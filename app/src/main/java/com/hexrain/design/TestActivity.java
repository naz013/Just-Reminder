package com.hexrain.design;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.datas.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

public class TestActivity extends AppCompatActivity {

    private ShoppingListDataProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(new ColorSetter(this).getStyle());
        setContentView(R.layout.activity_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.windowBackground).setBackgroundColor(new ColorSetter(this).getBackgroundStyle());

        RecyclerView testList = (RecyclerView) findViewById(R.id.testList);
        RecyclerViewDragDropManager mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        provider = new ShoppingListDataProvider(TestActivity.this, true);
        final TaskListRecyclerAdapter myItemAdapter = new TaskListRecyclerAdapter(TestActivity.this, provider);
        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(myItemAdapter);
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        testList.setLayoutManager(new LinearLayoutManager(TestActivity.this));
        testList.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        testList.setItemAnimator(animator);
        mRecyclerViewDragDropManager.attachRecyclerView(testList);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(Constants.LOG_TAG, "List size " + provider.getCount());
                StringBuilder sb = new StringBuilder();
                for (ShoppingList item : provider.getData()){
                    sb.append(item.getTitle());
                    sb.append(" uuid ").append(item.getUuId());
                    sb.append("\n");
                }
                Log.d(Constants.LOG_TAG, sb.toString());
            }
        });
    }

}
