package com.example.rally.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;

public class LoadingActivity extends AppCompatActivity {
    private TextView dot1, dot2, dot3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 점 세개
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        startDotAnimationViaXML();
    }

    private void startDotAnimationViaXML() {
        // 애니메이터 설정
        AnimatorSet animSet1 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.dot_combined);
        AnimatorSet animSet2 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.dot_combined);
        AnimatorSet animSet3 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.dot_combined);

        // 각각 움직일 대상 (점) 설정
        animSet1.setTarget(dot1);
        animSet2.setTarget(dot2);
        animSet3.setTarget(dot3);

        // dot1은 지체 없이 dot2는 300ms 지연, dot3는 600ms 지연 후 시작
        animSet1.setStartDelay(0);
        animSet2.setStartDelay(300);
        animSet3.setStartDelay(600);

        // 세 개를 동시에 play, 끝나면 다시 반복
        AnimatorSet allDots = new AnimatorSet();
        allDots.playTogether(animSet1, animSet2, animSet3);

        // 끝난 뒤 다시 처음부터 반복하도록 리스너 추가
        allDots.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                allDots.start();
            }
        });
        allDots.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dot1.animate().cancel();
        dot2.animate().cancel();
        dot3.animate().cancel();
    }
}
