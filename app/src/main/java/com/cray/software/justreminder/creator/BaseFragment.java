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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.datas.ShoppingListDataProvider;
import com.cray.software.justreminder.fragments.helpers.MapFragment;
import com.cray.software.justreminder.fragments.helpers.PlacesMap;
import com.cray.software.justreminder.helpers.Permissions;
import com.cray.software.justreminder.json.JModel;
import com.cray.software.justreminder.utils.SuperUtil;

import java.util.Calendar;
import java.util.Date;

public class BaseFragment extends Fragment {

    protected final static String THEME = "theme";
    protected final static String STOCK = "stock";
    protected final static String CALENDAR = "calendar";
    protected final static String TASKS = "tasks";

    protected PlacesMap placesMap;
    protected MapFragment mapFragment;

    protected JModel item;
    protected boolean isCalendar;
    protected boolean isTasks;
    protected String number;
    protected String message;
    protected String filePath;
    protected String eventTask;
    protected ShoppingListDataProvider shoppingListDataProvider;

    protected boolean hasCalendar;
    protected boolean hasStock;
    protected boolean isDark;
    protected boolean hasTasks;

    protected int myHour = 0;
    protected int myMinute = 0;
    protected int myYear = 0;
    protected int myMonth = 0;
    protected int myDay = 1;
    protected long eventTime;

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Select contact button click listener.
     */
    public View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Permissions.checkPermission(getActivity(), Permissions.READ_CONTACTS)) {
                SuperUtil.selectContact(getActivity(), Constants.REQUEST_CODE_CONTACTS);
            } else {
                Permissions.requestPermission(getActivity(), 107, Permissions.READ_CONTACTS);
            }
        }
    };

    public void setEventTask(String eventTask) {
        this.eventTask = eventTask;
    }

    public String getNumber() {
        return number;
    }

    public JModel getItem() {
        return item;
    }

    public ShoppingListDataProvider getShoppingListDataProvider() {
        return shoppingListDataProvider;
    }

    public String getMessage() {
        return message;
    }

    public boolean getCalendar() {
        return isCalendar;
    }

    public boolean getTasks() {
        return isTasks;
    }

    public BaseFragment() {
    }

    public void setItem(JModel item) {
        this.item = item;
    }

    protected long updateCalendar(long millis, boolean deny) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        if (myYear > 0 && !deny) cal.set(myYear, myMonth, myDay, myHour, myMinute);
        else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }
        return cal.getTimeInMillis();
    }

    protected Date updateTime(long millis, boolean deny) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        if (myYear > 0 && !deny) cal.set(myYear, myMonth, myDay, myHour, myMinute);
        else {
            myYear = cal.get(Calendar.YEAR);
            myMonth = cal.get(Calendar.MONTH);
            myDay = cal.get(Calendar.DAY_OF_MONTH);
            myHour = cal.get(Calendar.HOUR_OF_DAY);
            myMinute = cal.get(Calendar.MINUTE);
        }
        return cal.getTime();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(false);
    }

    public boolean onBackPressed() {
        if (placesMap != null) {
            return !placesMap.onBackPressed();
        }
        return mapFragment != null && !mapFragment.onBackPressed();
    }
}
