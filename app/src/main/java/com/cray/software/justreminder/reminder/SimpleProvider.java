package com.cray.software.justreminder.reminder;

import android.content.Context;

import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.datas.AdapterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SimpleProvider {
    private Context mContext;
    private static SimpleProvider instance;

    private SimpleProvider(Context context) {
        this.mContext = context;
    }

    public static SimpleProvider getInstance(Context context) {
        if (instance == null) {
            instance = new SimpleProvider(context);
        }
        return instance;
    }

    public List<AdapterItem> getActive() {
        List<AdapterItem> list = new ArrayList<>();
        List<ReminderItem> items = ReminderHelper.getInstance(mContext).getRemindersActive();
        for (ReminderItem item : items) {
            int viewType = 0;
            if (item.getType().matches(Constants.TYPE_SHOPPING_LIST)) {
                viewType = 1;
            }
            list.add(new AdapterItem(viewType, item));
        }
        return list;
    }

    public List<AdapterItem> getTrashed() {
        List<AdapterItem> list = new ArrayList<>();
        List<ReminderItem> items = ReminderHelper.getInstance(mContext).getRemindersArchived();
        for (ReminderItem item : items) {
            int viewType = 0;
            if (item.getType().matches(Constants.TYPE_SHOPPING_LIST)) {
                viewType = 1;
            }
            list.add(new AdapterItem(viewType, item));
        }
        return list;
    }
}
