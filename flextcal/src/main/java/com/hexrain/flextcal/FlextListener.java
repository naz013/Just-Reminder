package com.hexrain.flextcal;

import android.view.View;

import java.util.Date;

/**
 * Created by nazar on 17.08.15.
 */
public interface FlextListener{
    void onClickDate(Date date, View view);
    void onLongClickDate(Date date, View view);
    void onMonthChanged(int month, int year);
    void onCaldroidViewCreated();
}
