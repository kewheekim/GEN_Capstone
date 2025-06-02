package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

import java.util.ArrayList;

public class PopupLocationActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 바 제거
        setContentView(R.layout.activity_popup_location);

        ImageButton xBtn = (ImageButton) findViewById(R.id.x_btn);
        Button backBtn = (Button) findViewById(R.id.back_Btn);
        Button goBtn = (Button) findViewById(R.id.go_Btn);

        TextView tvPlaceName = findViewById(R.id.tv_place_name);
        TextView tvPlaceAddress = findViewById(R.id.tv_place_address);

        // Intent에서 전달된 장소 정보 꺼내기
        Intent intent = getIntent();
        String locationName = intent.getStringExtra("location_name");
        String addressName = intent.getStringExtra("address_name");
        String date            = intent.getStringExtra("date");
        ArrayList<Integer> hours   = intent.getIntegerArrayListExtra("hours");

        if (locationName != null) {
            tvPlaceName.setText(locationName);
        }
        if (addressName != null) {
            tvPlaceAddress.setText(addressName);
        }

        // x 버튼 클릭 시
        xBtn.setOnClickListener(v -> finish());

        // 다시 선택 버튼 클릭 시
        backBtn.setOnClickListener(v -> finish());

        goBtn.setOnClickListener(v -> {
            // → 두 번째 팝업(최종 매칭 확인)으로
            Intent nextIntent = new Intent(PopupLocationActivity.this,
                    PopupConfirmActivity.class);

            nextIntent.putExtra("date", date);
            nextIntent.putIntegerArrayListExtra("hours", hours);
            nextIntent.putExtra("place_name", locationName);
            nextIntent.putExtra("place_address", addressName);

            startActivity(nextIntent);
            finish();
        });

    }
}
