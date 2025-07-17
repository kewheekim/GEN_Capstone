package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

import java.util.ArrayList;

// MAT_AP_006
public class PopupLocationActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 바 제거
        setContentView(R.layout.activity_popup_location);

        ImageButton xBtn = (ImageButton) findViewById(R.id.x_btn);
        Button backBtn = (Button) findViewById(R.id.back_btn);
        Button goBtn = (Button) findViewById(R.id.go_btn);

        TextView tvPlaceName = findViewById(R.id.tv_place_name);
        TextView tvPlaceAddress = findViewById(R.id.tv_place_address);

        // Intent에서 전달된 장소 정보 꺼내기
        Intent prev = getIntent();
        String locationName = prev.getStringExtra("location_name");
        String addressName = prev.getStringExtra("address_name");
        String date            = prev.getStringExtra("date");
        ArrayList<Integer> hours   = prev.getIntegerArrayListExtra("hours");

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
            // → 두 번째 팝업(최종 매칭 확인) PopupConfirmActivity로
            Intent nextIntent = new Intent(PopupLocationActivity.this,
                    PopupConfirmActivity.class);

            nextIntent.putExtra("gameType", prev.getIntExtra("gameType", -1));
            nextIntent.putExtra("gameStyle", prev.getIntExtra("gameStyle", -1));
            nextIntent.putExtra("sameGender", prev.getBooleanExtra("sameGender", false));

            nextIntent.putExtra("date", date);
            nextIntent.putIntegerArrayListExtra("hours", hours);
            nextIntent.putExtra("place_name", locationName);
            nextIntent.putExtra("place_address", addressName);
            nextIntent.putExtra("latitude", prev.getDoubleExtra("lat", 0));
            nextIntent.putExtra("longitude", prev.getDoubleExtra("lng", 0));

            startActivity(nextIntent);
            finish();
        });

    }
}
