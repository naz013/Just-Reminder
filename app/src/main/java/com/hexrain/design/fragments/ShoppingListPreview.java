package com.hexrain.design.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ReminderManager;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.datas.ReminderModel;
import com.cray.software.justreminder.datas.ShoppingList;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.reminder.Reminder;
import com.cray.software.justreminder.reminder.ReminderDataProvider;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.cray.software.justreminder.utils.ViewUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class ShoppingListPreview extends AppCompatActivity {

    private TextView shopTitle;
    private RecyclerView todoList;
    private Toolbar toolbar;
    private FloatingActionButton mFab;

    private ShoppingListDataProvider provider;
    private ColorSetter cSetter;

    private long id;
    private boolean isHIdden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cSetter = new ColorSetter(this);
        setTheme(cSetter.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cSetter.colorPrimaryDark());
        }
        setContentView(R.layout.activity_shopping_preview);
        setRequestedOrientation(cSetter.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle("");

        id = getIntent().getLongExtra(Constants.EDIT_ID, 0);

        shopTitle = (TextView) findViewById(R.id.shopTitle);

        CheckBox showHidden = (CheckBox) findViewById(R.id.showHidden);
        showHidden.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isHIdden = isChecked;
                loadData();
            }
        });

        todoList = (RecyclerView) findViewById(R.id.todoList);
        todoList.setLayoutManager(new LinearLayoutManager(this));

        mFab = new FloatingActionButton(this);
        mFab.setSize(FloatingActionButton.SIZE_MINI);
        mFab.setIcon(R.drawable.ic_create_white_24dp);
        mFab.setColorNormal(cSetter.colorAccent());
        mFab.setColorPressed(cSetter.colorAccent());
        mFab.setVisibility(View.GONE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id != 0) {
                    Intent intentId = new Intent(ShoppingListPreview.this, ReminderManager.class);
                    intentId.putExtra(Constants.EDIT_ID, id);
                    startActivity(intentId);
                }
            }
        });

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.windowBackground);
        wrapper.setBackgroundColor(cSetter.getBackgroundStyle());
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams paramsR = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        paramsR.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        paramsR.addRule(RelativeLayout.BELOW, R.id.toolbar);
        paramsR.setMargins(0, -(QuickReturnUtils.dp2px(this, 28)), 0, 0);

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               ViewUtils.zoom(mFab, 350);
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        loadUi();
    }

    private void loadUi() {
        ReminderModel item = ReminderDataProvider.getItem(this, id);
        if (item != null) {
            shopTitle.setText(item.getTitle());
            int catColor = item.getCatColor();
            toolbar.setBackgroundColor(ViewUtils.getColor(this, cSetter.getCategoryColor(catColor)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(cSetter.getNoteDarkColor(catColor));
            }
            mFab.setColorNormal(cSetter.colorAccent(catColor));
            mFab.setColorPressed(cSetter.colorAccent(catColor));
        }
    }

    private void loadData() {
        provider = new ShoppingListDataProvider(this, id, isHIdden ? ShoppingList.DELETED : ShoppingList.ACTIVE);
        TaskListRecyclerAdapter shoppingAdapter = new TaskListRecyclerAdapter(this, provider, new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                ShoppingList.switchItem(ShoppingListPreview.this, provider.getItem(position).getId(), isChecked);
                loadData();
            }

            @Override
            public void onItemDelete(int position) {
                ShoppingList.hideItem(ShoppingListPreview.this, provider.getItem(position).getId());
                loadData();
            }

            @Override
            public void onItemChange(int position) {
                ShoppingList.showItem(ShoppingListPreview.this, provider.getItem(position).getId());
                loadData();
            }
        });
        todoList.setAdapter(shoppingAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int ids = item.getItemId();
        if (ids == R.id.action_delete) {
            Reminder.moveToTrash(id, ShoppingListPreview.this, null);
            ViewUtils.zoomOut(mFab, 350);
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else finish();
                }
            }, 350);
            return true;
        }
        if (ids == android.R.id.home){
            ViewUtils.zoomOut(mFab, 350);
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAfterTransition();
                    } else finish();
                }
            }, 350);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        ViewUtils.zoomOut(mFab, 350);
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else finish();
            }
        }, 350);
    }
}
