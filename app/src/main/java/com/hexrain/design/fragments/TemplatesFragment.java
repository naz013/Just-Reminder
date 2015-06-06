package com.hexrain.design.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.dialogs.utils.NewTemplate;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.Constants;
import com.hexrain.design.NavigationDrawerFragment;
import com.hexrain.design.ScreenManager;

public class TemplatesFragment extends Fragment {

    ColorSetter cSetter;
    DataBase db;
    SharedPrefs sPrefs;
    ListView listView;

    private NavigationDrawerFragment.NavigationDrawerCallbacks mCallbacks;

    public static TemplatesFragment newInstance() {
        return new TemplatesFragment();
    }

    public TemplatesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_list_layout, container, false);

        cSetter = new ColorSetter(getActivity());
        sPrefs = new SharedPrefs(getActivity());

        listView = (ListView) rootView.findViewById(R.id.listView);
        TextView empty = (TextView) rootView.findViewById(R.id.emptyList);
        empty.setText(getString(R.string.message_list_empty_text));
        listView.setEmptyView(empty);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                db.open();
                db.deleteTemplate(id);
                Toast.makeText(getActivity(), getString(R.string.string_template_deleted), Toast.LENGTH_SHORT).show();
                loadTemplates();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                startActivity(new Intent(getActivity(), NewTemplate.class)
                        .putExtra(Constants.ITEM_ID_INTENT, id));
            }
        });

        loadTemplates();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerFragment.NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
        ((ScreenManager)activity).onSectionAttached(ScreenManager.FRAGMENT_TEMPLATES);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadTemplates(){
        db = new DataBase(getActivity());
        db.open();
        boolean isDark = new SharedPrefs(getActivity()).loadBoolean(Constants.APP_UI_PREFERENCES_USE_DARK_THEME);
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                isDark ? R.layout.list_item_simple_card_dark : R.layout.list_item_simple_card,
                db.queryTemplates(),
                new String[] {Constants.COLUMN_TEXT},
                new int[] { R.id.textView }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(simpleCursorAdapter);
        if (mCallbacks != null) mCallbacks.onListChange(listView);
    }
}
