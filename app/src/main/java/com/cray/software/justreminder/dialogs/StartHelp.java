package com.cray.software.justreminder.dialogs;

import android.content.Intent;
import android.os.Bundle;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.modules.Module;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.hexrain.design.LogInActivity;

public class StartHelp extends AppIntro2 {

    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(AppIntroFragment.newInstance(getString(R.string.guide_title_flexible), getString(R.string.flexible_explanation), R.drawable.flexible_tr, getColorRes(R.color.colorBlueDark)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.guide_smart_title), getString(R.string.smart_explanation), R.drawable.smart_tr, getColorRes(R.color.colorIndigo)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.widgets_support_guide_title), getString(R.string.widget_explanation), R.drawable.widgets_tr, getColorRes(R.color.colorDeepPurple)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.dark_theme_guide_title), getString(R.string.dark_mode_explanation), R.drawable.dark_tr, getColorRes(R.color.colorViolet)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.guide_sync_title), getString(R.string.cloud_sync_explanation), R.drawable.cloud_tr, getColorRes(R.color.colorPink)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.simple_note), getString(R.string.note_explnation), R.drawable.note_tr, getColorRes(R.color.colorRed)));
        showDoneButton(true);
        setVibrate(false);
        setFadeAnimation();
    }

    private int getColorRes(int resId){
        if (Module.isMarshmallow()) return getResources().getColor(resId, null);
        return getResources().getColor(resId);
    }

    @Override
    public void onDonePressed() {
        startActivity(new Intent(this, LogInActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
    }
}
