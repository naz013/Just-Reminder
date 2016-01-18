/*
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

package com.cray.software.justreminder.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.constants.Prefs;
import com.cray.software.justreminder.helpers.Messages;
import com.cray.software.justreminder.helpers.SharedPrefs;
import com.cray.software.justreminder.interfaces.ActionCallbacksExtended;

public class ExtraView extends LinearLayout implements View.OnClickListener {

    boolean isVibrate, isVoice, isUnlock, isAwake, isNotification, isAuto;
    boolean isDark, enableAuto;

    ImageView vIcon, voIcon, uIcon, wIcon, rIcon, aIcon;
    TextView vText, voText, uText, wText, rText, aText;
    RelativeLayout aContainer, vContainer, voContainer, wContainer, rContainer, uContainer;

    Context context;
    String type;
    ActionCallbacksExtended callbacksExtended;

    public ExtraView(Context context) {
        super(context);
        init(context, null);
    }

    public ExtraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExtraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        View.inflate(context, R.layout.extra_view_layout, this);
        setOrientation(VERTICAL);

        isDark = new SharedPrefs(context).loadBoolean(Prefs.USE_DARK_THEME);

        vIcon = (ImageView) findViewById(R.id.vIcon);
        voIcon = (ImageView) findViewById(R.id.voIcon);
        uIcon = (ImageView) findViewById(R.id.uIcon);
        wIcon = (ImageView) findViewById(R.id.wIcon);
        aIcon = (ImageView) findViewById(R.id.aIcon);
        rIcon = (ImageView) findViewById(R.id.rIcon);

        if (isDark) {
            vIcon.setImageResource(R.drawable.ic_vibration_white_24dp);
            voIcon.setImageResource(R.drawable.ic_multitrack_audio_white_24dp);
            uIcon.setImageResource(R.drawable.ic_screen_lock_portrait_white_24dp);
            wIcon.setImageResource(R.drawable.ic_brightness_high_white_24dp);
            aIcon.setImageResource(R.drawable.ic_send_white_24dp);
            rIcon.setImageResource(R.drawable.ic_notifications_white_24dp);
        } else {
            vIcon.setImageResource(R.drawable.ic_vibration_black_24dp);
            voIcon.setImageResource(R.drawable.ic_multitrack_audio_black_24dp);
            uIcon.setImageResource(R.drawable.ic_screen_lock_portrait_black_24dp);
            wIcon.setImageResource(R.drawable.ic_brightness_high_black_24dp);
            aIcon.setImageResource(R.drawable.ic_send_black_24dp);
            rIcon.setImageResource(R.drawable.ic_notifications_black_24dp);
        }

        vText = (TextView) findViewById(R.id.vText);
        voText = (TextView) findViewById(R.id.voText);
        uText = (TextView) findViewById(R.id.uText);
        wText = (TextView) findViewById(R.id.wText);
        rText = (TextView) findViewById(R.id.rText);
        aText = (TextView) findViewById(R.id.aText);

        aContainer = (RelativeLayout) findViewById(R.id.aContainer);
        vContainer = (RelativeLayout) findViewById(R.id.vContainer);
        voContainer = (RelativeLayout) findViewById(R.id.voContainer);
        uContainer = (RelativeLayout) findViewById(R.id.uContainer);
        wContainer = (RelativeLayout) findViewById(R.id.wContainer);
        rContainer = (RelativeLayout) findViewById(R.id.rContainer);

        aContainer.setOnClickListener(this);
        vContainer.setOnClickListener(this);
        voContainer.setOnClickListener(this);
        uContainer.setOnClickListener(this);
        wContainer.setOnClickListener(this);
        rContainer.setOnClickListener(this);

        aContainer.setOnLongClickListener(longClickListener);
        vContainer.setOnLongClickListener(longClickListener);
        voContainer.setOnLongClickListener(longClickListener);
        uContainer.setOnLongClickListener(longClickListener);
        wContainer.setOnLongClickListener(longClickListener);
        rContainer.setOnLongClickListener(longClickListener);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(
                    attrs, R.styleable.ExtraView, 0, 0);

            try {
                enableAuto = a.getBoolean(R.styleable.ExtraView_has_action, true);
            } catch (Exception e) {
                Log.e("ExtraView", "There was an error loading attributes.");
            } finally {
                a.recycle();
            }
        }

        updateView(enableAuto);
    }

    public int getVibrate() {
        return isVibrate ? 1 : 0;
    }

    public int getVoice() {
        return isVoice ? 1 : 0;
    }

    public int getUnlock() {
        return isUnlock ? 1 : 0;
    }

    public int getAwake() {
        return isAwake ? 1 : 0;
    }

    public int getRepeat() {
        return isNotification ? 1 : 0;
    }

    public int getAuto() {
        return isAuto ? 1 : 0;
    }

    public void setCallbacksExtended(ActionCallbacksExtended callbacksExtended) {
        this.callbacksExtended = callbacksExtended;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void updateView(boolean enableAuto) {
        this.enableAuto = enableAuto;
        aContainer.setVisibility(enableAuto ? VISIBLE : GONE);
    }

    public void setValues(int v, int vo, int u, int w, int r, int a) {
        isVibrate = v == -1;
        isVoice = vo == -1;
        isUnlock = u == -1;
        isAwake = w == -1;
        isNotification = r == -1;
        isAuto = a == -1;
        switchA();
        switchR();
        switchW();
        switchU();
        switchV();
        switchVo();
    }

    private void switchV() {
        isVibrate = !isVibrate;
        if (isVibrate) vText.setText(R.string.enabled4);
        else vText.setText(R.string.disabled);
    }

    private void switchVo() {
        isVoice = !isVoice;
        if (isVoice) voText.setText(R.string.enabled4);
        else voText.setText(R.string.disabled);
    }

    private void switchU() {
        isUnlock = !isUnlock;
        if (isUnlock) uText.setText(R.string.enabled4);
        else uText.setText(R.string.disabled);
    }

    private void switchW() {
        isAwake = !isAwake;
        if (isAwake) wText.setText(R.string.enabled4);
        else wText.setText(R.string.disabled);
    }

    private void switchR() {
        isNotification = !isNotification;
        if (isNotification) rText.setText(R.string.enabled4);
        else rText.setText(R.string.disabled);
    }

    private void switchA() {
        isAuto = !isAuto;
        if (isAuto) aText.setText(R.string.enabled4);
        else aText.setText(R.string.disabled);
    }

    /**
     * Long click listener for extra options buttons.
     * Show toast with button action explanation.
     */
    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            String message = "";
            switch (v.getId()){
                case R.id.aContainer:
                    message = context.getString(R.string.enable_sending_sms_automatically);
                    if (type.contains(Constants.TYPE_APPLICATION)) message = context.getString(R.string.launch_application);
                    break;
                case R.id.vContainer:
                    message = context.getString(R.string.vibrate);
                    break;
                case R.id.voContainer:
                    message = context.getString(R.string.voice_notification);
                    break;
                case R.id.wContainer:
                    message = context.getString(R.string.turn_on_screen);
                    break;
                case R.id.uContainer:
                    message = context.getString(R.string.unlock_screen);
                    break;
                case R.id.rContainer:
                    message = context.getString(R.string.repeat_notification);
                    break;
            }
            if (callbacksExtended != null)
                callbacksExtended.showSnackbar(message);
            else Messages.toast(context, message);
            return false;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vContainer:
                switchV();
                break;
            case R.id.voContainer:
                switchVo();
                break;
            case R.id.uContainer:
                switchU();
                break;
            case R.id.wContainer:
                switchW();
                break;
            case R.id.rContainer:
                switchR();
                break;
            case R.id.aContainer:
                switchA();
                break;
        }
    }
}
