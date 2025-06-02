package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

public class PopupConfirmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_confirm);

        // 1) 뷰 바인딩
        ImageButton xBtn        = findViewById(R.id.x_btn);
        Button backBtn          = findViewById(R.id.back_Btn);
        Button goBtn            = findViewById(R.id.go_Btn);

        TextView tvDate         = findViewById(R.id.tv_date);
        TextView tvTime         = findViewById(R.id.tv_time);
        TextView tvPlaceName    = findViewById(R.id.tv_place_name);
        TextView tvPlaceAddress = findViewById(R.id.tv_place_address);
        // TextView tvStyle, tvStyleDetail 필요하면 바인딩

        // 2) Intent에서 전달된 값 꺼내기
        Intent intent = getIntent();
        String date         = intent.getStringExtra("date");
        String time         = intent.getStringExtra("time");
        String placeName    = intent.getStringExtra("place_name");
        String placeAddress = intent.getStringExtra("place_address");
        // 스타일 정보가 있으면 같이 꺼내기 (지금은 제외)

        // 3) 가져온 값을 화면에 세팅
        if (date != null) {
            tvDate.setText(date);
        }
        if (time != null) {
            tvTime.setText(time);
        }
        if (placeName != null) {
            tvPlaceName.setText(placeName);
        }
        if (placeAddress != null) {
            tvPlaceAddress.setText(placeAddress);
        }
        // 스타일 처리할 때는 tvStyle.setText(...), tvStyleDetail.setText(...) 등

        // 4) 버튼 동작
        xBtn.setOnClickListener(v -> finish());
        backBtn.setOnClickListener(v -> finish());

        goBtn.setOnClickListener(v -> {
            // 최종 확인하면 로딩 화면으로 이동
            Intent nextIntent = new Intent(PopupConfirmActivity.this,
                    LoadingActivity.class);
            startActivity(nextIntent);
            finish();
        });
    }
}
