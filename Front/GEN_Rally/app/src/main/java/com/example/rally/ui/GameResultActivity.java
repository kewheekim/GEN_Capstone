package com.example.rally.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.GameHealthDto;
import com.example.rally.dto.GameInfoDto;
import com.example.rally.dto.GameResultResponse;
import com.example.rally.dto.SetResultDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class GameResultActivity extends AppCompatActivity {

    private ApiService apiService;

    private TextView tvDate, tvPlace;
    private ImageView ivOpponentProfile, ivUserProfile;
    private TextView tvOpponentName, tvUserName;
    private TextView tvOpponentScore, tvUserScore, tvTime;

    private TextView tvOpponentScore1, tvMyScore1, tvTime1;
    private TextView tvOpponentScore2, tvMyScore2, tvTime2;
    private TextView tvOpponentScore3, tvMyScore3, tvTime3;
    private ScoreBarView sbOpponent1, sbUser1, sbOpponent2, sbUser2, sbOpponent3, sbUser3;
    private HeartRateView hrGraph;

    private TextView tvHrMax, tvHrMin, tvSteps, tvCalorie, tvCompliment;
    View set3Group;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_result);

        long gameId = getIntent().getLongExtra("gameId", -1L);

        Log.d("GameResultDebug", "Activity 시작 - 받은 gameId: " + gameId);

        initViews();

        apiService = RetrofitClient.getSecureClient(this, BuildConfig.API_BASE_URL).create(ApiService.class);

        if (gameId > 0) {
            loadGameResult(gameId);
        } else {
            Toast.makeText(this, "잘못된 경기 정보입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvDate = findViewById(R.id.tv_date);
        tvPlace = findViewById(R.id.tv_place);
        ivOpponentProfile = findViewById(R.id.iv_opponent_profile);
        ivUserProfile = findViewById(R.id.iv_user_profile);
        tvOpponentName = findViewById(R.id.tv_opponent_name);
        tvUserName = findViewById(R.id.tv_user_name);
        tvOpponentScore = findViewById(R.id.tv_opponent_score);
        tvUserScore = findViewById(R.id.tv_user_score);
        tvTime = findViewById(R.id.tv_time);

        tvOpponentScore1 = findViewById(R.id.tv_opponent_score1);
        tvMyScore1 = findViewById(R.id.tv_my_score1);
        tvTime1 = findViewById(R.id.tv_time1);

        tvOpponentScore2 = findViewById(R.id.tv_opponent_score2);
        tvMyScore2 = findViewById(R.id.tv_my_score2);
        tvTime2 = findViewById(R.id.tv_time2);

        tvOpponentScore3 = findViewById(R.id.tv_opponent_score3);
        tvMyScore3 = findViewById(R.id.tv_my_score3);
        tvTime3 = findViewById(R.id.tv_time3);

        sbOpponent1 = findViewById(R.id.sb_opponent1);
        sbUser1 = findViewById(R.id.sb_user1);
        sbOpponent2 = findViewById(R.id.sb_opponent2);
        sbUser2 = findViewById(R.id.sb_user2);
        sbOpponent3 = findViewById(R.id.sb_opponent3);
        sbUser3 = findViewById(R.id.sb_user3);
        set3Group = findViewById(R.id.layout_set3);

        hrGraph = findViewById(R.id.hr_graph);
        tvHrMax = findViewById(R.id.tv_hr_max);
        tvHrMin = findViewById(R.id.tv_hr_min);
        tvSteps = findViewById(R.id.tv_steps);
        tvCalorie = findViewById(R.id.tv_calorie);
        tvCompliment = findViewById(R.id.tv_compliment);
    }

    private void loadGameResult(long gameId) {
        Log.d("GameResultDebug", "API 호출 시작 - gameId: " + gameId);

        apiService.getGameResult(gameId).enqueue(new Callback<GameResultResponse>() {
            @Override
            public void onResponse(Call<GameResultResponse> call, Response<GameResultResponse> response) {
                Log.d("GameResultDebug", "응답 받음 - isSuccessful: " + response.isSuccessful()
                        + ", code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("GameResultDebug", "데이터 바인딩 시작");
                    bindGameResult(response.body());
                } else {
                    Log.e("GameResultDebug", "실패 응답 - code: " + response.code()
                            + ", message: " + response.message());
                    Toast.makeText(GameResultActivity.this, "경기 결과를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GameResultResponse> call, Throwable t) {
                Toast.makeText(GameResultActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindGameResult(GameResultResponse dto) {
        GameInfoDto g = dto.game;
        GameHealthDto h = dto.health;
        String comment = dto.comment;
        boolean hasSet3 = false;

        tvDate.setText(g.dateText);
        tvPlace.setText(g.place);
        tvOpponentName.setText(g.opponentName);
        tvUserName.setText(g.myName);

        tvOpponentScore.setText(String.valueOf(g.opponentSetScore));
        tvUserScore.setText(String.valueOf(g.mySetScore));
        tvTime.setText(g.totalDuration);

        // 상대 프로필
        try {
            if (g.opponentProfileUrl != null && !g.opponentProfileUrl.isEmpty()) {
                int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
                Glide.with(ivOpponentProfile.getContext())
                        .load(g.opponentProfileUrl)
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_default_profile)
                        .centerCrop()
                        .override(sizePx, sizePx)
                        .into(ivOpponentProfile);
            } else {
                ivOpponentProfile.setImageResource(R.drawable.ic_default_profile);
            }
        } catch (Throwable t) {
            ivOpponentProfile.setImageResource(R.drawable.ic_default_profile);
        }
        // 사용자 프로필
        try {
            if (g.myProfileUrl != null && !g.myProfileUrl.isEmpty()) {
                int sizePx = (int) (48 * getResources().getDisplayMetrics().density);
                Glide.with(ivUserProfile.getContext())
                        .load(g.myProfileUrl)
                        .placeholder(R.drawable.ic_default_profile)
                        .error(R.drawable.ic_default_profile)
                        .centerCrop()
                        .override(sizePx, sizePx)
                        .into(ivUserProfile);
            } else {
                ivUserProfile.setImageResource(R.drawable.ic_default_profile);
            }
        } catch (Throwable t) {
             ivUserProfile.setImageResource(R.drawable.ic_default_profile);
        }

        if (dto.sets != null) {
            for (SetResultDto s : dto.sets) {
                int set = s.setNumber;
                if (set == 1) {
                    // 1세트 바인딩
                    tvOpponentScore1.setText(String.valueOf(s.opponentScore));
                    tvMyScore1.setText(String.valueOf(s.myScore));
                    tvTime1.setText(s.time);
                    sbOpponent1.setValue(s.opponentScore);
                    sbUser1.setValue(s.myScore);

                } else if (set == 2) {
                    // 2세트 바인딩
                    tvOpponentScore2.setText(String.valueOf(s.opponentScore));
                    tvMyScore2.setText(String.valueOf(s.myScore));
                    tvTime2.setText(s.time);
                    sbOpponent2.setValue(s.opponentScore);
                    sbUser2.setValue(s.myScore);

                } else if (set == 3) {
                    hasSet3 = true;

                    // 3세트 바인딩
                    tvOpponentScore3.setText(String.valueOf(s.opponentScore));
                    tvMyScore3.setText(String.valueOf(s.myScore));
                    tvTime3.setText(s.time);
                    sbOpponent3.setValue(s.opponentScore);
                    sbUser3.setValue(s.myScore);
                }
            }
        }
        // 3세트 없으면 GONE
        if (set3Group != null) {
            set3Group.setVisibility(hasSet3 ? View.VISIBLE : View.GONE);
        }

        // 헬스데이터
        if (h != null) {
            Log.d("HR_DEBUG", "seriesHr = " + h.seriesHr);
            Log.d("HR_DEBUG", "seriesHr size = " + (h.seriesHr != null ? h.seriesHr.size() : -1));
            if (h.maxHr != null) tvHrMax.setText(String.valueOf(h.maxHr));
            if (h.minHr != null) tvHrMin.setText("/ " + h.minHr);
            if (h.steps != null) tvSteps.setText(String.format("%,d", h.steps));
            if (h.calories != null) tvCalorie.setText(String.valueOf(h.calories));

            if (h.seriesHr != null && !h.seriesHr.isEmpty()) {
                List<HeartRateView.HeartSample> list = new ArrayList<>();
                for(GameHealthDto.HeartSampleDto src : h.seriesHr) {
                    list.add(new HeartRateView.HeartSample(src.bpm, src.epochMs));
                }
                hrGraph.setVisibility(View.VISIBLE);
                hrGraph.setHeartSeries(list);
            } else {
                hrGraph.setVisibility(View.GONE);
            }
        }

        // 칭찬 코멘트
        if (comment != null && !comment.isEmpty()) {
            tvCompliment.setText(comment);
        } else {
            tvCompliment.setText("아직 등록된 칭찬이 없어요.");
        }
    }
}