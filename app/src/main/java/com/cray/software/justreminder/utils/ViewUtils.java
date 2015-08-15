package com.cray.software.justreminder.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cray.software.justreminder.R;

public class ViewUtils {

    public ViewUtils(){}

    public static void setImage(ImageButton ib, boolean isDark){
        if (isDark){
            ib.setImageResource(R.drawable.ic_person_add_white_24dp);
        } else ib.setImageResource(R.drawable.ic_person_add_grey600_24dp);
    }

    public static void setImage(Button ib, boolean isDark){
        if (isDark){
            ib.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_white_24dp, 0, 0, 0);
        } else ib.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_add_grey600_24dp, 0, 0, 0);
    }

    public static void setTypeFont(Context context, TextView... views){
        Typeface typeface = AssetsUtil.getLightTypeface(context);
        for (TextView v : views){
            v.setTypeface(typeface);
        }
    }

    public static void fadeInAnimation(View view, boolean animation){
        if (animation) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setStartOffset(400);
            fadeIn.setDuration(400);
            view.setAnimation(fadeIn);
            view.setVisibility(View.VISIBLE);
        } else view.setVisibility(View.VISIBLE);
    }

    public static void fadeOutAnimation(View view, boolean animation){
        if (animation) {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
            fadeOut.setDuration(400);
            view.setAnimation(fadeOut);
            view.setVisibility(View.GONE);
        } else view.setVisibility(View.GONE);
    }

    public static void showOver(View view, boolean animation){
        if (animation) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new OvershootInterpolator());
            fadeIn.setDuration(300);
            view.setAnimation(fadeIn);
            view.setVisibility(View.VISIBLE);
        } else view.setVisibility(View.VISIBLE);
    }

    public static void hideOver(View view, boolean animation){
        if (animation) {
            Animation fadeIn = new AlphaAnimation(1, 0);
            fadeIn.setInterpolator(new OvershootInterpolator());
            fadeIn.setDuration(300);
            view.setAnimation(fadeIn);
            view.setVisibility(View.GONE);
        } else view.setVisibility(View.GONE);
    }

    public static void show(Context context, View v, boolean animation) {
        if (animation) {
            Animation slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom);
            v.startAnimation(slide);
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void hide(Context context, View v, boolean animation) {
        if (animation) {
            Animation slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom_out);
            v.startAnimation(slide);
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    public static void zoom(View view, int pos, int number){
        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        animation.setStartOffset(pos * number * 20 + 100);
        animation.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void zoom(View view, long duration){
        ScaleAnimation animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        animation.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void zoomOut(View view, long duration){
        ScaleAnimation animation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        animation.setFillAfter(true);
        animation.setInterpolator(new DecelerateInterpolator());
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                } else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
