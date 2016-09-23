package com.wagos.calendarcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by Wagos
 */
public class CalendarCardView extends View {
    private static final int SHOW_PRESS = 0;
    private static final long TAP_TIMEOUT = ViewConfiguration.getTapTimeout();

    private Handler handler;

    private int width;
    private int headerHeight;
    private int textSize;
    private Paint paint;
    private Paint backgroundPaint;
    private Paint circlePaint;
    private Paint selectedPaint;
    private Paint selectedTextPaint;
    private Paint shaderPaint;

    private float cellSize;
    private float cellSizeHalf;
    private float textOffset;
    private float yOffset;

    private String monthName;
    private int[] dayNumbers = new int[42];
    private long[] dayTimestamp = new long[42];
    private String[] dayLabels = new String[7];
    private SparseArray<ValueAnimator> animatorMap = new SparseArray<>();
    private Queue<ValueAnimator> reusableAnimators = new ArrayDeque<>();
    private ArrayList<Long> selectedDates = new ArrayList<>();

    private int touchedPosition = -1;

    ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            postInvalidate();
        }
    };
    private boolean pressed;

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

        selectedTextPaint = new Paint(paint);
        selectedTextPaint.setColor(Color.WHITE);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);

        selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setColor(Color.BLUE);
        selectedPaint.setStyle(Paint.Style.FILL);

        circlePaint = new Paint(selectedPaint);
        circlePaint.setAlpha(selectedPaint.getAlpha() / 2);

        shaderPaint = new Paint();

        headerHeight = getResources().getDimensionPixelSize(R.dimen.calendar_card_header_height);
        float textHeight = paint.descent() - paint.ascent();
        textOffset = (textHeight / 2) - paint.descent();
        yOffset = textOffset + headerHeight;

        handler = new GestureHandler();
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
        animator.setDuration(pressed ? 400 : 200);
        animatorMap.put(position, animator);
        return animator;
    }

    private void recycleAnimator(int position) {
        ValueAnimator animator = animatorMap.get(position);
        animatorMap.remove(position);
        reusableAnimators.add(animator);
        animator.removeAllListeners();
    }

    private ValueAnimator createAnimator() {
        ValueAnimator rippleAnimator = ValueAnimator.ofFloat(0, cellSizeHalf);
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
        cellSizeHalf = cellSize / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthNums(canvas);
    }

    private void drawMonthNums(Canvas canvas) {
        float x = cellSizeHalf;

        float y = x + yOffset;
        float cellX = 0f;
        float cellY = headerHeight;
        int counter = 0;
        Paint textPaint;
        Canvas rippleCanvas = null;
        if (animatorMap.size() > 0) {
            Bitmap original = Bitmap.createBitmap(
                    getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            rippleCanvas = new Canvas(original);
            rippleCanvas.drawColor(Color.BLUE);
            Shader shader = new BitmapShader(original,
                    Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shaderPaint.setShader(shader);
        }
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
//                canvas.drawRect(cellX+1, cellY+1, cellX + cellSize-1, cellY + cellSize-1, backgroundPaint);
//                canvas.drawRect(cellX, cellY, cellX + cellSize, cellY + cellSize, circlePaint);
                ValueAnimator animator = animatorMap.get(counter);
                Float animatedValue = null;
                if (selectedDates.contains(dayTimestamp[counter])
                        && (animator == null || !animator.isRunning())
                        && !handler.hasMessages(counter)) {
                    canvas.drawCircle(x, cellY + cellSizeHalf, cellSizeHalf, selectedPaint);
                    textPaint = selectedTextPaint;
                } else {
                    textPaint = paint;
                    if (animator != null) {
                        rippleCanvas.drawText(String.valueOf(dayNumbers[counter]), x, y, selectedTextPaint);

                        animatedValue = (Float) animator.getAnimatedValue();
                        circlePaint.setAlpha((int) (selectedTextPaint.getAlpha() * animatedValue / cellSize));
                        canvas.drawCircle(x, cellY + cellSizeHalf, cellSizeHalf, circlePaint);
                    }
                }
                canvas.drawText(String.valueOf(dayNumbers[counter]), x, y, textPaint);
                if (animatedValue != null) {
                    canvas.drawCircle(x, cellY + cellSizeHalf, animatedValue, shaderPaint);
                }


                counter++;
                x += cellSize;
                cellX += cellSize;
            }
            cellY += cellSize;
            y += cellSize;
            x = cellSizeHalf;
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
            dayTimestamp[i] = 10000 * month.get(Calendar.YEAR) + 100 * month.get(Calendar.MONTH) + i;
            dayNumbers[i] = month.get(Calendar.DAY_OF_MONTH);
            if (i < 7) {
                dayLabels[i] = month.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            }
            month.add(Calendar.DATE, 1);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                final float y = event.getY();
                pressed = true;
                if (y > yOffset) {
                    touchedPosition = (int) ((y - yOffset) / cellSize) * 7 + (int) (event.getX() / cellSize);
                    if (!selectedDates.contains(dayTimestamp[touchedPosition])) {
                        handler.sendEmptyMessageAtTime(touchedPosition, event.getDownTime() + TAP_TIMEOUT);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                touchedPosition = -2;
                handler.removeMessages(SHOW_PRESS);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                pressed = false;
                if (touchedPosition >= 0) {
                    ValueAnimator animator = animatorMap.get(touchedPosition);
                    if (animator != null) {
                        if (animator.isRunning()) {
                            animator.setDuration(200);
                        } else {
                            recycleAnimator(touchedPosition);
                        }
                    }
                    if (!selectedDates.remove(dayTimestamp[touchedPosition])) {
                        selectedDates.add(dayTimestamp[touchedPosition]);
                    }
                    postInvalidate();
                    touchedPosition = -1;
                }
                break;
            }
        }
        return true;
    }

    private class GestureHandler extends Handler {
        GestureHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            if (touchedPosition != -2) {
                getAnimator(msg.what).start();
            }
        }
    }

    public void setSelectedDates(ArrayList<Long> selectedDates) {
        this.selectedDates = selectedDates;
    }
}
