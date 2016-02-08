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

package com.cray.software.justreminder.creator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.TaskListRecyclerAdapter;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.datas.models.ShoppingList;
import com.cray.software.justreminder.helpers.ColorSetter;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.utils.ViewUtils;
import com.cray.software.justreminder.views.DateTimeView;

import java.util.ArrayList;

public class ShoppingFragment extends BaseFragment {

    private DateTimeView.OnSelectListener mCallbacks;

    /**
     * Shopping list reminder type variables.
     */
    private EditText shopEdit;
    private TextView shoppingNoTime;
    private RelativeLayout shoppingTimeContainer;
    private DateTimeView dateViewShopping;
    private RecyclerView todoList;

    private TaskListRecyclerAdapter shoppingAdapter;
    private ShoppingListDataProvider shoppingLists;

    private boolean isShoppingReminder;

    public boolean isShoppingReminder() {
        return isShoppingReminder;
    }

    public int getCount() {
        if (shoppingLists != null) return shoppingLists.getCount();
        else return 0;
    }

    public ArrayList<ShoppingList> getData() {
        if (shoppingLists != null) return shoppingLists.getData();
        else return null;
    }

    public static ShoppingFragment newInstance(JModel item, boolean isDark, boolean hasCalendar,
                                                  boolean hasStock, boolean hasTasks) {
        ShoppingFragment fragment = new ShoppingFragment();
        Bundle args = new Bundle();
        args.putBoolean(THEME, isDark);
        args.putBoolean(CALENDAR, hasCalendar);
        args.putBoolean(STOCK, hasStock);
        args.putBoolean(TASKS, hasTasks);
        fragment.setItem(item);
        fragment.setArguments(args);
        return fragment;
    }

    public ShoppingFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            hasCalendar = args.getBoolean(CALENDAR);
            hasStock = args.getBoolean(STOCK);
            hasTasks = args.getBoolean(TASKS);
            isDark = args.getBoolean(THEME);
        }
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
        View view = inflater.inflate(R.layout.reminder_shopping_list_layout, container, false);

        todoList = (RecyclerView) view.findViewById(R.id.todoList);
        todoList.setLayoutManager(new LinearLayoutManager(getActivity()));
        CardView cardContainer = (CardView) view.findViewById(R.id.cardContainer);
        cardContainer.setCardBackgroundColor(new ColorSetter(getActivity()).getCardStyle());

        shoppingTimeContainer = (RelativeLayout) view.findViewById(R.id.shoppingTimeContainer);

        dateViewShopping = (DateTimeView) view.findViewById(R.id.dateViewShopping);
        dateViewShopping.setListener(mCallbacks);
        eventTime = System.currentTimeMillis();
        dateViewShopping.setDateTime(updateCalendar(eventTime, false));

        ImageView shopTimeIcon = (ImageView) view.findViewById(R.id.shopTimeIcon);
        shopTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingTimeContainer.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingTimeContainer);
                }
                ViewUtils.show(shoppingNoTime);
                myYear = 0;
                myMonth = 0;
                myDay = 0;
                myHour = 0;
                myMinute = 0;
                isShoppingReminder = false;
            }
        });
        if (isDark)
            shopTimeIcon.setImageResource(R.drawable.ic_alarm_white_24dp);
        else
            shopTimeIcon.setImageResource(R.drawable.ic_alarm_black_24dp);

        shoppingNoTime  = (TextView) view.findViewById(R.id.shoppingNoTime);
        shoppingNoTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shoppingNoTime.getVisibility() == View.VISIBLE) {
                    ViewUtils.hide(shoppingNoTime);
                }
                ViewUtils.show(shoppingTimeContainer);
                dateViewShopping.setDateTime(updateCalendar(System.currentTimeMillis(), false));
                isShoppingReminder = true;
            }
        });

        shopEdit = (EditText) view.findViewById(R.id.shopEdit);
        shopEdit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                    String task = shopEdit.getText().toString().trim();
                    if (task.matches("")) {
                        shopEdit.setError(getString(R.string.must_be_not_empty));
                        return false;
                    } else {
                        shoppingLists.addItem(new ShoppingList(task));
                        shoppingAdapter.notifyDataSetChanged();
                        shopEdit.setText("");
                        return true;
                    }
                } else return false;
            }
        });
        ImageButton addButton = (ImageButton) view.findViewById(R.id.addButton);
        if (isDark) addButton.setImageResource(R.drawable.ic_add_white_24dp);
        else addButton.setImageResource(R.drawable.ic_add_black_24dp);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String task = shopEdit.getText().toString().trim();
                if (task.matches("")) {
                    shopEdit.setError(getString(R.string.must_be_not_empty));
                    return;
                }

                shoppingLists.addItem(new ShoppingList(task));
                shoppingAdapter.notifyDataSetChanged();
                shopEdit.setText("");
            }
        });

        shoppingLists = new ShoppingListDataProvider();
        loadShoppings();

        if (item != null) {
            shoppingLists.clear();
            shoppingLists = new ShoppingListDataProvider(item.getShoppings(), true);
            loadShoppings();
            eventTime = item.getStartTime();

            if (eventTime > 0) {
                dateViewShopping.setDateTime(updateCalendar(eventTime, true));
                if (shoppingNoTime.getVisibility() == View.VISIBLE)
                    ViewUtils.hide(shoppingNoTime);

                ViewUtils.show(shoppingTimeContainer);
                isShoppingReminder = true;
            } else {
                if (shoppingTimeContainer.getVisibility() == View.VISIBLE)
                    ViewUtils.hide(shoppingTimeContainer);

                ViewUtils.show(shoppingNoTime);
                isShoppingReminder = false;
            }
        }
        return view;
    }

    private void loadShoppings() {
        shoppingAdapter = new TaskListRecyclerAdapter(getActivity(),
                shoppingLists, new TaskListRecyclerAdapter.ActionListener() {
            @Override
            public void onItemCheck(int position, boolean isChecked) {
                ShoppingList item = shoppingLists.getItem(position);
                if (item.isChecked() == 1) item.setIsChecked(0);
                else item.setIsChecked(1);
                loadShoppings();
            }

            @Override
            public void onItemDelete(int position) {
                shoppingLists.removeItem(position);
                loadShoppings();
            }

            @Override
            public void onItemChange(int position) {
                ShoppingList item = shoppingLists.getItem(position);
                if (item.getStatus() == 1) item.setStatus(0);
                else item.setStatus(1);
                loadShoppings();
            }
        });
        todoList.setAdapter(shoppingAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (DateTimeView.OnSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement listeners.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}
