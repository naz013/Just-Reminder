package com.cray.software.justreminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.cray.software.justreminder.LogInActivity;
import com.cray.software.justreminder.R;
import com.cray.software.justreminder.adapters.IntroPagerAdapter;
import com.cray.software.justreminder.datas.models.IntroModel;
import com.cray.software.justreminder.modules.Module;

import java.util.ArrayList;

public class StartHelp extends AppCompatActivity {

    private ViewPager introPager;
    private Button doneButton, skipButton;
    private IntroPagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_help);

        introPager = (ViewPager) findViewById(R.id.introPager);
        introPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position > 0){
                    skipButton.setText(R.string.previous);
                    doneButton.setText(R.string.next);
                } else if (position == adapter.getCount() - 1){
                    doneButton.setText(R.string.simple_done);
                } else {
                    skipButton.setText(R.string.skip_button);
                    doneButton.setText(R.string.next);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (introPager.getCurrentItem() == adapter.getCount() - 1) {
                    startActivity(new Intent(StartHelp.this, LogInActivity.class));
                    finish();
                } else {
                    introPager.setCurrentItem(introPager.getCurrentItem() + 1, true);
                }
            }
        });

        skipButton = (Button) findViewById(R.id.skipButton);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (introPager.getCurrentItem() == 0) {
                    startActivity(new Intent(StartHelp.this, LogInActivity.class));
                    finish();
                } else {
                    introPager.setCurrentItem(introPager.getCurrentItem() - 1, true);
                }
            }
        });

        init();
    }

    public void init() {
        ArrayList<IntroModel> list = new ArrayList<>();
        list.add(new IntroModel(
                getString(R.string.guide_title_flexible),
                getString(R.string.flexible_explanation),
                R.drawable.flexible_tr,
                getColorRes(R.color.bluePrimaryDark)));
        list.add(new IntroModel(
                getString(R.string.guide_smart_title),
                getString(R.string.smart_explanation),
                R.drawable.smart_tr,
                getColorRes(R.color.indigoPrimary)));
        list.add(new IntroModel(
                getString(R.string.widgets_support_guide_title),
                getString(R.string.widget_explanation),
                R.drawable.widgets_tr,
                getColorRes(R.color.purpleDeepPrimary)));
        list.add(new IntroModel(
                getString(R.string.dark_theme_guide_title),
                getString(R.string.dark_mode_explanation),
                R.drawable.dark_tr,
                getColorRes(R.color.purplePrimary)));
        list.add(new IntroModel(
                getString(R.string.guide_sync_title),
                getString(R.string.cloud_sync_explanation),
                R.drawable.cloud_tr,
                getColorRes(R.color.pinkPrimary)));
        list.add(new IntroModel(
                getString(R.string.simple_note),
                getString(R.string.note_explnation),
                R.drawable.note_tr,
                getColorRes(R.color.redPrimary)));

        adapter = new IntroPagerAdapter(getSupportFragmentManager(), list);
        introPager.setAdapter(adapter);
    }

    private int getColorRes(int resId){
        if (Module.isMarshmallow()) {
            return getResources().getColor(resId, null);
        }
        return getResources().getColor(resId);
    }

    @Override
    public void onBackPressed() {
    }
}
