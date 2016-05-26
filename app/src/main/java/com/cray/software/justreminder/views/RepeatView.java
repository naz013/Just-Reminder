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

package com.cray.software.justreminder.views;

import android.app.AlarmManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cray.software.justreminder.R;

public class RepeatView extends LinearLayout implements SeekBar.OnSeekBarChangeListener, TextWatcher {
    private EditText repeatTitle;
    private SeekBar repeatViewSeek;
    private Context mContext;
    private OnRepeatListener listener;
    private InputMethodManager imm;


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
        setOrientation(VERTICAL);
        repeatTitle = (EditText) findViewById(R.id.repeatTitle);
        TextView repeatType = (TextView) findViewById(R.id.repeatType);
        repeatViewSeek = (SeekBar) findViewById(R.id.repeatViewSeek);

        repeatViewSeek.setOnSeekBarChangeListener(this);
        repeatTitle.addTextChangedListener(this);

        repeatTitle.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                imm = (InputMethodManager) mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (!hasFocus) {
                    imm.hideSoftInputFromWindow(repeatTitle.getWindowToken(), 0);
                } else {
                    imm.showSoftInput(repeatTitle, 0);
                }
            }
        });
        repeatTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                imm = (InputMethodManager) mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (!imm.isActive(repeatTitle)){
                    imm.showSoftInput(repeatTitle, 0);
                }
            }
        });

        repeatViewSeek.setProgress(0);
        repeatTitle.setText(String.valueOf(0));

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

            repeatType.setText(titleText);
        }

        this.mContext = context;
    }

    public void setListener(OnRepeatListener listener) {
        this.listener = listener;
    }

    public void setMax(int max){
        repeatViewSeek.setMax(max);
    }

    public void setProgress(int progress){
        if (progress < repeatViewSeek.getMax()) {
            repeatViewSeek.setProgress(progress);
            updateEditField();
        }
    }

    private void updateEditField() {
        repeatTitle.setSelection(repeatTitle.getText().length());
    }

    public void setProgress(long mills){
        long progress = mills / AlarmManager.INTERVAL_DAY;
        if (progress < repeatViewSeek.getMax()) {
            repeatViewSeek.setProgress((int) progress);
            updateEditField();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        repeatTitle.setText(String.valueOf(progress));
        if (listener != null){
            listener.onProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (listener != null){
            try {
                int res = Integer.parseInt(s.toString());
                listener.onProgress(res);
                if (res < repeatViewSeek.getMax()) {
                    setProgress(res);
                } else {
                    setProgress(repeatViewSeek.getMax());
                }
            } catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnRepeatListener {
        void onProgress(int progress);
    }
}
