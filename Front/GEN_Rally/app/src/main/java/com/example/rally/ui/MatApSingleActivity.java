package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rally.R;

import java.util.ArrayList;
import java.util.List;

public class MatApSingleActivity extends AppCompatActivity {
    private static final String[] QUESTIONS = {
            "원하는 경기 스타일을 선택해주세요",
            "원하는 경기 상대 성별을 선택해주세요"
    };
    private static final String[][] OPTIONS = {
            {"상관없어요", "편하게 즐겨요", "열심히 경기해요"},
            {"상관없어요", "나와 같은 성별의\n상대와 경기하고 싶어요"}
    };

    private int currentIndex = 0;
    private final List<String> answers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        // 첫 질문 띄우기
        showQuestion(currentIndex);
    }

    private void showQuestion(int idx) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,
                        MatApSingleFragment.newInstance(
                                QUESTIONS[idx],
                                OPTIONS[idx]
                        ))
                .commit();
    }

    public void goToNextQuestion(String selectedAnswer) {
        answers.add(selectedAnswer);

        currentIndex++;
        if (currentIndex < QUESTIONS.length) {
            // 아직 남은 질문이 있으면 다음 질문으로
            showQuestion(currentIndex);
        } else {
            // 마지막 질문 뒤에 수행할 액션 (예: 다음 액티비티로 이동)
            Intent intent = new Intent(this, MatchActivity.class);
            intent.putStringArrayListExtra("answers",
                    new ArrayList<>(answers));
            startActivity(intent);
            finish();
        }
    }
}
