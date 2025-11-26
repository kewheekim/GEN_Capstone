package com.example.rally.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;


public class RecordFragment extends Fragment {

    TextView tvAnalysis, tvCalendar;
    int selectedColor;
    int unselectedColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAnalysis = view.findViewById(R.id.tv_analysis);
        tvCalendar = view.findViewById(R.id.tv_calendar);

        selectedColor = tvAnalysis.getCurrentTextColor();
        unselectedColor = Color.parseColor("#AAAAAA");

        tvAnalysis.setOnClickListener(v -> showAnalysis());
        tvCalendar.setOnClickListener(v -> showCalendar());

        // 최초 실행 시 분석 화면 표시
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.record_fragment_container, new RecordAnalysisFragment())
                    .commit();
            tvAnalysis.setTextColor(selectedColor);
            tvCalendar.setTextColor(unselectedColor);
        }
    }


    // 분석 뷰
    public void showAnalysis() {
        tvAnalysis.setTextColor(selectedColor);
        tvCalendar.setTextColor(unselectedColor);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.record_fragment_container, new RecordAnalysisFragment())
                .addToBackStack(null)
                .commit();
    }

    // 달력 뷰
    public void showCalendar() {
        tvAnalysis.setTextColor(unselectedColor);
        tvCalendar.setTextColor(selectedColor);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.record_fragment_container, new RecordCalendarFragment())
                .addToBackStack(null)
                .commit();
    }
}