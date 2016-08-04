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

package com.cray.software.justreminder.templates;

import android.app.Activity;
import android.content.Context;
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

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.ScreenManager;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.helpers.Dialogues;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;
import com.cray.software.justreminder.interfaces.SimpleListener;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.List;

public class TemplatesFragment extends Fragment implements SimpleListener {

    private RecyclerView listView;
    private LinearLayout emptyItem;
    private List<TemplateItem> mDataList;
    private NavigationCallbacks mCallbacks;
    private Activity mContext;

    public static TemplatesFragment newInstance() {
        return new TemplatesFragment();
    }

    public TemplatesFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_screen_manager, container, false);
        rootView.findViewById(R.id.backgroundFragment).setBackgroundColor(new ColorSetter(mContext).getBackgroundStyle());
        emptyItem = (LinearLayout) rootView.findViewById(R.id.emptyItem);
        emptyItem.setVisibility(View.VISIBLE);
        RoboTextView emptyText = (RoboTextView) rootView.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_messages));
        ImageView emptyImage = (ImageView) rootView.findViewById(R.id.emptyImage);
        if (new ColorSetter(mContext).isDark()) {
            emptyImage.setImageResource(R.drawable.ic_textsms_white_vector);
        } else {
            emptyImage.setImageResource(R.drawable.ic_textsms_black_vector);
        }

        listView = (RecyclerView) rootView.findViewById(R.id.currentList);
        loadTemplates();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = (Activity) context;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) context;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) context).onSectionAttached(ScreenManager.FRAGMENT_TEMPLATES);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallbacks == null) {
            try {
                mCallbacks = (NavigationCallbacks) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
            }
        }
        ((ScreenManager) activity).onSectionAttached(ScreenManager.FRAGMENT_TEMPLATES);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPrefs.getInstance(mContext).getBoolean(Prefs.TEMPLATE_CHANGED)) {
            loadTemplates();
        }
    }

    private void loadTemplates(){
        SharedPrefs.getInstance(mContext).putBoolean(Prefs.TEMPLATE_CHANGED, false);
        mDataList = TemplateHelper.getInstance(mContext).getAll();
        reloadView();
        TemplatesRecyclerAdapter adapter = new TemplatesRecyclerAdapter(mContext, mDataList);
        adapter.setEventListener(this);
        listView.setLayoutManager(new LinearLayoutManager(mContext));
        listView.setAdapter(adapter);
        listView.setItemAnimator(new DefaultItemAnimator());
        if (mCallbacks != null) {
            mCallbacks.onListChanged(listView);
        }
    }

    private void reloadView() {
        int size = mDataList.size();
        if (size > 0){
            listView.setVisibility(View.VISIBLE);
            emptyItem.setVisibility(View.GONE);
        } else {
            listView.setVisibility(View.GONE);
            emptyItem.setVisibility(View.VISIBLE);
        }
    }

    private void editTemplate(int position){
        startActivity(new Intent(mContext, TemplateManager.class)
                .putExtra(Constants.ITEM_ID_INTENT, mDataList.get(position).getId()));
    }

    private void removeTemplate(int position){
        TemplateHelper.getInstance(mContext).deleteTemplate(mDataList.get(position).getId());
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
        Dialogues.showLCAM(mContext, item -> {
            if (item == 0) {
                editTemplate(position);
            }
            if (item == 1) {
                removeTemplate(position);
            }
        }, items);
    }
}
