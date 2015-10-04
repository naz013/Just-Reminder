package com.cray.software.justreminder.helpers;

import android.content.Context;
import android.widget.Toast;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;

/**
 * Copyright 2015 Nazar Suhovich
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
public class Messages {
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context context, int resId){
        Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
    }

    public static void snackbar(Context context, String message){
        boolean isLong = false;
        if (message.length() > 70){
            isLong = true;
        }
        SnackbarManager.show(
                Snackbar.with(context)
                        .text(message)
                        .type(isLong ? SnackbarType.MULTI_LINE : SnackbarType.SINGLE_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT));
    }

    public static void snackbar(Context context, int resId){
        boolean isLong = false;
        String message = context.getString(resId);
        if (message.length() > 70){
            isLong = true;
        }
        SnackbarManager.show(
                Snackbar.with(context)
                        .text(message)
                        .type(isLong ? SnackbarType.MULTI_LINE : SnackbarType.SINGLE_LINE)
                        .duration(Snackbar.SnackbarDuration.LENGTH_SHORT));
    }
}
