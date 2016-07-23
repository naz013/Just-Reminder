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

package com.cray.software.justreminder.notes;

import android.content.Context;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.app_widgets.UpdatesHelper;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.Notifier;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.helpers.SyncHelper;
import com.cray.software.justreminder.helpers.Telephony;
import com.cray.software.justreminder.interfaces.NavigationCallbacks;

import org.json.JSONException;

import java.io.File;

public class Note {

    /**
     * Create note file and send it to other users.
     * @param id note identifier.
     * @param context application context.
     * @return Boolean
     */
    public static boolean shareNote(long id, Context context) {
        SyncHelper sHelp = new SyncHelper(context);
        boolean res = false;
        NoteItem item = NoteHelper.getInstance(context).getNote(id);
        if (item != null) {
            String note = item.getNote();
            if (SharedPrefs.getInstance(context).getBoolean(Prefs.NOTE_ENCRYPT)) {
                note = SyncHelper.decrypt(note);
            }
            item.setNote(note);
            String uuID = item.getUuId();
            if (uuID == null || uuID.matches("")) {
                item.setUuId(SyncHelper.generateID());
            }
            try {
                File file = sHelp.createNote(item);
                if (!file.exists() || !file.canRead()) {
                    return false;
                } else {
                    res = true;
                    Telephony.sendNote(file, context, note);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Delete note from database and file from SDCard or Cloud.
     * @param id note identifier.
     * @param context application context.
     * @param callbacks callback for toast or snackbar message.
     */
    public static void deleteNote(long id, Context context, NavigationCallbacks callbacks) {
        String uuId = NoteHelper.getInstance(context).deleteNote(id).getUuId();
        new DeleteNoteFilesAsync(context).execute(uuId);
        new UpdatesHelper(context).updateNotesWidget();
        new Notifier(context).discardStatusNotification(id);
        if (callbacks != null) callbacks.showSnackbar(context.getString(R.string.note_deleted));
        else Messages.toast(context, R.string.note_deleted);
    }
}
