package com.cray.software.justreminder.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.activities.NewTemplate;
import com.cray.software.justreminder.adapters.TemplateRecyclerAdapter;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.databases.DataBase;
import com.cray.software.justreminder.datas.TemplateDataProvider;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.LCAMListener;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.modules.Module;
import com.cray.software.justreminder.utils.ViewUtils;

public class TemplatesFragment extends Fragment implements SimpleListener {

    private RecyclerView listView;
    private LinearLayout emptyItem;
    private TemplateDataProvider provider;
    private NavigationCallbacks mCallbacks;

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
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);

        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);

        TextView emptyText = (TextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_messages));

        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.USE_DARK_THEME)) {
            emptyImage.setImageResource(R.drawable.textsms_white);
        } else {
            emptyImage.setImageResource(R.drawable.textsms);
        }

        if (Module.isMarshmallow()) {
            emptyImage.setImageDrawable(ViewUtils.getVector(getActivity(),
                    new ColorSetter(getActivity()).colorAccent(),
                    R.drawable.ic_textsms_black_24dp1));
        }

        listView = (RecyclerView) rootView.findViewById(R.id.currentList);
        loadTemplates();
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationCallbacks) activity;
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
        if (new SharedPrefs(getActivity()).loadBoolean(Prefs.TEMPLATE_CHANGED)) {
            loadTemplates();
        }
    }

    private void loadTemplates(){
        new SharedPrefs(getActivity()).saveBoolean(Prefs.TEMPLATE_CHANGED, false);
        provider = new TemplateDataProvider(getActivity());
        reloadView();
        TemplateRecyclerAdapter adapter = new TemplateRecyclerAdapter(getActivity(), provider);
        adapter.setEventListener(this);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(adapter);
        listView.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(listView);
        }
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
        startActivity(new Intent(getActivity(), NewTemplate.class)
                .putExtra(Constants.ITEM_ID_INTENT, provider.getItem(position).getId()));
    }

    private void removeTemplate(int position){
        DataBase db = new DataBase(getActivity());
        db.open();
        db.deleteTemplate(provider.getItem(position).getId());
        db.close();
        if (mCallbacks != null) {
            mCallbacks.showSnackbar(R.string.deleted);
        }
        loadTemplates();
    }

    @Override
    public void onItemClicked(int position, View view) {
        editTemplate(position);
    }

    @Override
    public void onItemLongClicked(final int position, View view) {
        final String[] items = {getString(R.string.edit), getString(R.string.delete)};
        Dialogues.showLCAM(getActivity(), new LCAMListener() {
            @Override
            public void onAction(int item) {
                if (item == 0) {
                    editTemplate(position);
                }
                if (item == 1) {
                    removeTemplate(position);
                }
            }
        }, items);
    }
}
