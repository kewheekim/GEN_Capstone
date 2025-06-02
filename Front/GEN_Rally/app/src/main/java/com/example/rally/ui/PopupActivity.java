package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

public class PopupActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 바 제거
        setContentView(R.layout.activity_popup);

        ImageButton xBtn = (ImageButton) findViewById(R.id.x_btn);
        Button backBtn = (Button) findViewById(R.id.back_Btn);
        Button goBtn = (Button) findViewById(R.id.go_Btn);

        // 닫기 아이콘(×) 클릭 시
        xBtn.setOnClickListener(v -> finish());

        // “다시 선택” 버튼 클릭 시
        backBtn.setOnClickListener(v -> finish());

        goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PopupActivity.this,LoadingActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
