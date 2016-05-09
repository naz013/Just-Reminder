package com.cray.software.justreminder.roboto_views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.cray.software.justreminder.R;
import com.cray.software.justreminder.utils.AssetsUtil;

public class RoboRadioButton extends RadioButton {

    private static final String DEFAULT = "Roboto-Regular";
    private static final String EXT = ".ttf";
    private Typeface mTypeface;

    public RoboRadioButton(Context context) {
        super(context);
        init(null);
    }

    public RoboRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoboRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoboRadioButton);
            int fontCode = a.getInt(R.styleable.RoboRadioButton_radio_font_style, -1);
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
}
