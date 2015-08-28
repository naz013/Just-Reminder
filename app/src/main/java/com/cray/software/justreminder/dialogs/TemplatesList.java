package com.cray.software.justreminder.dialogs;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.utils.NewTemplate;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.cray.software.justreminder.interfaces.Prefs;
import com.cray.software.justreminder.interfaces.QuickReturnListViewOnScrollListener;
import com.cray.software.justreminder.interfaces.QuickReturnViewType;
import com.cray.software.justreminder.utils.QuickReturnUtils;
import com.getbase.floatingactionbutton.AddFloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionButton;

public class TemplatesList extends AppCompatActivity {

    ListView listView;
    ColorSetter cs = new ColorSetter(TemplatesList.this);
    DataBase db = new DataBase(TemplatesList.this);
    SharedPrefs sPrefs = new SharedPrefs(TemplatesList.this);
    Toolbar toolbar;
    AddFloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(cs.getStyle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(cs.colorStatus());
        }
        setContentView(R.layout.places_activity_layout);
        setRequestedOrientation(cs.getRequestOrientation());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setTitle(getString(R.string.settings_sms_templates_title));

        findViewById(R.id.windowBackground).setBackgroundColor(cs.getBackgroundStyle());

        listView = (ListView) findViewById(R.id.listView);
        TextView empty = (TextView) findViewById(R.id.emptyList);
        empty.setText(getString(R.string.message_list_empty_text));
        listView.setEmptyView(empty);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                db.open();
                db.deleteTemplate(id);
                Toast.makeText(TemplatesList.this, getString(R.string.string_template_deleted), Toast.LENGTH_SHORT).show();
                loadTemplates();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                startActivity(new Intent(TemplatesList.this, NewTemplate.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id));
            }
        });

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
        mFab.setColorNormal(cs.colorSetter());
        mFab.setColorPressed(cs.colorChooser());
    }

    private void loadTemplates(){
        db.open();
        boolean isDark = new SharedPrefs(this).loadBoolean(Prefs.USE_DARK_THEME);
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(
                TemplatesList.this,
                isDark ? R.layout.list_item_simple_card_dark : R.layout.list_item_simple_card,
                db.queryTemplates(),
                new String[] {Constants.COLUMN_TEXT},
                new int[] { R.id.textView }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(simpleCursorAdapter);
        QuickReturnListViewOnScrollListener scrollListener = new
                QuickReturnListViewOnScrollListener.Builder(QuickReturnViewType.FOOTER)
                .footer(mFab)
                .minFooterTranslation(QuickReturnUtils.dp2px(this, 88))
                .isSnappable(true)
                .build();
        listView.setOnScrollListener(scrollListener);
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
}
