package com.wagos.calendarcard;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

/**
 * Created by Wagos
 */
public class CalendarCardView extends View{
    private int width;
    private Paint paint;
    private int textSize;
    private Paint backgroundPaint;
    private Paint circlePaint;

    public CalendarCardView(Context context) {
        super(context);
        init(context);
    }

    public CalendarCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CalendarCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CalendarCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        textSize = getResources().getDimensionPixelSize(R.dimen.month_text_size);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint();
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), size*6/7);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
//
        // Invalidate cached accessibility information.
//        mTouchHelper.invalidateRoot();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    private void drawMonthNums(Canvas canvas) {
        float cellSize = width /7;
        float x = cellSize/2;
        float textHeight = paint.descent() - paint.ascent();
        float textOffset = (textHeight / 2) - paint.descent();
        float y = x + textOffset;
        float cellX = 0;
        float cellY = 0;
        int counter = 0;
        for(int i=0; i < 6; i++){
            for(int j=1; j<8; j++){
                canvas.drawRect(cellX+1, cellY+1, cellX + cellSize-1, cellY + cellSize-1, backgroundPaint);
//                canvas.drawCircle(x, cellY + cellSize / 2, cellSize / 2, circlePaint);
                canvas.drawText(String.valueOf(++counter), x, y, paint);
                x+=cellSize;
                cellX+=cellSize;
            }
            cellY+=cellSize;
            y+=cellSize;
            x=cellSize/2;
            cellX=0;
        }
    }

    private void drawMonthDayLabels(Canvas canvas) {

    }

    private void drawMonthTitle(Canvas canvas) {
        canvas.drawText("January", width/2, textSize, paint);
    }

    /**
     * Provides a virtual view hierarchy for interfacing with an accessibility service.
     */
    private class MonthViewTouchHelper extends ExploreByTouchHelper {

        /**
         * Factory method to create a new {@link ExploreByTouchHelper}.
         *
         * @param forView View whose logical children are exposed by this helper.
         */
        public MonthViewTouchHelper(View forView) {
            super(forView);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            return 0;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {

        }

        @Override
        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {

        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {

        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            return false;
        }
    }
}
