package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MatchActivity extends AppCompatActivity {
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        Intent prev = getIntent();
        MatchRequestDto userInput = (MatchRequestDto) getIntent().getSerializableExtra("userInput");

        TextView tvDday = findViewById(R.id.tv_dday);
        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvPlace = findViewById(R.id.tv_location);
        TextView tvType = findViewById(R.id.tv_type);

        // 하단 내비게이션
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.navigation_matching);

        LocalDate today = LocalDate.now();
        int dday = (int) ChronoUnit.DAYS.between(today, LocalDate.parse(userInput.getGameDate()));
        tvDday.setText("매칭 마감까지 D-" + dday);

        LocalDate game_date = LocalDate.parse(userInput.getGameDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일(E)");
        String formatted_date = game_date.format(formatter);
        tvDate.setText(formatted_date);

        tvTime.setText(userInput.getStartTime() + ":00 ~ " + userInput.getEndTime() + ":00");

        tvPlace.setText(userInput.getPlace());
        tvType.setText(userInput.getGameType() == 0 ? "단식" : "복식");

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(MatchActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // 전환 애니메이션 제거 (선택)
                return true;
            } else if (id == R.id.navigation_matching) {
                // 현재 화면이므로 아무 동작 없이 true 반환
                return true;
            }
            // 필요하면 다른 메뉴 항목도 여기에 추가 가능
            return false;
        });
    }
}
