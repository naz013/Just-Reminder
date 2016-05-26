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

package com.cray.software.justreminder.roboto_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.utils.AssetsUtil;

public class RoboTextView extends TextView {

    private static final String DEFAULT = "Roboto-Regular";
    private static final String EXT = ".ttf";
    private Typeface mTypeface;

    public RoboTextView(Context context) {
        super(context);
        init(null);
    }

    public RoboTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoboTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setDrawingCacheEnabled(true);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoboTextView);
            int fontCode = a.getInt(R.styleable.RoboTextView_font_style, -1);
            if (fontCode != -1) {
                mTypeface = AssetsUtil.getTypeface(getContext(), fontCode);
            } else {
                mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + DEFAULT + EXT);
            }
            a.recycle();
        } else {
            mTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/" + DEFAULT + EXT);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean onPreDraw() {
        return super.onPreDraw();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTypeface != null)
            setTypeface(mTypeface);
    }

    @Override
    public void setTypeface(Typeface tf) {
        super.setTypeface(tf);
    }
}
