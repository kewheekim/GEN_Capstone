package com.example.rally.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.rally.R;

public class ScoreBarView extends View {

    private int maxValue = 21;
    private int value = 0;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    private final RectF bgRect = new RectF();
    private final Path barPath = new Path();

    private boolean fillFromRight = false; // true: 오른쪽 → 왼쪽
    private boolean roundLeft = false;
    private boolean roundRight = false;

    public ScoreBarView(Context context) {
        super(context);
        init(null);
    }

    public ScoreBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ScoreBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        int bgColor = ContextCompat.getColor(getContext(), R.color.gray_inactive);
        int barColor = ContextCompat.getColor(getContext(), R.color.green_active);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ScoreBarView);
            maxValue = a.getInt(R.styleable.ScoreBarView_maxValue, maxValue);
            value = a.getInt(R.styleable.ScoreBarView_value, value);
            bgColor = a.getColor(R.styleable.ScoreBarView_bgColor, bgColor);
            barColor = a.getColor(R.styleable.ScoreBarView_barColor, barColor);
            fillFromRight = a.getBoolean(R.styleable.ScoreBarView_fillFromRight, false);
            roundLeft = a.getBoolean(R.styleable.ScoreBarView_roundLeft, false);
            roundRight = a.getBoolean(R.styleable.ScoreBarView_roundRight, false);
            a.recycle();
        }

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(bgColor);

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        if (width <= 0 || height <= 0) return;

        // 배경: 전체 막대를 양쪽 둥글게
        float bgRadius = height / 2f;
        bgRect.set(0, 0, width, height);
        canvas.drawRoundRect(bgRect, bgRadius, bgRadius, bgPaint);

        if (maxValue <= 0 || value <= 0) return;

        float ratio = Math.max(0f, Math.min(1f, value / (float) maxValue));
        float barWidth = width * ratio;
        if (barWidth <= 0f) return;

        // 막대 위치 계산
        if (fillFromRight) {
            // 오른쪽 → 왼쪽
            barRect.set(width - barWidth, 0, width, height);
        } else {
            // 왼쪽 → 오른쪽
            barRect.set(0, 0, barWidth, height);
        }

        // 한쪽만 둥글게 하기 위한 radii 계산
        float radius = height / 2f;
        float[] radii = new float[8]; // tl,tl, tr,tr, br,br, bl,bl

        if (roundLeft) {
            // 이 View 기준 왼쪽 모서리들
            radii[0] = radius; // top-left x
            radii[1] = radius; // top-left y
            radii[6] = radius; // bottom-left x
            radii[7] = radius; // bottom-left y
        }
        if (roundRight) {
            // 이 View 기준 오른쪽 모서리들
            radii[2] = radius; // top-right x
            radii[3] = radius; // top-right y
            radii[4] = radius; // bottom-right x
            radii[5] = radius; // bottom-right y
        }

        barPath.reset();
        barPath.addRoundRect(barRect, radii, Path.Direction.CW);
        canvas.drawPath(barPath, barPaint);
    }

    // ===== 공개 메서드 =====

    public void setValue(int value) {
        this.value = value;
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        invalidate();
    }

    public void setFillFromRight(boolean fillFromRight) {
        this.fillFromRight = fillFromRight;
        invalidate();
    }

    public void setRoundLeft(boolean roundLeft) {
        this.roundLeft = roundLeft;
        invalidate();
    }

    public void setRoundRight(boolean roundRight) {
        this.roundRight = roundRight;
        invalidate();
    }

    public void setBarColorInt(int color) {
        barPaint.setColor(color);
        invalidate();
    }

    public void setBgColorInt(int color) {
        bgPaint.setColor(color);
        invalidate();
    }
}
