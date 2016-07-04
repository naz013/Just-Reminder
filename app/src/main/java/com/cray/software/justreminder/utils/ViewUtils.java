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

package com.cray.software.justreminder.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.constants.Constants;
import com.cray.software.justreminder.modules.Module;

public class ViewUtils {

    /**
     * Create ColorStateList for FAB from colors.
     * @param context application context.
     * @param colorNormal color normal.
     * @param colorPressed color pressed.
     * @return ColorStateList
     */
    public static ColorStateList getFabState(Context context, @ColorRes int colorNormal, @ColorRes int colorPressed) {
        int[][] states = {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_focused}, new int[] {}
        };
        int colorP = getColor(context, colorPressed);
        int colorN = getColor(context, colorNormal);
        int colors[] = {colorP, colorN, colorN};
        return new ColorStateList(states, colors);
    }

    /**
     * Get drawable from resource.
     * @param context application context.
     * @param resource drawable resource.
     * @return Drawable
     */
    public static Drawable getDrawable (Context context, @DrawableRes int resource){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return context.getResources().getDrawable(resource, null);
        } else {
            return context.getResources().getDrawable(resource);
        }
    }

    /**
     * Get color from resource.
     * @param context application context.
     * @param resource color resource.
     * @return Color
     */
    @ColorInt
    public static int getColor(Context context, @ColorRes int resource) {
        try {
            if (Module.isMarshmallow())
                return context.getResources().getColor(resource, null);
            else
                return context.getResources().getColor(resource);
        } catch (Resources.NotFoundException e) {
            return resource;
        }
    }

    public static void slideInUp(Context context, View view){
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_up);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    public static void slideOutDown(Context context, View view){
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_down);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void slideOutUp(Context context, View view){
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_up_out);
        view.startAnimation(animation);
        view.setVisibility(View.GONE);
    }

    public static void slideInDown(Context context, View view){
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.slide_down_in);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    /**
     * Get icon for reminder type.
     * @param typePrefs type of reminder.
     * @return Drawable resource
     */
    @DrawableRes
    public static int getIcon(String typePrefs) {
        int icon;
        if (typePrefs.contains(Constants.TYPE_CALL)) {
            icon = R.drawable.ic_call_white_24dp;
        } else if (typePrefs.contains(Constants.TYPE_MESSAGE)) {
            icon = R.drawable.ic_textsms_white_vector;
        } else if (typePrefs.matches(Constants.TYPE_LOCATION) || typePrefs.matches(Constants.TYPE_LOCATION_OUT)) {
            icon = R.drawable.ic_navigation_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_TIME)) {
            icon = R.drawable.ic_timer_white_24dp;
        } else if (typePrefs.startsWith(Constants.TYPE_SKYPE)) {
            icon = R.drawable.skype_icon_white;
        } else if (typePrefs.matches(Constants.TYPE_APPLICATION)) {
            icon = R.drawable.ic_launch_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_APPLICATION_BROWSER)) {
            icon = R.drawable.ic_public_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_SHOPPING_LIST)) {
            icon = R.drawable.ic_shopping_cart_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_MAIL)) {
            icon = R.drawable.ic_email_white_24dp;
        } else if (typePrefs.matches(Constants.TYPE_PLACES)) {
            icon = R.drawable.ic_near_me_white_24dp;
        } else {
            icon = R.drawable.ic_event_white_24dp;
        }
        return icon;
    }

    /**
     * Set contact image to ImageButton.
     * @param ib ImageButton.
     * @param isDark dark theme flag.
     */
    public static void setImage(ImageButton ib, boolean isDark){
        if (isDark){
            ib.setImageResource(R.drawable.ic_person_add_white_24dp);
        } else ib.setImageResource(R.drawable.ic_person_add_black_24dp);
    }

    public static void fadeInAnimation(View view){
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOutAnimation(View view){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(400);
        view.setAnimation(fadeOut);
        view.setVisibility(View.GONE);
    }

    public static void show(View view){
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setStartOffset(400);
        fadeIn.setDuration(400);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void hide(View view){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(400);
        view.setAnimation(fadeOut);
        view.setVisibility(View.INVISIBLE);
    }

    public static void hideFull(View view){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setDuration(400);
        view.setAnimation(fadeOut);
        view.setVisibility(View.GONE);
    }

    public static void showOver(View view){
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new OvershootInterpolator());
        fadeIn.setDuration(300);
        view.setAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void hideOver(View view){
        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new OvershootInterpolator());
        fadeIn.setDuration(300);
        view.setAnimation(fadeIn);
        view.setVisibility(View.GONE);
    }

    public static void show(Context context, View v) {
        Animation slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom);
        v.startAnimation(slide);
        v.setVisibility(View.VISIBLE);
    }

    public static void hide(Context context, View v) {
        Animation slide = AnimationUtils.loadAnimation(context, R.anim.scale_zoom_out);
        v.startAnimation(slide);
        v.setVisibility(View.GONE);
    }

    public static void showReveal(View v) {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setDuration(300);
        v.setAnimation(fadeIn);
        v.setVisibility(View.VISIBLE);
    }

    public static void hideReveal(View v) {
        Animation fadeIn = new AlphaAnimation(1, 0);
        fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeIn.setDuration(300);
        v.setAnimation(fadeIn);
        v.setVisibility(View.GONE);
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
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
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
