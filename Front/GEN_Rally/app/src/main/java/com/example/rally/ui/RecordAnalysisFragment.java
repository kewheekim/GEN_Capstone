package com.example.rally.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.RecordGoalAdapter;
import com.example.rally.adapter.RecordAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class RecordAnalysisFragment extends Fragment {
    private BarChart barChart;
    private RecyclerView rvGoals, rvRecords;
    private RecordGoalAdapter goalAdapter;
    private RecordAdapter recordAdapter;
    private TextView tvNewGoal;
    private ImageButton btnPrevWeek, btnNextWeek, btnCompliment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = view.findViewById(R.id.chart_kcal);
        rvGoals = view.findViewById(R.id.rv_goals);
        rvRecords = view.findViewById(R.id.rv_records);
        tvNewGoal = view.findViewById(R.id.tv_new_goal);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        btnCompliment = view.findViewById(R.id.btn_compliment);

        rvGoals.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // TODO: GoalAdapter 클래스 생성 후 주석 해제
        //goalAdapter = new RecordGoalAdapter(new ArrayList<>());
        // rvGoals.setAdapter(goalAdapter);

        rvRecords.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // TODO: RecordAdapter 클래스 생성 후 주석 해제
        // recordAdapter = new RecordAdapter(new ArrayList<>());
        // rvRecords.setAdapter(recordAdapter);

        // 모서리 렌더러 설정
        barChart.setRenderer(new RoundedBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler()));

        // 차트 외형 커스텀
        setupChartAppearance();

        // 차트 데이터 세팅
        loadChartData();
    }

    private void setupClickListeners() {
        tvNewGoal.setOnClickListener(v -> {
            // TODO: 새 목표 추가 화면으로 이동
        });

        btnPrevWeek.setOnClickListener(v -> {
            // TODO: 이전 주 데이터 로드
        });

        btnNextWeek.setOnClickListener(v -> {
            // TODO: 다음 주 데이터 로드
        });

        btnCompliment.setOnClickListener(v -> {
            // TODO: 칭찬 상세 화면으로 이동
        });
    }

    private void setupChartAppearance() {

        barChart.getDescription().setEnabled(false); // 차트 설명 텍스트 비활성화
        barChart.getLegend().setEnabled(false); // 범례 비활성화
        barChart.setTouchEnabled(false); //  차트 터치 비활성화
        barChart.setDrawGridBackground(false); // 그리드 배경 비활성화
        barChart.setScaleEnabled(false); // 확대/축소 비활성화
        barChart.setDrawBorders(false);

        // X축 (요일) 설정
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X축을 하단에 표시
        xAxis.setDrawGridLines(false); // X축 그리드 라인 비활성화
        xAxis.setDrawAxisLine(false); // [수정] X축 라인 숨기기
        xAxis.setGranularity(1f); // X축 간격

        // X축 라벨
        final String[] days = {"일", "월", "화", "수", "목", "금", "토"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                if (value >= 0 && value < days.length) {
                    return days[(int) value];
                }
                return "";
            }
        });
        xAxis.setTextColor(Color.parseColor("#757575")); // X축 라벨 색상
        xAxis.setTextSize(12f);


        // Y축 (칼로리) 설정
        // 왼쪽 Y축 비활성화 (라인, 라벨 모두 숨김)
        barChart.getAxisLeft().setEnabled(false);
        // 오른쪽 Y축 비활성화
        barChart.getAxisRight().setEnabled(false);

        // [추가] Y축 그리드 라인 (점선)
        YAxis leftAxis = barChart.getAxisLeft(); // 설정은 leftAxis에서 가져와야 함
        leftAxis.setDrawLabels(false); // 라벨만 숨김
        leftAxis.setDrawAxisLine(false); // 축 라인 숨김
        leftAxis.setDrawGridLines(true); // 그리드 라인 표시
        leftAxis.setGridColor(Color.parseColor("#E0E0E0")); // 그리드 라인 색상
        leftAxis.enableGridDashedLine(10f, 10f, 0f); // 점선으로 변경
        leftAxis.setAxisMinimum(0f); // Y축 최소값을 0으로 설정
    }

    private void loadChartData() {
        // TODO: 헬스데이터 불러와서 여기에 적용
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 89f));  // 일
        entries.add(new BarEntry(1f, 150f)); // 월
        entries.add(new BarEntry(2f, 89f));  // 화
        entries.add(new BarEntry(3f, 110f)); // 수
        entries.add(new BarEntry(4f, 40f));  // 목
        entries.add(new BarEntry(5f, 65f));  // 금
        entries.add(new BarEntry(6f, 89f));  // 토

        BarDataSet dataSet = new BarDataSet(entries, "Weekly Kcal");

        // 바 위에 숫자 표시
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#343434"));

        // 값 포맷터 (소수점 제거)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.valueOf((int) barEntry.getY());
            }
        });

        // 바 색상 설정
        dataSet.setColor(Color.parseColor("#2ABA72"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f); // 바 두께 (0.1f ~ 1.0f)

        barChart.setData(barData);
        barChart.setMinOffset(0f);
        barChart.invalidate();
    }
}
