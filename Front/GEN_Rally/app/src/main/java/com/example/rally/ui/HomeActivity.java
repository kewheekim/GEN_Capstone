package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

public class HomeActivity  extends AppCompatActivity {
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageButton requestBtn = findViewById(R.id.requestBtn);

        requestBtn.setOnClickListener( r -> {
            Intent intent = new Intent(HomeActivity.this, MatApActivity.class);
            startActivity(intent);
        });
    }
}
