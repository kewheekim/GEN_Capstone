package com.example.rally.ui;

import com.example.rally.R;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.EvaluationCreateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EvaluationActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etCompliment;
    private Button btnNext;

    private CheckBox cbCompliment1, cbCompliment2, cbCompliment3, cbCompliment4, cbCompliment5;
    private CheckBox cbProblem1, cbProblem2, cbProblem3, cbProblem4, cbProblem5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        ratingBar = findViewById(R.id.rating_bar);
        etCompliment = findViewById(R.id.et_compliment);

        cbCompliment1 = findViewById(R.id.cb_compliment1);
        cbCompliment2 = findViewById(R.id.cb_compliment2);
        cbCompliment3 = findViewById(R.id.cb_compliment3);
        cbCompliment4 = findViewById(R.id.cb_compliment4);
        cbCompliment5 = findViewById(R.id.cb_compliment5);
        cbProblem1 = findViewById(R.id.cb_problem1);
        cbProblem2 = findViewById(R.id.cb_problem2);
        cbProblem3 = findViewById(R.id.cb_problem3);
        cbProblem4 = findViewById(R.id.cb_problem4);
        cbProblem5 = findViewById(R.id.cb_problem5);

        btnNext = findViewById(R.id.btn_next);
        btnNext.setEnabled(false);

        // 0.5 단위 입력
        ratingBar.setStepSize(0.5f);

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (!fromUser) return;
            float finalValue = rating < 1.0f ? 1.0f : rating;
            if (finalValue != rating) {
                bar.setRating(finalValue);
                return;
            }
            btnNext.setEnabled(true);
            btnNext.setBackgroundResource(R.drawable.bg_next_button_active);
            btnNext.setTextColor(ContextCompat.getColor(this, R.color.white));
        });

        btnNext.setOnClickListener(v -> {
            btnNext.setEnabled(false);

            long gameId = getIntent().getLongExtra("game_id", 1);
            String subjectUserId = getIntent().getStringExtra("subject_user_id");

            if (gameId == 0L || subjectUserId == null) {
                Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
                subjectUserId = "user003";
                btnNext.setEnabled(true);
            }

            // 최소 1.0 보정
            float r = ratingBar.getRating();
            if (r < 1.0f) {
                r = 1.0f;
                ratingBar.setRating(1.0f);
            }

            EvaluationCreateRequest body = new EvaluationCreateRequest(
                    gameId,
                    subjectUserId,
                    (double) r,
                    etCompliment.getText().toString().trim()
            );

            ApiService api = RetrofitClient
                    .getClient("http://172.19.46.132:8080/")
                    .create(ApiService.class);

            api.createEvaluation(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> resp) {
                    if (resp.isSuccessful()) {
                        Toast.makeText(EvaluationActivity.this, "평가가 저장되었습니다.", Toast.LENGTH_SHORT).show();

                        // 성공 시 다음 화면으로 이동
                        Intent intentToGameResult = new Intent(EvaluationActivity.this, GameResultActivity.class);
                        startActivity(intentToGameResult);
                        finish();
                    } else {
                        Toast.makeText(EvaluationActivity.this, "저장 실패: " + resp.code(), Toast.LENGTH_SHORT).show();
                        btnNext.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EvaluationActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    btnNext.setEnabled(true);
                }
            });
        });
    }
}