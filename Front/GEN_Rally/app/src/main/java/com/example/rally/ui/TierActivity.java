package com.example.rally.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rally.R;

import javax.annotation.Nullable;

// SIG_012
public class TierActivity extends AppCompatActivity {

    private TextView tvName, tvTier;
    private ImageView ivTier;
    private Button btnNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tier);

        tvName = findViewById(R.id.tv_name);
        tvTier = findViewById(R.id.tv_tier);
        ivTier = findViewById(R.id.iv_tier);
        btnNext = findViewById(R.id.btn_next);


    }
}
