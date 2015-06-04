package com.cray.software.justreminder.dialogs;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.fragments.StartFragment;
import com.hexrain.design.LogInActivity;

public class StartHelp extends FragmentActivity implements View.OnClickListener {

    ViewPager pager;
    PagerAdapter pagerAdapter;
    TextView skipButton, nextCloseButton;
    Typeface typeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        setContentView(R.layout.guide_layout);

        typeface = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        skipButton = (TextView) findViewById(R.id.skipButton);
        skipButton.setOnClickListener(this);
        skipButton.setTypeface(typeface);

        nextCloseButton = (TextView) findViewById(R.id.nextCloseButton);
        nextCloseButton.setOnClickListener(this);
        nextCloseButton.setTypeface(typeface);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {
                if (i > 0){
                    skipButton.setText(getString(R.string.previous_button));
                } else skipButton.setText(R.string.skip_button);

                if (i == 4){
                    nextCloseButton.setText(R.string.button_close);
                } else {
                    nextCloseButton.setText(R.string.next_button);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.skipButton:
                if (pager.getCurrentItem() == 0){
                    startActivity(new Intent(StartHelp.this, LogInActivity.class));
                    finish();
                } else pager.setCurrentItem(pager.getCurrentItem() - 1, true);
                break;
            case R.id.nextCloseButton:
                if (pager.getCurrentItem() == 4){
                    startActivity(new Intent(StartHelp.this, LogInActivity.class));
                    finish();
                } else {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                }
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return StartFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 5;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
