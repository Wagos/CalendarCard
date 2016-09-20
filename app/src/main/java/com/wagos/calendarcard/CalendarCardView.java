package com.wagos.calendarcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

/**
 * Created by Wagos
 */
public class CalendarCardView extends View {
    private int width;
    private Paint paint;
    private int textSize;
    private Paint backgroundPaint;
    private Paint circlePaint;
    private int headerHeight;
    private float textOffset;
    private String monthName;
    private int[] dayNumbers = new int[42];
    private long[] dayTimestamp = new long[42];
    private String[] dayLabels = new String[7];
    private HashMap<Integer, ValueAnimator> animatorMap = new HashMap<>();
    private Queue<ValueAnimator> reusableAnimators = new ArrayDeque<>();
    private int touchedPosition = -1;
    private float cellSize;
    private Paint ripplePaint;

    ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            postInvalidate();
        }
    };
    private Calendar startDate;

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
        paint.setColor(Color.BLACK);
        textSize = getResources().getDimensionPixelSize(R.dimen.month_text_size);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);

        ripplePaint = new Paint();
        ripplePaint.setAntiAlias(true);
        ripplePaint.setColor(Color.BLUE);
        ripplePaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint(ripplePaint);
        ripplePaint.setAlpha(ripplePaint.getAlpha() / 2);

        headerHeight = getResources().getDimensionPixelSize(R.dimen.calendar_card_header_height);
        float textHeight = paint.descent() - paint.ascent();
        textOffset = (textHeight / 2) - paint.descent();

    }

    private ValueAnimator getAnimator(final int position) {
        ValueAnimator animator = reusableAnimators.poll();
        if (animator == null) {
            animator = createAnimator();
        }
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                if (touchedPosition != position) {
                    recycleAnimator(position);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (touchedPosition != position) {
                    recycleAnimator(position);
                }
            }
        });

        animator.addUpdateListener(updateListener);
        animator.setDuration(400);
        animatorMap.put(position, animator);
        return animator;
    }

    private void recycleAnimator(int position) {
        ValueAnimator animator = animatorMap.remove(position);
        reusableAnimators.add(animator);
        animator.removeAllListeners();
    }

    private ValueAnimator createAnimator() {
        ValueAnimator rippleAnimator = ValueAnimator.ofFloat(0, cellSize / 2f);
        rippleAnimator.setInterpolator(new LinearInterpolator());
        return rippleAnimator;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), size * 6 / 7 + headerHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        cellSize = w / 7f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    private void drawMonthNums(Canvas canvas) {
        float x = cellSize / 2f;

        float y = x + textOffset + headerHeight;
        float cellX = 0f;
        float cellY = headerHeight;
        int counter = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
//                canvas.drawRect(cellX+1, cellY+1, cellX + cellSize-1, cellY + cellSize-1, backgroundPaint);
//                canvas.drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, circlePaint);
                for (Map.Entry<Integer, ValueAnimator> entry : animatorMap.entrySet()) {
                    if (entry.getKey() == counter) {
                        Float animatedValue = (Float) entry.getValue().getAnimatedValue();
                        circlePaint.setAlpha((int) (ripplePaint.getAlpha() * animatedValue / cellSize));
                        canvas.drawCircle(x, cellY + cellSize / 2f, cellSize / 2f, circlePaint);
                        canvas.drawCircle(x, cellY + cellSize / 2f, animatedValue, ripplePaint);
                    }
                }
                canvas.drawText(String.valueOf(dayNumbers[counter++]), x, y, paint);
                x += cellSize;
                cellX += cellSize;
            }
            cellY += cellSize;
            y += cellSize;
            x = cellSize / 2f;
            cellX = 0;
        }
    }

    private void drawMonthDayLabels(Canvas canvas) {
        float x = cellSize / 2;
        for (int i = 0; i < 7; i++) {
            canvas.drawText(dayLabels[i], x, headerHeight - textOffset, paint);
            x += cellSize;
        }
    }

    private void drawMonthTitle(Canvas canvas) {
        canvas.drawText(monthName, width / 2, headerHeight / 2.5f, paint);
    }

    public void setDate(Calendar month) {
        monthName = month.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        int firstDayOfWeek = month.getFirstDayOfWeek();
        month.set(Calendar.DAY_OF_MONTH, 1);
        if (month.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            month.set(Calendar.DAY_OF_WEEK, firstDayOfWeek);
        }
        for (int i = 0; i < dayNumbers.length; i++) {
            dayTimestamp[i] = month.getTimeInMillis();
            dayNumbers[i] = month.get(Calendar.DAY_OF_MONTH);
            if (i < 7) {
                dayLabels[i] = month.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            }
            month.add(Calendar.DATE, 1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final float yOffset = textOffset + headerHeight;
                if (y > yOffset) {
                    int newTouchedPosition = (int) ((y - yOffset) / cellSize) * 7 + (int) (x / cellSize);
                    if (newTouchedPosition != touchedPosition) {
                        touchedPosition = newTouchedPosition;
                        getAnimator(touchedPosition).start();
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                ValueAnimator animator = animatorMap.get(touchedPosition);
                if (animator != null) {
                    if (animator.isRunning()) {
                        animator.setDuration(200);
                    } else {
                        recycleAnimator(touchedPosition);
                        postInvalidate();
                    }
                }
                touchedPosition = -1;
                break;
            }

        }
        return true;
    }
}
