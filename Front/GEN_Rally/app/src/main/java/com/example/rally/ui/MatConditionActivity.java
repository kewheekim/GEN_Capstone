package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rally.R;

// MAT_AP_S_001, MAT_AP_S_002, MAT_AP_D_001, MAT_AP_D_002
public class MatConditionActivity extends AppCompatActivity {
    private static final String[] QUESTIONS = {
            "원하는 경기 스타일을 선택해주세요",
            "원하는 경기 상대 성별을 선택해주세요"
    };
    private static final String[][] OPTIONS = {
            {"상관없어요", "편하게 즐겨요", "열심히 경기해요"},
            {"상관없어요", "나와 같은 성별의\n상대와 경기하고 싶어요"}
    };

    private int currentIndex = 0;
    private int gameStyle=-1;
    private boolean sameGender=false;
    private int gameType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_condition);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        gameType= getIntent().getIntExtra("gameType", -1);

        // 첫 질문 띄우기
        showQuestion(currentIndex);
    }

    private void showQuestion(int idx) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,
                        MatConditionFragment.newInstance(
                                QUESTIONS[idx],
                                OPTIONS[idx]
                        ))
                .addToBackStack(null)
                .commit();
    }

    public void goToNextQuestion(String selectedAnswer) {
        int selectedIndex = -1;
        String[] options = OPTIONS[currentIndex];
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(selectedAnswer)) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1) return;

        if (currentIndex == 0) {
            // 경기 스타일 선택 (0~2)
            gameStyle=selectedIndex;
        } else if (currentIndex == 1) {
            // 성별 조건 선택 (0 = 상관없음, 1 = 같은 성별만)
            sameGender = (selectedIndex == 1);
        }

        currentIndex++;
        if (currentIndex < QUESTIONS.length) {
            showQuestion(currentIndex);
        } else {
            Intent intent = new Intent(this, SetTimeActivity.class);
            intent.putExtra("gameType", gameType);
            intent.putExtra("gameStyle", gameStyle);
            intent.putExtra("sameGender",sameGender);
            startActivity(intent);
            finish();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();  // 액티비티 종료
        }
    }
}
