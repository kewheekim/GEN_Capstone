package com.example.rally.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.RecordCommentAdapter;
import com.example.rally.adapter.RecordGoalAdapter;
import com.example.rally.adapter.RecordGameAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.RecordAnalysisResponse;
import com.example.rally.dto.RecordWeeklyCalorie;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordAnalysisFragment extends Fragment {
    private BarChart barChart;
    private RecyclerView rvGoals, rvRecords, rvComments;
    private RecordGoalAdapter goalAdapter;
    private RecordGameAdapter recordGameAdapter;
    private RecordCommentAdapter recordCommentAdapter;
    private TextView tvNewGoal, tvWeeks;
    private ImageButton btnPrevWeek, btnNextWeek, btnCompliment;
    private ApiService apiService;
    private TokenStore tokenStore;
    private long userId = -1L;
    private String dateParam = LocalDate.now().toString();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_record_analysis, container, false);

        try {
            tokenStore = new TokenStore(requireContext());

            userId = tokenStore.getUserId();

            if (userId == -1L) {
                startActivity(new Intent(requireActivity(), AuthActivity.class));
                requireActivity().finish();
                return view;
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e("RecordAnalysisFragment", "TokenStore 초기화 실패", e);
            requireActivity().finish();
            return view;
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barChart = view.findViewById(R.id.chart_kcal);
        rvGoals = view.findViewById(R.id.rv_goals);
        rvRecords = view.findViewById(R.id.rv_records);
        rvComments = view.findViewById(R.id.rv_comments);
        tvNewGoal = view.findViewById(R.id.tv_new_goal);
        tvWeeks = view.findViewById(R.id.tv_weeks);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);
        btnCompliment = view.findViewById(R.id.btn_compliment);


        apiService = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

        rvGoals.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        goalAdapter = new RecordGoalAdapter(new ArrayList<>());
        rvGoals.setAdapter(goalAdapter);

        rvRecords.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recordGameAdapter = new RecordGameAdapter(new ArrayList<>());
        rvRecords.setAdapter(recordGameAdapter);

        rvComments.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recordCommentAdapter = new RecordCommentAdapter(new ArrayList<>());
        rvComments.setAdapter(recordCommentAdapter);

        // 모서리 렌더러 설정
        barChart.setRenderer(new RoundedBarChartRenderer(barChart, barChart.getAnimator(), barChart.getViewPortHandler()));

        setupClickListeners();
        // 차트 외형 커스텀
        setupChartAppearance();

        loadAnalysisData();
        // 차트 데이터 세팅
        loadChartData(dateParam);
    }

    private void setupClickListeners() {
        tvNewGoal.setOnClickListener(v -> {
            // TODO: 새 목표 추가 화면으로 이동
        });

        btnPrevWeek.setOnClickListener(v -> {
            LocalDate current = LocalDate.parse(dateParam);
            LocalDate prevWeek = current.minusWeeks(1);
            dateParam = prevWeek.toString();
            loadChartData(dateParam);
        });

        btnNextWeek.setOnClickListener(v -> {
            LocalDate current = LocalDate.parse(dateParam);
            LocalDate nextWeek = current.plusWeeks(1);
            dateParam = nextWeek.toString();
            loadChartData(dateParam);
        });

        btnCompliment.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RecordCommentActivity.class);
            startActivity(intent);
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

    // 목표, 게임 카드 조회
    private void loadAnalysisData(){
        apiService.getRecordAnalysis().enqueue(new Callback<RecordAnalysisResponse>() {
            @Override
            public void onResponse(Call<RecordAnalysisResponse> call, Response<RecordAnalysisResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecordAnalysisResponse data = response.body();

                    if (data.getGoalItems() != null) {
                        goalAdapter.setItems(data.getGoalItems());
                    }

                    if (data.getGameResults() != null) {
                        recordGameAdapter.setItems(data.getGameResults());
                    }

                    if (data.getComments() != null) {
                        recordCommentAdapter.setItems(data.getComments());
                    }

                } else {
                    Log.e("AnalysisFragment", "분석 데이터 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RecordAnalysisResponse> call, Throwable t) {
                Log.e("AnalysisFragment", "통신 오류", t);
            }
        });
    }

    // 칼로리 조회
    private void loadChartData(String date) {
        apiService.getWeeklyCalorie(date).enqueue(new Callback<RecordWeeklyCalorie>() {
            @Override
            public void onResponse(Call<RecordWeeklyCalorie> call, Response<RecordWeeklyCalorie> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecordWeeklyCalorie data = response.body();
                    String title = data.getTitle(); // 11월 2주차
                    tvWeeks.setText(title);
                    List<RecordWeeklyCalorie.DailyCalorie> list = data.getDailyCalories();
                    ArrayList<BarEntry> entries = new ArrayList<>();

                    for (int i = 0; i < 7; i++) {
                        entries.add(new BarEntry((float) i, 0f));
                    }

                    for (RecordWeeklyCalorie.DailyCalorie item : list) {
                        int index = getDayIndex(item.getDayOfWeek());
                        entries.set(index, new BarEntry((float) index, (float) item.getCalories()));
                    }
                    updateChart(entries);
                } else {
                    Log.e("RecordAnalysisFragment", "칼로리 로드 실패");
                }
            }

            @Override
            public void onFailure(Call<RecordWeeklyCalorie> call, Throwable t) {
                Log.e("RecordAnalysisFragment", "서버 오류",t);
            }
        });
    }

    private int getDayIndex(String dayOfWeek) {
        switch (dayOfWeek) {
            case "일": return 0;
            case "월": return 1;
            case "화": return 2;
            case "수": return 3;
            case "목": return 4;
            case "금": return 5;
            case "토": return 6;
            default: return 0;
        }
    }

    private void updateChart(ArrayList<BarEntry> entries) {
        BarDataSet dataSet = new BarDataSet(entries, "Weekly Kcal");

        // 바 위에 숫자 표시
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.parseColor("#343434"));

        // 값 포맷터 (소수점 제거: 150.0 -> 150)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                return String.valueOf((int) barEntry.getY());
            }
        });

        // 바 색상 설정
        dataSet.setColor(Color.parseColor("#2ABA72"));

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // 바 두께 조절

        barChart.setData(barData);
        barChart.invalidate(); // 차트 새로고침 (필수)
    }
}
