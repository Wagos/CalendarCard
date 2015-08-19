package com.wagos.calendarcard;

import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

/**
 * Created by Wagos
 */
public class CalendarCardPagerAdapter extends RecyclePagerAdapter {
    @Override
    protected Object getItem(int position) {
        return position;
    }

    @Override
    protected View getView(Object object, View convertView, ViewGroup parent) {
        CalendarCardView calendar;
        if(convertView == null){
            calendar = new CalendarCardView(parent.getContext());
        }else {
            calendar = (CalendarCardView) convertView;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, (Integer) object);

        return calendar;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }
}
