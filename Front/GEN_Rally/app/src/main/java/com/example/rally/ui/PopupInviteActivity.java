package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

// MAT_AP_S_005
public class PopupInviteActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_invite);

        TextView info = findViewById(R.id.tv_title);
        ImageButton xButton = findViewById(R.id.btn_x);
        Button backButton = findViewById(R.id.btn_back);
        Button goButton = findViewById(R.id.go_btn);

        Intent prev = getIntent();
        String name = prev.getStringExtra("partnerName");
        info.setText("'" + name + "' 님에게\n매칭 요청을 보내시겠어요?");

        // X 버튼: 닫기
        xButton.setOnClickListener(v -> finish());
        // 돌아가기 버튼
        backButton.setOnClickListener(v -> finish());
        // 보내기 버튼
        goButton.setOnClickListener(v -> {
            // request invite api 요청
        });
    }
}
