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

// MAT_REC_N
public class CandidateNullActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_null);

        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvPlaceName = findViewById(R.id.tv_place);
        TextView tvType = findViewById(R.id.tv_type);
        TextView tvStyle = findViewById(R.id.tv_style);
        Button btnRestart = findViewById(R.id.btn_restart);
        Button btnHome = findViewById(R.id.btn_home);

        Intent prev= getIntent();

        String date = prev.getStringExtra("date");
        String gameType = prev.getStringExtra("gameType");
        String gameStyle = prev.getStringExtra("gameStyle");
        String time = prev.getStringExtra("time");
        String place = prev.getStringExtra("placeName");
        tvDate.setText(date != null ? date : "-");
        tvTime.setText(time != null ? time : "-");
        tvPlaceName.setText(place != null ? place : "-");
        tvType.setText(gameType != null ? gameType : "-");
        tvStyle.setText(gameStyle != null ? gameStyle : "-");

        btnRestart.setOnClickListener(v -> {
            Intent intent = new Intent(CandidateNullActivity.this, MatTypeActivity.class);
            intent.putExtra("from", "CandidateNullActivity");
            startActivity(intent);
            finish();
        });

        btnHome.setOnClickListener( v -> {
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
