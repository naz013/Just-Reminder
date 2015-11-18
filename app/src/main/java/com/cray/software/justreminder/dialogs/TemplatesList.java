package com.cray.software.justreminder.dialogs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TemplateRecyclerAdapter;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.TemplateDataProvider;
import com.cray.software.justreminder.dialogs.utils.NewTemplate;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.QuickReturnRecyclerViewOnScrollListener;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class TemplatesList extends AppCompatActivity implements SimpleListener {

    private RecyclerView listView;
    private LinearLayout emptyItem;
    private ColorSetter cs = new ColorSetter(TemplatesList.this);
    private AddFloatingActionButton mFab;

    private TemplateDataProvider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorPrimaryDark());
        }
        setContentView(R.layout.places_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.settings_sms_templates_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        emptyItem = (LinearLayout) findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.message_list_empty_text));

        ImageView emptyImage = (ImageView) findViewById(R.id.emptyImage);
        if (new SharedPrefs(this).loadBoolean(Prefs.USE_DARK_THEME))
            emptyImage.setImageResource(R.drawable.textsms_white);
        else
            emptyImage.setImageResource(R.drawable.textsms);

        listView = (RecyclerView) findViewById(R.id.currentList);

        mFab = new AddFloatingActionButton(TemplatesList.this);
        mFab.setSize(FloatingActionButton.SIZE_NORMAL);

        RelativeLayout wrapper = (RelativeLayout) findViewById(R.id.wrapper);
        wrapper.addView(mFab);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFab.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TemplatesList.this, NewTemplate.class));
            }
        });
        mFab.setColorNormal(cs.colorAccent());
        mFab.setColorPressed(cs.colorAccent());
    }

    private void loadTemplates(){
        provider = new TemplateDataProvider(this);
        reloadView();
        TemplateRecyclerAdapter adapter = new TemplateRecyclerAdapter(this, provider);
        adapter.setEventListener(this);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setAdapter(adapter);  // requires *wrapped* adapter
        listView.setItemAnimator(new DefaultItemAnimator());
        QuickReturnRecyclerViewOnScrollListener scrollListener = new
                QuickReturnRecyclerViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();
        listView.setOnScrollListener(scrollListener);
    }

    private void reloadView() {
        int size = provider.getCount();
        if (size > 0){
            listView.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void editTemplate(int position){
        startActivity(new Intent(this, NewTemplate.class)
                .putExtra(Constants.ITEM_ID_INTENT, provider.getItem(position).getId()));
    }

    private void removeTemplate(int position){
        DataBase db = new DataBase(this);
        db.open();
        db.deleteTemplate(provider.getItem(position).getId());
        db.close();
        Messages.toast(this, getString(R.string.string_template_deleted));
        loadTemplates();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTemplates();
    }

    @Override
    public void onItemClicked(int position, View view) {
        editTemplate(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final CharSequence[] items = {getString(R.string.edit), getString(R.string.delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                dialog.dismiss();
                if (item == 0) {
                    editTemplate(position);
                }
                if (item == 1) {
                    removeTemplate(position);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
