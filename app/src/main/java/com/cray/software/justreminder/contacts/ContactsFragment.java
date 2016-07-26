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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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

public class ContactsFragment extends Fragment implements LoadListener {

    private Context mContext;
    private NumberCallback mCallback;

    private ContactsRecyclerAdapter mAdapter;
    private List<ContactData> mData;
    private String name = "";

    private LinearLayout mEmptyItem;
    private RoboEditText searchField;
    private RecyclerView mRecyclerView;
    private CircularProgressBar mProgressView;

    private RecyclerClickListener mClickListener = new RecyclerClickListener() {
        @Override
        public void onItemClick(int position) {
            if (position != -1) {
                name = mAdapter.getItem(position).getName();
                selectNumber();
            }
        }
    };

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
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
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initSearchView(view);
        initRecyclerView(view);
        initEmptyLayout(view);
        mProgressView = (CircularProgressBar) view.findViewById(R.id.progressView);
        new ContactsAsync(mContext, this).execute();
        return view;
    }

    private void initEmptyLayout(View view) {
        RoboTextView emptyText = (RoboTextView) view.findViewById(R.id.emptyText);
        emptyText.setText(getString(R.string.no_contacts_found));
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
        List<ContactData> res = filter(mData, q);
        mAdapter.animateTo(res);
        mRecyclerView.scrollToPosition(0);
        refreshView(res.size());
    }

    private List<ContactData> filter(List<ContactData> mData, String q) {
        q = q.toLowerCase();
        List<ContactData> filteredModelList = new ArrayList<>();
        if (mData == null) mData = new ArrayList<>();
        if (q.matches("")) {
            filteredModelList = new ArrayList<>(mData);
        } else {
            filteredModelList.addAll(getFiltered(mData, q));
        }
        return filteredModelList;
    }

    private List<ContactData> getFiltered(List<ContactData> models, String query) {
        List<ContactData> list = new ArrayList<>();
        for (ContactData model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(query)) {
                list.add(model);
            }
        }
        return list;
    }

    private void selectNumber() {
        Cursor c = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "=?",
                new String[]{name}, null);

        int phoneIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        int phoneType = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);

        if (c.getCount() > 1) {
            final CharSequence[] numbers = new CharSequence[c.getCount()];
            int i = 0;
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                            getResources(), c.getInt(phoneType), "");
                    String number = type + ": " + c.getString(phoneIdx);
                    numbers[i++] = number;
                    c.moveToNext();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(numbers, (dialog, which) -> {
                    dialog.dismiss();
                    String number = (String) numbers[which];
                    int index = number.indexOf(":");
                    number = number.substring(index + 2);
                    if (mCallback != null) {
                        mCallback.onContactSelected(number, name);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

            }
        } else if (c.getCount() == 1) {
            if (c.moveToFirst()) {
                String number = c.getString(phoneIdx);
                if (mCallback != null) {
                    mCallback.onContactSelected(number, name);
                }
            }
        } else if (c.getCount() == 0) {
            if (mCallback != null) {
                mCallback.onContactSelected(null, name);
            }
        }
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
    public void onLoaded(List<ContactData> list) {
        this.mData = list;
        mProgressView.setVisibility(View.GONE);
        mAdapter = new ContactsRecyclerAdapter(mContext, mData, mClickListener);
        mRecyclerView.setAdapter(mAdapter);
        refreshView(mAdapter.getItemCount());
    }
}
