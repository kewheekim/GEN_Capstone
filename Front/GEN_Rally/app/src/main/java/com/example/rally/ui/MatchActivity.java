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
        setContentView(R.layout.activity_match_001);

        Intent prev = getIntent();
        MatchRequestDto userInput = (MatchRequestDto)getIntent().getSerializableExtra("userInput");

        TextView tvDday = findViewById(R.id.tv_dday);
        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvPlace = findViewById(R.id.tv_location);
        TextView tvType = findViewById(R.id.tv_type);

        // 하단 내비게이션
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setSelectedItemId(R.id.navigation_matching);

        LocalDate today = LocalDate.now();
        int dday =(int)ChronoUnit.DAYS.between(today, LocalDate.parse(userInput.getGameDate()));
        tvDday.setText("매칭 마감까지 D-"+dday);

        LocalDate game_date= LocalDate.parse(userInput.getGameDate());
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("M월 d일(E)");
        String formatted_date = game_date.format(formatter);
        tvDate.setText(formatted_date);

        tvTime.setText(userInput.getStartTime()+":00 ~ "+userInput.getEndTime()+":00");

        tvPlace.setText(userInput.getPlace());
        tvType.setText(userInput.getGameType()==0 ? "단식": "복식");

    }
}
