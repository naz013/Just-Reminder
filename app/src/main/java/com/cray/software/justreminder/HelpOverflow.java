package com.cray.software.justreminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cray.software.justreminder.interfaces.Constants;

public class HelpOverflow extends Activity {

    private LinearLayout swipeItem, simpleTap, longTap, swipeHeader, swipeUpDown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        setContentView(R.layout.main_help_overflov);

        Intent intent = getIntent();
        final int code = intent.getIntExtra(Constants.ITEM_ID_INTENT, 0);

        FrameLayout help_container = (FrameLayout) findViewById(R.id.help_container);

        RelativeLayout list_help = (RelativeLayout) findViewById(R.id.list_help);
        RelativeLayout create_help = (RelativeLayout) findViewById(R.id.create_help);
        help_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (code == 1){
                    if (swipeItem.getVisibility() == View.VISIBLE){
                        swipeItem.setVisibility(View.GONE);
                        simpleTap.setVisibility(View.VISIBLE);
                    } else if (simpleTap.getVisibility() == View.VISIBLE){
                        simpleTap.setVisibility(View.GONE);
                        longTap.setVisibility(View.VISIBLE);
                    } else if (longTap.getVisibility() == View.VISIBLE){
                        finish();
                    }
                } else if (code == 2){
                    if (swipeHeader.getVisibility() == View.VISIBLE){
                        swipeHeader.setVisibility(View.GONE);
                        swipeUpDown.setVisibility(View.VISIBLE);
                    } else if (swipeUpDown.getVisibility() == View.VISIBLE){
                        finish();
                    }
                }
            }
        });

        swipeItem = (LinearLayout) findViewById(R.id.swipeItem);
        simpleTap = (LinearLayout) findViewById(R.id.simpleTap);
        longTap = (LinearLayout) findViewById(R.id.longTap);
        swipeHeader = (LinearLayout) findViewById(R.id.swipeHeader);
        swipeUpDown = (LinearLayout) findViewById(R.id.swipeUpDown);

        if (code == 1){
            list_help.setVisibility(View.VISIBLE);
            swipeItem.setVisibility(View.VISIBLE);
        } else if (code == 2){
            create_help.setVisibility(View.VISIBLE);
            swipeHeader.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return MotionEvent.ACTION_OUTSIDE != event.getAction() && super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
    }
}
