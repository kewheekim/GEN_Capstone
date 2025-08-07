package com.example.rally.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.rally.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;


//SIG_006~SIG010
public class SurveyActivity extends AppCompatActivity {
    private static final String[] QUESTIONS = {
            "서브를 넣을 때, \n원하는 대로 안정적으로 넣을 수 있나요?",
            "셔틀콕이 날아올 때, 상대의 코트로\n정확하게 보내는 것이 어느 정도 가능한가요?",
            "셔틀콕이 갑자기 다른 방향으로 날아올 때,\n발이 얼마나 잘 따라가나요?",
            "최근 6개월 동안 점수를 세며\n배드민턴을 한 경험이 얼마나 있었나요?",
            "구력을\n선택해주세요"
    };
    private static final String[][] OPTIONS = {
            {       "서브를 넣을 때 네트에 자주 걸리거나,\n상대 코트로 제대로 넘기기 <green>어렵고 불안정해요</green>",
                    "서브를 넘길 수는 있지만\n높이, 거리, 방향이 들쑥날쑥하고 <green>실수가 많아요</green>",
                    "<green>대부분 서브를 넣을 수 있고,</green>\n롱서브/숏서브 구분도 어느 정도 가능해요",
                    "상대 위치나 상황에 따라 롱/숏서브를\n<green>안정적으로 조절할 수 있어요</green>",
                    "서브로 상대를 흔들거나 <green>전략적으로</green>\n위치를 노려 <green>서브할 수 있어요</green>"
            },
            {       "넘기기도 벅차서\n방향 조절은 <green>거의 불가능했어요</green>",
                    "넘기긴 했지만\n상대 코트 중 <green>어디로 가는지는 몰라요</green>",
                    "<green>코트 앞/중/뒤</green> 정도는\n구분해서 <green>보낼 수 있어요</green>",
                    "내가 의도한 방향으로\n<green>꽤 정확히 보낼 수 있어요</green>",
                    "상대의 위치나 빈 공간을 보고 <green>전략적으로</green>\n원하는 곳에 <green>셔틀콕을 보낼 수 있어요</green>"
            },
            {
                    "셔틀콕 방향이 바뀌면 <green>발이 잘 움직이지 않아,</green>\n몸이 따라가기 전에 손만 뻗게 되는 경우가 많아요",
                    "셔틀콕이 오면 이동하려 하지만,\n발이 느려서 <green>항상 한 박자 늦게</green> 반응해요",
                    "<green>방향 전환과 이동은 되지만,</green> 발의 움직임이 매끄럽지\n않아서 치기 전 자세가 <green>불안정할 때가 있어요</green>", // TODO: 글자 수정 필요, 넘침
                    "대부분의 상황에서 셔틀콕 <green>방향에 빠르게 반응하고</green>,\n코트 안에서 부드럽고 안정적으로 움직일 수 있어요",
                    "셔틀콕의 방향을 미리 예측하고 자연스럽게 <green>발이</green>\n<green>먼저 움직이며, 경기 흐름을 주도</green>할 수 있어요"
            },
            {
                "0회", "1~4회", "5~9회", "10~19회", "20회 이상"
            },
            {
                "입문", "1년", "2년", "3년", "4년", "5년", "6년 이상"
            }
    };

    private int currentIndex = 0;
    private int canService = -1;
    private int canPass = -1;
    private int footWork = -1;
    private int count = -1;
    private int howLong = -1;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(QUESTIONS.length);           // 전체 문항 수
        progressBar.setProgress(1);

        // 첫 질문 띄우기
        showQuestion(currentIndex);
    }

    private void showQuestion(int idx) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,
                        SurveyFragment.newInstance(
                                QUESTIONS[idx],
                                OPTIONS[idx]
                        ))
                .addToBackStack(null)
                .commit();
    }

    public void goToNextQuestion(int selectedIndex) {
        switch (currentIndex) {
            case 0: canService = selectedIndex; break;
            case 1: canPass    = selectedIndex; break;
            case 2: footWork   = selectedIndex; break;
            case 3: count      = selectedIndex; break;
            case 4: howLong    = selectedIndex; break;
        }

        currentIndex++;
        progressBar.setProgress(currentIndex + 1);

        if (currentIndex < QUESTIONS.length) {
            showQuestion(currentIndex);
        } else {
            // 모든 질문 완료 시 결과 화면으로
            Intent intent = new Intent(this, TierActivity.class);
            intent.putExtra("canService", canService);
            intent.putExtra("canPass",    canPass);
            intent.putExtra("footWork",   footWork);
            intent.putExtra("count",      count);
            intent.putExtra("howLong",    howLong);
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
