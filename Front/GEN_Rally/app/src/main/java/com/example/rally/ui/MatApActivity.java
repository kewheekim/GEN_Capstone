package com.example.rally.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rally.R;

public class MatApActivity extends AppCompatActivity {
    private View singleCard,doubleCard;
    private Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_ap_001);

        Toolbar toolbar = findViewById(R.id.include_toolbar);
        Log.d("MatAp", "toolbar is " + toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 뒤로가기
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        nextBtn = findViewById(R.id.next_button);
        singleCard = findViewById(R.id.rectangle_single);
        doubleCard = findViewById(R.id.rectangle_double);

        View.OnClickListener cardClick = v -> {
            boolean isSingle = v.getId() == R.id.rectangle_single;

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
                Intent intent = new Intent(getApplicationContext(),MatApSingleActivity.class);
                startActivity(intent);
            }
        });
    }
}
