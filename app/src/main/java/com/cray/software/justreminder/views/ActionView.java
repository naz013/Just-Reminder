package com.cray.software.justreminder.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.cray.software.justreminder.R;

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
public class ActionView extends LinearLayout {
    private Context mContext;

    public ActionView(Context context) {
        super(context);
        init(context, null);
    }

    public ActionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ActionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.action_view_layout, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setOrientation(VERTICAL);
        //TextView repeatType = (TextView) findViewById(R.id.repeatType);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.RepeatView, 0, 0);

            String titleText = "";
            try {
                titleText = a.getString(R.styleable.RepeatView_repeat_type_text);
            } catch (Exception e) {
                Log.e("RepeatView", "There was an error loading attributes.");
            } finally {
                a.recycle();
            }
        }

        this.mContext = context;
    }
}
