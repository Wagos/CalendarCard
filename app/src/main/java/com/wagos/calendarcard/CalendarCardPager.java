package com.wagos.calendarcard;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by Wagos
 */
public class CalendarCardPager extends ViewPager{

    public CalendarCardPager(Context context) {
        super(context);
        init();
    }

    public CalendarCardPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setAdapter(new CalendarCardPagerAdapter());
    }
}
