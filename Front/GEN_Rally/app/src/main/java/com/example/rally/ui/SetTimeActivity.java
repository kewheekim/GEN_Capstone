package com.example.rally.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.DateAdapter;
import com.example.rally.adapter.HourAdapter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SetTimeActivity extends AppCompatActivity {
        private RecyclerView rvDates, rvHours;
        private Button nextBtn;
        private HourAdapter hourAdapter;

        // 선택된 값 저장
        private LocalDate selectedDate;
        private Set<Integer> selectedHours = new HashSet<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_match_2);

            Toolbar toolbar = findViewById(R.id.include_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

            rvDates  = findViewById(R.id.rv_dates);
            rvHours  = findViewById(R.id.rv_hours);
            nextBtn  = findViewById(R.id.next_button);
            nextBtn.setEnabled(false);

            setupHourRecycler();
            setupDateRecycler();

            nextBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, SetLocationActivity.class);
                intent.putExtra("date", selectedDate.toString());
                intent.putIntegerArrayListExtra(
                        "hours", new ArrayList<>(selectedHours));
                startActivity(intent);
            });
        }

        private void setupDateRecycler() {
            // 오늘부터 6일 뒤까지
            List<LocalDate> dates = new ArrayList<>();
            LocalDate today = LocalDate.now();
            for (int i = 0; i <= 6; i++) {
                dates.add(today.plusDays(i));
            }

            DateAdapter dateAdapter = new DateAdapter(dates, date -> {
                selectedDate = date;
                updateNextButtonState();
            });

            rvDates.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvDates.setAdapter(dateAdapter);

            // 디폴트로 오늘 선택
            selectedDate = today;
        }

    private void updateNextButtonState() {
        selectedHours = hourAdapter.getSelected();  // 항상 최신 선택 상태 가져오기

        boolean isEnabled = selectedDate != null && !selectedHours.isEmpty();
        nextBtn.setEnabled(isEnabled);
        nextBtn.setBackgroundResource(isEnabled ? R.drawable.bg_next_button_active : R.drawable.bg_next_button_inactive);
        nextBtn.setTextColor(Color.parseColor(isEnabled ? "#FFFFFF" : "#AAAAAA"));
    }

        @SuppressLint("ClickableViewAccessibility")
        private void setupHourRecycler() {
            // 0시부터 23시까지
            List<Integer> hours = new ArrayList<>();
            for (int h = 0; h < 24; h++) hours.add(h);

            hourAdapter = new HourAdapter(hours);
            GridLayoutManager glm = new GridLayoutManager(this, 6);
            rvHours.setLayoutManager(glm);
            rvHours.setAdapter(hourAdapter);

            // 드래그 & 터치로 시간대 토글
            rvHours.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent ev) {
                    int action = ev.getActionMasked();
                    View child = rvHours.findChildViewUnder(ev.getX(), ev.getY());

                    if (child != null) {
                        HourAdapter.VH vh = (HourAdapter.VH) rvHours.getChildViewHolder(child);
                        int hour = hours.get(vh.getAdapterPosition());

                        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                            hourAdapter.toggle(hour);
                            updateNextButtonState();
                        }
                    }

                    if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                        v.performClick();
                    }
                    return false; // 스크롤은 유지
                }
            });

        }
    }
