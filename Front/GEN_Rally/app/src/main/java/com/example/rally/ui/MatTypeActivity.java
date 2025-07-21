package com.example.rally.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rally.R;

// MAT_AP_001
public class MatTypeActivity extends AppCompatActivity {
    private View singleCard,doubleCard;
    private Button nextBtn;
    private int gameType=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_type);

        Toolbar toolbar = findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        String from = getIntent().getStringExtra("from");
        // 뒤로가기
        if ("CandidateNullActivity".equals(from)) {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    // 아무 동작도 하지 않음 → 뒤로가기 비활성화
                }
            });
        }
        else {
            findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
        }

        nextBtn = findViewById(R.id.btn_next);
        singleCard = findViewById(R.id.rectangle_single);
        doubleCard = findViewById(R.id.rectangle_double);

        View.OnClickListener cardClick = v -> {
            boolean isSingle = v.getId() == R.id.rectangle_single;
            gameType= isSingle? 0:1;

            singleCard.setSelected(isSingle);
            doubleCard.setSelected(!isSingle);

            nextBtn.setEnabled(true);
            nextBtn.setTextColor(Color.parseColor("#FFFFFF"));
            nextBtn.setBackgroundResource(R.drawable.bg_next_button_active);
        };

        singleCard.setOnClickListener(cardClick);
       // doubleCard.setOnClickListener(cardClick);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), MatConditionActivity.class);
                intent.putExtra("gameType", gameType);
                startActivity(intent);
            }
        });
    }
}
