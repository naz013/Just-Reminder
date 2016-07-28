/**
 * Copyright 2015 Nazar Suhovich
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
package com.cray.software.justreminder.reminder;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.reminder.json.JExport;

public class Type {

    private Context mContext;
    private Fragment fragment;
    private String type;

    public Type(Context context){
        this.mContext = context;
        this.type = "";
    }

    /**
     * Inflate layout file for reminder.
     * @param fragment fragment.
     */
    public void inflateView(Fragment fragment){
        this.fragment = fragment;
    }

    /**
     * Get reminder layout resource identifier.
     * @return reminder layout id
     */
    public Fragment getView(){
        return fragment;
    }

    /**
     * Set reminder type.
     * @param type reminder type.
     */
    public void setType(String type){
        this.type = type;
    }

    /**
     * Get reminder type.
     * @return reminder type
     */
    public String getType(){
        return type;
    }

    /**
     * Save new reminder to database.
     * @param item reminder object.
     * @return reminder identifier
     */
    public long save(ReminderItem item){
        long id = ReminderHelper.getInstance(mContext).saveReminder(item);
        updateViews();
        return id;
    }

    /**
     * Add reminder to Google, Stock Calendar and/or Google Tasks.
     * @param item reminder object.
     * @param id reminder identifier.
     */
    protected void exportToServices(ReminderItem item, long id){
        long due = item.getDateTime();
        if (due > 0) {
            boolean stock = SharedPrefs.getInstance(mContext).getBoolean(Prefs.EXPORT_TO_STOCK);
            boolean calendar = SharedPrefs.getInstance(mContext).getBoolean(Prefs.EXPORT_TO_CALENDAR);
            JExport jExport = item.getModel().getExport();
            if (jExport != null) {
                if (jExport.getCalendar() == 1) {
                    ReminderUtils.exportToCalendar(mContext, item.getSummary(), due, id, calendar, stock);
                }
                if (jExport.getgTasks() == 1) {
                    ReminderUtils.exportToTasks(mContext, item.getSummary(), due, id);
                }
            }
        }
    }

    /**
     * Update all application widgets and permanent notification in Status Bar.
     */
    private void updateViews(){
        new Notifier(mContext).recreatePermanent();
        UpdatesHelper.getInstance(mContext).updateWidget();
    }
}
