package com.cray.software.justreminder.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.utils.SuperUtil;
import com.cray.software.justreminder.utils.ViewUtils;

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

    public static final int TYPE_CALL = 1;
    public static final int TYPE_MESSAGE = 2;

    private Activity activity;
    private CheckBox actionCheck;
    private LinearLayout actionBlock;
    private RadioButton callAction, messageAction;
    private ImageButton selectNumber;
    private FloatingEditText numberView;

    private OnActionListener listener;

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

        actionBlock = (LinearLayout) findViewById(R.id.actionBlock);
        actionBlock.setVisibility(View.GONE);

        actionCheck = (CheckBox) findViewById(R.id.actionCheck);
        actionCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    ViewUtils.showOver(actionBlock);
                    selectNumber = (ImageButton) findViewById(R.id.selectNumber);
                    selectNumber.setOnClickListener(contactClick);
                    ViewUtils.setImage(selectNumber, new SharedPrefs(activity).loadBoolean(Prefs.USE_DARK_THEME));

                    numberView = (FloatingEditText) findViewById(R.id.numberView);

                    callAction = (RadioButton) findViewById(R.id.callAction);
                    callAction.setChecked(true);
                    messageAction = (RadioButton) findViewById(R.id.messageAction);
                    messageAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (listener != null){
                                listener.onTypeChange(b);
                            }
                        }
                    });
                } else {
                    ViewUtils.hideOver(actionBlock);
                }
                if (listener != null){
                    listener.onActionChange(b);
                }
            }
        });

        if (actionCheck.isChecked()) {
            ViewUtils.showOver(actionBlock);
        }
    }

    public void setListener(OnActionListener listener) {
        this.listener = listener;
    }

    public boolean hasAction(){
        return actionCheck.isChecked();
    }

    public void setAction(boolean action){
        actionCheck.setChecked(action);
    }

    public void showError(){
        numberView.setError(activity.getString(R.string.empty_field_error));
    }

    public int getType(){
        if (hasAction()){
            if (callAction.isChecked()){
                return TYPE_CALL;
            } else {
                return TYPE_MESSAGE;
            }
        } else {
            return 0;
        }
    }

    public void setType(int type){
        if (type == TYPE_CALL){
            callAction.setChecked(true);
        } else {
            messageAction.setChecked(true);
        }
    }

    public String getNumber(){
        return numberView.getText().toString().trim();
    }

    public void setNumber(String number){
        numberView.setText(number);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * Select contact button click listener.
     */
    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SuperUtil.selectContact(activity, Constants.REQUEST_CODE_CONTACTS);
        }
    };

    public interface OnActionListener{
        void onActionChange(boolean b);
        void onTypeChange(boolean type);
    }
}
