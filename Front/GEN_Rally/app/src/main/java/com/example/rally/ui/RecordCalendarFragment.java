package com.example.rally.ui;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.RecordCalendarResponse;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.prolificinteractive.materialcalendarview.format.ArrayWeekDayFormatter;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordCalendarFragment extends Fragment {
    private MaterialCalendarView calendarView;
    private TextView tvWeeks;
    private ImageButton btnPrevWeek, btnNextWeek;
    private ApiService apiService;
    private TokenStore tokenStore;
    private long userId = -1L;
    private Map<CalendarDay, Long> dateGameIdMap = new HashMap<>();

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

        apiService = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

        initCalendarDesign();
        initListeners();

        CalendarDay today = CalendarDay.today();
        loadMonthlyData(today.getYear(), today.getMonth());
    }

    private void initCalendarDesign() {
        calendarView.setTopbarVisible(false);
        calendarView.setWeekDayFormatter(new ArrayWeekDayFormatter(new CharSequence[]{"월", "화", "수", "목", "금", "토", "일"}));

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

        // 월 변경시 다시 api 호출
        calendarView.setOnMonthChangedListener((widget, date) -> {
            updateMonthTitle(date);
            loadMonthlyData(date.getYear(), date.getMonth());
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            if (selected) {
                Long gameId = dateGameIdMap.get(date);

                if (gameId != null) {
                    Intent intent = new Intent(requireContext(), GameResultActivity.class);
                    intent.putExtra("gameId", gameId);
                    startActivity(intent);
                } else {
                    calendarView.clearSelection();
                }
            }
        });
    }

    private void loadMonthlyData(int year, int month) {
        try {
            tokenStore = new TokenStore(requireContext());

            userId = tokenStore.getUserId();

            if (userId == -1L) { // 로그인 안 되어있으면
                startActivity(new Intent(requireActivity(), AuthActivity.class));
                requireActivity().finish();
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e("RecordCalendarFragment", "TokenStore 초기화 실패", e);
            requireActivity().finish();
        }

        apiService.getMonthlyGames(year, month).enqueue(new Callback<List<RecordCalendarResponse>>() {
            @Override
            public void onResponse(Call<List<RecordCalendarResponse>> call, Response<List<RecordCalendarResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyCalendarData(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<RecordCalendarResponse>> call, Throwable t) {
                Log.e("RecordCalendarFragment", "달력 데이터 로드 실패", t);
            }
        });
    }

    private void applyCalendarData(List<RecordCalendarResponse> games) {
        dateGameIdMap.clear(); // 기존 데이터 초기화
        List<CalendarDay> eventDates = new ArrayList<>();

        for (RecordCalendarResponse game : games) {
            LocalDate d = LocalDate.parse(game.getDate());
            CalendarDay day = CalendarDay.from(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
            eventDates.add(day);
            dateGameIdMap.put(day, game.getGameId());
        }

        // 데코레이터 갱신
        calendarView.removeDecorators(); // 기존 데코레이터 제거
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_select);
        if (drawable != null) {
            calendarView.addDecorator(new EventDecorator(drawable, eventDates));
        }
    }

    // "M월" 텍스트 업데이트
    private void updateMonthTitle(CalendarDay date) {
        // date.getMonth()는 라이브러리 버전에 따라 0~11 또는 1~12일 수 있습니다.
        // 최신 버전 기준 1~12이므로 그대로 사용합니다.
        tvWeeks.setText(date.getMonth() + "월");
    }

    // 데코레이터
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
