package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchRequestDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;

// MAT_AP_007
public class PopupConfirmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_match_confirm);

        ImageButton xBtn = findViewById(R.id.btn_x);
        Button backBtn = findViewById(R.id.btn_back);
        Button goBtn = findViewById(R.id.go_btn);

        TextView tvDate = findViewById(R.id.tv_date);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvPlaceName = findViewById(R.id.tv_place);

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
        double latitude= prev.getDoubleExtra("latitude", -1);
        double longitude= prev.getDoubleExtra("longitude", -1);
        int startHour;
        int endHour;

        String dateLabel = null;
        String timeLabel = null;
        String placeLabel = null;
        String typeLabel = null;
        String styleLabel = null;

        if (date != null) {
           dateLabel = date.getMonthValue() + "월 " + date.getDayOfMonth() + "일";
            tvDate.setText(dateLabel);
        }
        // 시간 포맷
        if (hoursList != null && !hoursList.isEmpty()) {
            Collections.sort(hoursList);
            startHour = hoursList.get(0);
            endHour   = hoursList.get(hoursList.size()-1)+1;
            timeLabel = startHour + ":00 ~ " + endHour + ":00";
            tvTime.setText(timeLabel);
        } else {
            endHour = 0;
            startHour = 0;
        }
        if (placeName != null) {
            placeLabel = placeName;
            tvPlaceName.setText(placeLabel);
        }
        if (gameType ==0) {
           typeLabel  = "단식";
        }
        else if (gameType == 1){
            typeLabel  = "복식";
        }
        if (typeLabel != null) {
            tvType.setText(typeLabel);
        }
        if(gameStyle == 0)
            styleLabel = "상관없어요";
        else if(gameStyle == 1)
            styleLabel = "편하게 즐겨요";
        else if(gameStyle == 2)
            styleLabel = "열심히 경기해요";
        if (styleLabel != null)
            tvStyle.setText(styleLabel);

        xBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());

        final String finalDateLabel  = dateLabel;
        final String finalTimeLabel  = timeLabel;
        final String finalPlaceLabel = placeLabel;
        final String finalTypeLabel  = typeLabel;
        final String finalStyleLabel = styleLabel;

        // 확인버튼 클릭 시 api/match/request API 호출
        goBtn.setOnClickListener(v -> {
            MatchRequestDto matchRequest = new MatchRequestDto(
                    gameType,
                    gameStyle,
                    sameGender,
                    date,
                    startHour,
                    endHour,
                    placeName,
                    latitude,
                    longitude
            );
            goBtn.setEnabled(false);

            // /api/match/request 호출
            ApiService api = RetrofitClient.getSecureClient(PopupConfirmActivity.this, BuildConfig.API_BASE_URL).create(ApiService.class);
            Call<Long> requestCall = api.requestMatch(matchRequest);
            requestCall.enqueue(new retrofit2.Callback<Long>() {
                @Override
                public void onResponse(Call<Long> call, retrofit2.Response<Long> response) {
                    goBtn.setEnabled(true);

                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(PopupConfirmActivity.this, "매칭 신청에 실패했습니다", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Long requestId = response.body();

                    Intent intent = new Intent(PopupConfirmActivity.this, LoadingActivity.class);
                    intent.putExtra("requestId", requestId);
                    intent.putExtra("date", finalDateLabel);
                    intent.putExtra("gameType", finalTypeLabel);
                    intent.putExtra("gameStyle", finalStyleLabel);
                    intent.putExtra("time", finalTimeLabel);
                    intent.putExtra("placeName", finalPlaceLabel);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<Long> call, Throwable t) {
                    goBtn.setEnabled(true);
                    android.widget.Toast.makeText(PopupConfirmActivity.this,
                            "네트워크 오류로 매칭 신청에 실패했어요.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}