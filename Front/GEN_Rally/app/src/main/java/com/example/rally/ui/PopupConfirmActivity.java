package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

// MAT_AP_007
public class PopupConfirmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_match_confirm);

        ImageButton xBtn        = findViewById(R.id.btn_x);
        Button backBtn          = findViewById(R.id.btn_back);
        Button goBtn            = findViewById(R.id.go_btn);

        TextView tvDate         = findViewById(R.id.tv_date);
        TextView tvTime         = findViewById(R.id.tv_time);
        TextView tvPlaceName    = findViewById(R.id.tv_location);

        TextView tvType = findViewById(R.id.tv_type);
        TextView tvStyle = findViewById(R.id.tv_style);

        Intent prev = getIntent();
        LocalDate date= LocalDate.parse(prev.getStringExtra("date"));
        ArrayList<Integer> hoursList = prev.getIntegerArrayListExtra("hours");
        String place = prev.getStringExtra("place_name");
        String placeAddress = prev.getStringExtra("place_address");
        String placeName;

        if (placeAddress != null && "현재 위치".equals(place)) {
            String[] parts = placeAddress.split("\\s+");
            if (parts.length >= 4) {
                placeName = parts[2] + " " + parts[3];  // "노원구 공릉동"
            } else {
                placeName = placeAddress;  // fallback
            }
        } else {
            placeName = place;
        }

        int gameType = prev.getIntExtra("gameType", -1);
        int gameStyle = prev.getIntExtra("gameStyle", -1);
        boolean sameGender=prev.getBooleanExtra("sameGender", false);
        int startHour;
        int endHour;
        double latitude= prev.getDoubleExtra("latitude", -1);
        double longitude= prev.getDoubleExtra("longitude", -1);

        if (date != null) {
            // LocalDate로 파싱 (Java 8 이상)
            String formattedDate = date.getMonthValue() + "월 " + date.getDayOfMonth() + "일";
            tvDate.setText(formattedDate);
        }
        // 시간 포맷: 선택된 시간 리스트가 있으면 (최소값)시 ~ (최대값)시
        if (hoursList != null && !hoursList.isEmpty()) {
            Collections.sort(hoursList);
            startHour = hoursList.get(0);
            endHour   = hoursList.get(hoursList.size()-1)+1;
            String formattedTime = startHour + "시 ~ " + endHour + "시";
            tvTime.setText(formattedTime);
        } else {
            endHour = 0;
            startHour = 0;
        }
        if (placeName != null) {
            tvPlaceName.setText(placeName);
        }
        if (gameType ==0) {
            tvType.setText("단식");
        }
        else if (gameType == 1)
            tvType.setText("복식");
        if(gameStyle == 0)
            tvStyle.setText("상관없어요");
        else if(gameStyle == 1)
            tvStyle.setText("편하게 즐겨요");
        else if(gameStyle == 2)
            tvStyle.setText("열심히 경기해요");

        xBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());

        // 확인버튼 클릭 시 api/match/request API 호출
        goBtn.setOnClickListener(v -> {
            MatchRequestDto matchRequest = new MatchRequestDto("user001", gameType, gameStyle,
                    sameGender, date, startHour, endHour, placeName, latitude, longitude);
            Intent intent = new Intent(PopupConfirmActivity.this, LoadingActivity.class);
            intent.putExtra("matchRequest", matchRequest);
            startActivity(intent);
        });
    }
}