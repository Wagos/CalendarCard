package com.wagos.calendarcard;

import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Wagos
 */
public class CalendarCardPagerAdapter extends RecyclePagerAdapter {

    private final ArrayList<Long> selectedDates = new ArrayList<>();

    @Override
    protected Object getItem(int position) {
        return position - CalendarCardPager.OFFSET;
    }

    @Override
    protected View getView(Object object, View convertView, ViewGroup parent) {
        CalendarCardView calendar;
        if (convertView == null) {
            calendar = new CalendarCardView(parent.getContext());
            calendar.setSelectedDates(selectedDates);
        } else {
            calendar = (CalendarCardView) convertView;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, (Integer) object);

        calendar.setDate(cal);

        return calendar;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }
}
