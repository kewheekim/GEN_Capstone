package com.example.rally.ui;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;

public class CandidateNullActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_null);

        // ui
        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvPlaceName    = findViewById(R.id.tv_location);
        TextView tvType = findViewById(R.id.tv_type);
        TextView tvStyle = findViewById(R.id.tv_style);
        Button btnRestart = findViewById(R.id.btn_restart);
        Button btnHome = findViewById(R.id.btn_home);

        Intent prev= getIntent();
        MatchRequestDto userInput = (MatchRequestDto) prev.getSerializableExtra("userInput");
        LocalDate gameDate= LocalDate.parse(userInput.getGameDate());
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("M월 d일");
        String formattedDate = gameDate.format(formatter);
        tvDate.setText(formattedDate);

        tvTime.setText(userInput.getStartTime()+"시 ~ "+ userInput.getEndTime()+"시");
        tvPlaceName.setText(userInput.getPlace());
        tvType.setText(userInput.getGameType()==0? "단식": "복식");
        if(userInput.getGameStyle() == 0)
            tvStyle.setText("상관없어요");
        else if(userInput.getGameStyle() == 1)
            tvStyle.setText("편하게 즐겨요");
        else if(userInput.getGameStyle() == 2)
            tvStyle.setText("열심히 경기해요");

        btnRestart.setOnClickListener(v -> {
            Intent intent = new Intent(CandidateNullActivity.this, MatTypeActivity.class);
            intent.putExtra("from", "CandidateNullActivity");
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener( v -> {
            // 신청내역 DB에 저장 요청하는 코드
            Intent intent = new Intent(CandidateNullActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 뒤로가기 버튼 막기
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 처리x
            }
        });
    }
}
