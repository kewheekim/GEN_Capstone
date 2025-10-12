package com.example.rally.ui;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rally.R;

public class GameFinishActivity extends AppCompatActivity {

    ImageView ivFinishGif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finish);

        // gif 불러오기
        ivFinishGif = findViewById(R.id.iv_finish_gif);
        Glide.with(this).asGif().load(R.drawable.game_finish_gif)
                .into(ivFinishGif);
    }
}