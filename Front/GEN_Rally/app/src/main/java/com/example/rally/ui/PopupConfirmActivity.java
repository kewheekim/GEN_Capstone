package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

public class PopupConfirmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_confirm);

        ImageButton xBtn        = findViewById(R.id.x_btn);
        Button backBtn          = findViewById(R.id.back_Btn);
        Button goBtn            = findViewById(R.id.go_Btn);

        TextView tvDate         = findViewById(R.id.tv_date);
        TextView tvTime         = findViewById(R.id.tv_time);
        TextView tvPlaceName    = findViewById(R.id.tv_location);
        TextView tvPlaceAddress = findViewById(R.id.tv_address);

        Intent intent = getIntent();
        String dateStr       = intent.getStringExtra("date");
        ArrayList<Integer> hoursList = intent.getIntegerArrayListExtra("hours");
        String placeName    = intent.getStringExtra("place_name");
        String placeAddress = intent.getStringExtra("place_address");

        if (dateStr != null) {
            // LocalDate로 파싱 (Java 8 이상)
            LocalDate date = LocalDate.parse(dateStr);
            String formattedDate = date.getMonthValue() + "월 " + date.getDayOfMonth() + "일";
            tvDate.setText(formattedDate);
        }
        // 시간 포맷: 선택된 시간 리스트가 있으면 (최소값)시 ~ (최대값)시
        if (hoursList != null && !hoursList.isEmpty()) {
            Collections.sort(hoursList);
            int startHour = hoursList.get(0);
            int endHour   = hoursList.get(hoursList.size() - 1);

            String formattedTime = startHour + "시 ~ " + endHour + "시";
            tvTime.setText(formattedTime);
        }
        if (placeName != null) {
            tvPlaceName.setText(placeName);
        }
        if (placeAddress != null) {
            tvPlaceAddress.setText(placeAddress);
        }

        xBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());

        goBtn.setOnClickListener(v -> {
            Intent nextIntent = new Intent(PopupConfirmActivity.this,
                    LoadingActivity.class);
            startActivity(nextIntent);
            finish();
        });
    }
}
