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

    private RecyclerView rvDates;
    private RecyclerView rvHoursAm, rvHoursPm;
    private Button nextBtn;

    private HourAdapter hourAdapterAm, hourAdapterPm;

    // 선택된 값 저장
    private LocalDate selectedDate;
    private Set<Integer> selectedHours = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        Toolbar toolbar = findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        rvDates = findViewById(R.id.rv_dates);
        rvHoursAm = findViewById(R.id.rv_hours_am);
        rvHoursPm = findViewById(R.id.rv_hours_pm);
        nextBtn = findViewById(R.id.next_button);
        nextBtn.setEnabled(false);

        setupDateRecycler();
        setupHourRecycler();

        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetLocationActivity.class);
            intent.putExtra("date", selectedDate.toString());
            intent.putIntegerArrayListExtra("hours", new ArrayList<>(selectedHours));
            startActivity(intent);
        });
    }

    private void setupDateRecycler() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i <= 6; i++) {
            dates.add(today.plusDays(i));
        }

        DateAdapter dateAdapter = new DateAdapter(dates, date -> {
            selectedDate = date;
            updateNextButtonState();
        });

        rvDates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDates.setAdapter(dateAdapter);

        // 기본 선택 날짜
        selectedDate = today;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupHourRecycler() {
        List<Integer> hoursAm = new ArrayList<>();
        List<Integer> hoursPm = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            if (h < 12) hoursAm.add(h);
            else hoursPm.add(h);
        }

        hourAdapterAm = new HourAdapter(hoursAm);
        hourAdapterPm = new HourAdapter(hoursPm);

        rvHoursAm.setLayoutManager(new GridLayoutManager(this, 6));
        rvHoursPm.setLayoutManager(new GridLayoutManager(this, 6));

        rvHoursAm.setAdapter(hourAdapterAm);
        rvHoursPm.setAdapter(hourAdapterPm);

        rvHoursAm.setOnTouchListener((v, ev) -> handleTouch(ev, rvHoursAm, hoursAm, hourAdapterAm));
        rvHoursPm.setOnTouchListener((v, ev) -> handleTouch(ev, rvHoursPm, hoursPm, hourAdapterPm));
    }

    private boolean handleTouch(MotionEvent ev, RecyclerView recyclerView, List<Integer> hours, HourAdapter adapter) {
        int action = ev.getActionMasked();
        View child = recyclerView.findChildViewUnder(ev.getX(), ev.getY());

        if (child != null) {
            HourAdapter.VH vh = (HourAdapter.VH) recyclerView.getChildViewHolder(child);
            int hour = hours.get(vh.getAdapterPosition());

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                adapter.toggle(hour);
                updateNextButtonState();
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            recyclerView.performClick();
        }

        return false; // 스크롤 유지
    }

    private void updateNextButtonState() {
        selectedHours.clear();
        selectedHours.addAll(hourAdapterAm.getSelected());
        selectedHours.addAll(hourAdapterPm.getSelected());

        boolean isEnabled = selectedDate != null && !selectedHours.isEmpty();
        nextBtn.setEnabled(isEnabled);
        nextBtn.setBackgroundResource(isEnabled ? R.drawable.bg_next_button_active : R.drawable.bg_next_button_inactive);
        nextBtn.setTextColor(Color.parseColor(isEnabled ? "#FFFFFF" : "#AAAAAA"));
    }
}
