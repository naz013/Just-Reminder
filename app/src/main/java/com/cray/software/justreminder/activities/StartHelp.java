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
import com.cray.software.justreminder.utils.ViewUtils;

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
                    doneButton.setText(R.string.done);
                } else {
                    skipButton.setText(R.string.skip);
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
                getString(R.string.flexible),
                R.drawable.flexible_tr,
                ViewUtils.getColor(this, R.color.bluePrimaryDark)));
        list.add(new IntroModel(
                getString(R.string.smart),
                R.drawable.smart_tr,
                ViewUtils.getColor(this, R.color.indigoPrimary)));
        list.add(new IntroModel(
                getString(R.string.widgets_support),
                R.drawable.widgets_tr,
                ViewUtils.getColor(this, R.color.purpleDeepPrimary)));

        try {
            adapter = new IntroPagerAdapter(this, list);
            introPager.setAdapter(adapter);
        } catch (OutOfMemoryError e) {
            startActivity(new Intent(this, LogInActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
    }
}
