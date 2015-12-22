package com.cray.software.justreminder.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class RepeatView extends RelativeLayout{
    private TextView date;
    private TextView time;
    private Context mContext;
    private OnRepeatListener listener;

    public RepeatView(Context context) {
        super(context);
        init(context, null);
    }

    public RepeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RepeatView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.repeat_view_layout, this);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        date = (TextView) findViewById(R.id.dateField);
        time = (TextView) findViewById(R.id.timeField);

        this.mContext = context;
    }

    public void setListener(OnRepeatListener listener) {
        this.listener = listener;
    }

    public interface OnRepeatListener {
        void onDateSelect(int repeat);
    }
}
