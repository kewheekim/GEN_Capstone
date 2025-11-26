package com.example.rally.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.rally.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class RecordCalendarFragment extends Fragment {
    private MaterialCalendarView calendarView;
    private TextView tvWeeks;
    private ImageButton btnPrevWeek, btnNextWeek;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendar_view);
        tvWeeks = view.findViewById(R.id.tv_weeks);
        btnPrevWeek = view.findViewById(R.id.btn_prev_week);
        btnNextWeek = view.findViewById(R.id.btn_next_week);

        initCalendarDesign();
        initListeners();
        loadSampleData();

    }

    private void initCalendarDesign() {
        calendarView.setTopbarVisible(false);
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(new CharSequence[]{"일", "월", "화", "수", "목", "금", "토"}));

        updateMonthTitle(calendarView.getCurrentDate());
    }

    private void initListeners() {
        // 달력 넘길 때 월 텍스트 변경
        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                updateMonthTitle(date);
            }
        });

        btnPrevWeek.setOnClickListener(v -> calendarView.goToPrevious());
        btnNextWeek.setOnClickListener(v -> calendarView.goToNext());
    }

    // "M월" 텍스트 업데이트
    private void updateMonthTitle(CalendarDay date) {
        // date.getMonth()는 라이브러리 버전에 따라 0~11 또는 1~12일 수 있습니다.
        // 최신 버전 기준 1~12이므로 그대로 사용합니다.
        tvWeeks.setText(date.getMonth() + "월");
    }

    // 샘플 데이터 로드 (나중에 API 연동 시 이 부분을 수정하세요)
    private void loadSampleData() {
        List<CalendarDay> eventDates = new ArrayList<>();

        int year = CalendarDay.today().getYear();
        eventDates.add(CalendarDay.from(year, 11, 3));
        eventDates.add(CalendarDay.from(year, 11, 5));
        eventDates.add(CalendarDay.from(year, 11, 14));

        Drawable characterDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_select);

        if (characterDrawable != null) {
            // 데코레이터 적용
            calendarView.addDecorator(new EventDecorator(characterDrawable, eventDates));
        }
    }

    // ==========================================
    //  [내부 클래스] 이벤트 데코레이터
    // ==========================================
    private static class EventDecorator implements DayViewDecorator {
        private final Drawable drawable;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(Drawable drawable, Collection<CalendarDay> dates) {
            this.drawable = drawable;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            // 해당 날짜가 리스트에 있으면 true -> decorate 실행
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            // 배경 이미지를 셔틀콕으로 설정
            view.setBackgroundDrawable(drawable);
        }
    }
}
