package com.example.rally.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class TutorialActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private int page = 0;

    private static class Page {
        final String t, d; final int img; final float bias; // bias: 이미지 위치 조정용
        Page(String t, String d, int img, float bias){
            this.t=t; this.d=d; this.img=img; this.bias=bias;
        }
    }

    private final Page[] PAGES = new Page[]{
            new Page("쾌적한\n구장 이용을 위해", "구장 전용 실내화를 준비!\n함께 깨끗한 구장을 만들어요",
                    R.drawable.image_tut001, 0.6f),
            new Page("서로 존중하는\n경기를 위해","‘잘 부탁드립니다’, ‘수고하셨습니다’\n경기 전•후 모두에게 인사는 기본이에요",
                    R.drawable.image_tut002,0.8f),
            new Page("원활한\n경기를 위해","서브는 상대의 준비를\n꼭 확인하고 넣어요",
                    R.drawable.image_tut003,0.8f),
            new Page("배려 있는\n경기를 위해","상대를 셔틀콕으로 맞혔을 경우에는\n정중히 사과해요",
                    R.drawable.image_tut004,0.8f),
            new Page("깔끔한\n경기를 위해","인/아웃 판정은 빠르고 정확하게!\n애매할 땐 상대에게 유리하게 판정해요",
                    R.drawable.image_tut005,0.7f)
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(PAGES.length);
        updateProgress();

        showPage(page);

        Button btnNext = findViewById(R.id.btn_next);
        btnNext.setEnabled(true);
        btnNext.setTextColor(getColor(R.color.white));
        btnNext.setOnClickListener(v -> {
            if (page < PAGES.length){
                page++;
                showPage(page);
                updateProgress();
            }else{
                finish();
            }
        });

    }

    private void showPage(int idx) {
        Page p = PAGES[idx];
        Fragment f = TutorialFragment.newInstance(p.t, p.d, p.img, p.bias);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        android.R.anim.slide_in_left, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, f)
                .commit();
    }

    private void updateProgress() {
        progressBar.setProgress(page + 1);
    }
}
