package com.example.rally.ui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class HeartRateView extends View {

    public static class HeartSample {
        public final int bpm;
        public final long epochMs;

        public HeartSample(int bpm, long epochMs) {
            this.bpm = bpm;
            this.epochMs = epochMs;
        }
    }

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path fillPath = new Path();
    private final Path strokePath = new Path();

    private final Paint markerFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<HeartSample> samples = new ArrayList<>();
    private float minBpm = 60f;
    private float maxBpm = 180f;

    public HeartRateView(Context context) {
        super(context);
        init();
    }

    public HeartRateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeartRateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 채워진 영역
        fillPaint.setStyle(Paint.Style.FILL);
        // 그래프 선
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(0xFFFF3E30);
        linePaint.setStrokeWidth(2f * getResources().getDisplayMetrics().density);
        linePaint.setAntiAlias(true);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        // 마커
        markerFillPaint.setStyle(Paint.Style.FILL);
        markerFillPaint.setColor(0xFFFF5C50);
        markerStrokePaint.setStyle(Paint.Style.STROKE);
        markerStrokePaint.setStrokeWidth(1f * getResources().getDisplayMetrics().density);
        markerStrokePaint.setColor(0xFFFF5C50);
    }

    public void setHeartSeries(List<HeartSample> list) {
        this.samples = list != null ? list : new ArrayList<>();

        // y축 세팅
        if (!this.samples.isEmpty()) {
            float dataMin = Float.MAX_VALUE;
            float dataMax = Float.MIN_VALUE;
            for (HeartSample s : this.samples) {
                if (s.bpm < dataMin) dataMin = s.bpm;
                if (s.bpm > dataMax) dataMax = s.bpm;
            }
            float padding = (dataMax - dataMin) * 0.2f;
            if (padding < 5) padding = 5;
            minBpm = dataMin - padding;
            maxBpm = dataMax + padding;
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 세로 그라데이션
        Shader shader = new LinearGradient(
                0, 0, 0, h,
                0xFFFF5C50,
                0xFF2B9E64,
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(shader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (samples == null || samples.size() < 2) return;

        float width = getWidth();
        float height = getHeight();

        float leftPadding = 6*getResources().getDisplayMetrics().density;
        float rightPadding = 6*getResources().getDisplayMetrics().density;
        float topPadding = 0f;
        float bottomPadding = 0f;

        int n = samples.size();
        float usableWidth = width - leftPadding - rightPadding;
        float dx = usableWidth / (n - 1);
        float usableHeight = height - topPadding - bottomPadding;

        fillPath.reset();

        // 마커 계산
        float[] xs = new float[n];
        float[] ys = new float[n];

        // 첫 점
        HeartSample first = samples.get(0);
        xs[0] = leftPadding;
        ys[0] = bpmToY(first.bpm, topPadding, usableHeight);

        // 상단 선 시작
        strokePath.moveTo(xs[0], ys[0]);

        // 영역 path 시작
        fillPath.moveTo(xs[0], height - bottomPadding);
        fillPath.lineTo(xs[0], ys[0]);

        int minIndex = 0;
        int maxIndex = 0;
        int minBpmValue = first.bpm;
        int maxBpmValue = first.bpm;

        // 나머지 점들
        for (int i = 1; i < n; i++) {
            HeartSample s = samples.get(i);
            xs[i] = leftPadding + dx * i;
            ys[i] = bpmToY(s.bpm, topPadding, usableHeight);
            fillPath.lineTo(xs[i], ys[i]);
            strokePath.lineTo(xs[i], ys[i]);

            // 최고/최저점 인덱스 계산
            if (s.bpm < minBpmValue) {
                minBpmValue = s.bpm;
                minIndex = i;
            }
            if (s.bpm > maxBpmValue) {
                maxBpmValue = s.bpm;
                maxIndex = i;
            }
        }

        // 마지막 점에서 바닥으로 내려와서 영역 닫기
        float xLast = xs[n - 1];
        fillPath.lineTo(xLast, height - bottomPadding);
        fillPath.close();

        // 영역 채우기
        canvas.drawPath(fillPath, fillPaint);
        // 그래프 상단 선
        canvas.drawPath(strokePath, linePaint);

        // 최고, 최저 마커
        float markerRadius = 3f * getResources().getDisplayMetrics().density;

        // 최저점
        float minX = xs[minIndex];
        float minY = ys[minIndex];
        canvas.drawCircle(minX, minY, markerRadius, markerFillPaint);
        canvas.drawCircle(minX, minY, markerRadius, markerStrokePaint);

        // 최고점
        float maxX = xs[maxIndex];
        float maxY = ys[maxIndex];
        canvas.drawCircle(maxX, maxY, markerRadius, markerFillPaint);
        canvas.drawCircle(maxX, maxY, markerRadius, markerStrokePaint);
    }

    private float bpmToY(int bpm, float topPadding, float usableHeight) {
        if (maxBpm == minBpm) return topPadding + usableHeight / 2f;

        float ratio = (bpm - minBpm) / (maxBpm - minBpm);
        return topPadding + (1f - ratio) * usableHeight;
    }
}
