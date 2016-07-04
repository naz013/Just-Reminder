/*
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

package com.cray.software.justreminder.contacts;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.roboto_views.RoboEditText;
import com.cray.software.justreminder.roboto_views.RoboTextView;

import java.util.ArrayList;
import java.util.List;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class CallsFragment extends Fragment implements CallsLogListener {

    private Context mContext;
    private NumberCallback mCallback;

    private CallsRecyclerAdapter mAdapter;
    private List<CallsData> mData;

    private LinearLayout mEmptyItem;
    private RoboEditText searchField;
    private RecyclerView mRecyclerView;
    private CircularProgressBar mProgressView;

    private RecyclerClickListener mClickListener = new RecyclerClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position != -1) {
                String number = mAdapter.getItem(position).getNumber();
                String name = mAdapter.getItem(position).getName();
                if (mCallback != null) {
                    mCallback.onContactSelected(number, name);
                }
            }
        }
    };

    public CallsFragment() {
        // Required empty public constructor
    }

    public static CallsFragment newInstance() {
        return new CallsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mContext == null) {
            mContext = context;
        }
        if (mCallback == null) {
            try {
                mCallback = (NumberCallback) context;
            } catch (ClassCastException e) {
                throw new ClassCastException();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mContext == null) {
            mContext = activity;
        }
        if (mCallback == null) {
            try {
                mCallback = (NumberCallback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calls, container, false);
        initSearchView(view);
        initRecyclerView(view);
        initEmptyLayout(view);
        mProgressView = (CircularProgressBar) view.findViewById(R.id.progressView);
        new CallsAsync(mContext, this).execute();
        return view;
    }

    private void initEmptyLayout(View view) {
        RoboTextView emptyText = (RoboTextView) view.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_calls_found));
        ImageView mEmptyIcon = (ImageView) view.findViewById(R.id.emptyImage);
        if (new ColorSetter(mContext).isDark()) {
            mEmptyIcon.setImageResource(R.drawable.account_off_white);
        } else {
            mEmptyIcon.setImageResource(R.drawable.account_off);
        }
        mEmptyItem = (LinearLayout) view.findViewById(R.id.emptyItem);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.contactsList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
    }

    private void initSearchView(View view) {
        searchField = (RoboEditText) view.findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void filterContacts(String q) {
        List<CallsData> res = filter(mData, q);
        mAdapter.animateTo(res);
        mRecyclerView.scrollToPosition(0);
        refreshView(res.size());
    }

    private List<CallsData> filter(List<CallsData> mData, String q) {
        q = q.toLowerCase();
        List<CallsData> filteredModelList = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<CallsData> getFiltered(List<CallsData> models, String query) {
        List<CallsData> list = new ArrayList<>();
        for (CallsData model : models) {
            final String text = model.getNumberName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    private void refreshView(int count) {
        if (count > 0) {
            mEmptyItem.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mEmptyItem.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
    }

    @Override
    public void onLoaded(List<CallsData> list) {
        this.mData = list;
        mProgressView.setVisibility(View.GONE);
        mAdapter = new CallsRecyclerAdapter(mContext, mData, mClickListener);
        mRecyclerView.setAdapter(mAdapter);
        refreshView(mAdapter.getItemCount());
    }
}
