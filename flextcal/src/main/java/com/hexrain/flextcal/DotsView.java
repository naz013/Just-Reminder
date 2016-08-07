package com.hexrain.flextcal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Copyright 2016 Nazar Suhovich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DotsView extends TextView {

    private static final String TAG = "DotsView";
    private Events events;
    private Paint paint;

    public DotsView(Context context) {
        super(context);
        init(context, null);
    }

    public DotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DotsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        paint = new Paint();
        setDrawingCacheEnabled(true);
    }

    public void setEvents(Events events) {
        this.events = events;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        if (events != null) {
            int n = events.count();
            int sqrt = (int) Math.sqrt(n);
            Log.d(TAG, "onDraw: " + n + ", sqrt " + sqrt);
            for (int i = 0; i < sqrt; i++) {
                for (int j = 0; j < sqrt; j++) {
                    Events.Event event = events.getNext();
                    if (event == null) continue;
                    paint.setColor(event.getColor());
                    canvas.drawCircle(((float) i + 0.5f) / n * width, ((float) j + 0.5f) / n * width, 3, paint);
                }
            }
        }
    }
}
