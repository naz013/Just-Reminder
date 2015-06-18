package com.cray.software.justreminder.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.view.View;

public class StripedDrawable extends Drawable{

    Paint paint = new Paint();
    int colorStatus, colorNormal, colorTheme;

    public StripedDrawable(int colorStatus, int colorNormal, int colorTheme) {
        this.colorStatus = colorStatus;
        this.colorNormal = colorNormal;
        this.colorTheme = colorTheme;
    }

    @Override
    public void draw(Canvas canvas) {
        paint.setStrokeWidth(0);
        paint.setColor(colorStatus);
        canvas.drawRect(0, 0, 200, 100, paint);
        paint.setColor(colorNormal);
        canvas.drawRect(0, 100, 200, 200, paint);
        paint.setColor(colorTheme);
        canvas.drawRect(0, 200, 200, 300, paint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
