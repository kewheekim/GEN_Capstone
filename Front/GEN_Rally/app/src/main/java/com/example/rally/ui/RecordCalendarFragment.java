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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.CalendarCardAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.GameReviewDto;
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
    private CalendarCardAdapter calendarCardAdapter;
    private List<GameReviewDto> allMonthlyGames = new ArrayList<>();
    private RecyclerView rvGames;
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
        rvGames = view.findViewById(R.id.rv_games);

        rvGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        calendarCardAdapter = new CalendarCardAdapter(new ArrayList<>());
        rvGames.setAdapter(calendarCardAdapter);

        apiService = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

        calendarCardAdapter.setOnItemClickListener(gameId -> {
            Intent intent = new Intent(requireContext(), GameResultActivity.class);
            intent.putExtra("gameId", gameId);
            startActivity(intent);
        });

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
                    Log.w("CalendarDebug", "해당 날짜에 gameId가 없습니다.");
                }
                calendarView.clearSelection();
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

        apiService.getMonthlyGames(year, month).enqueue(new Callback<RecordCalendarResponse>() {
            @Override
            public void onResponse(Call<RecordCalendarResponse> call, Response<RecordCalendarResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    applyCalendarData(response.body());
                }
            }

            @Override
            public void onFailure(Call<RecordCalendarResponse> call, Throwable t) {
                Log.e("RecordCalendarFragment", "달력 데이터 로드 실패", t);
            }
        });
    }

    private void applyCalendarData(RecordCalendarResponse response) {
        dateGameIdMap.clear();

        if (response.getGames() != null) {
            this.allMonthlyGames = response.getGames();
            calendarCardAdapter.setItems(allMonthlyGames);

            for (GameReviewDto game : response.getGames()) {
                if (game.getDate() != null) {
                    try {
                        LocalDate d = LocalDate.parse(game.getDate());
                        CalendarDay day = CalendarDay.from(d.getYear(), d.getMonthValue(), d.getDayOfMonth());

                        dateGameIdMap.put(day, game.getGameId());

                    } catch (Exception e) {
                        Log.e("CalendarFragment", "날짜 파싱 오류: " + game.getDate());
                    }
                }
            }
        }

        List<CalendarDay> eventDates = new ArrayList<>();
        if (response.getMarkedDates() != null) {
            for (String dateStr : response.getMarkedDates()) {
                LocalDate d = LocalDate.parse(dateStr);
                CalendarDay day = CalendarDay.from(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
                eventDates.add(day);
            }
        }

        calendarView.removeDecorators();
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar_select);
        if (drawable != null) {
            calendarView.addDecorator(new EventDecorator(drawable, eventDates));
        }
    }

    // 몇월 텍스트 업데이트
    private void updateMonthTitle(CalendarDay date) {
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
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(drawable);
        }
    }
}
