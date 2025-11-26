package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.websocket.RealtimeClient;
import com.example.rally.api.websocket.WsRealtimeClient;
import com.example.rally.viewmodel.Player;
import com.example.rally.viewmodel.ScoreViewModel;

// SCO_002, SCO_003
public class ScoreRecordActivity extends AppCompatActivity {
    private ScoreViewModel viewModel;
    private RealtimeClient client;
    private Long gameId;

    private TextView tvSetNumber, tvOpponentName, tvUserName,
            tvOpponentScore, tvUserScore, tvOpponentSets, tvUserSets, tvTimer, tvUndo, tvPause;
    private ImageView ivServeLeft, ivServeRight;
    ImageButton btnBack;
    private Button btnScore;

    private int setNumber;
    private boolean isSetFinished = false;
    private Player nextFirstServer = Player.USER1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_record);

        viewModel = new ViewModelProvider(this).get(ScoreViewModel.class);

        bindViews();
        setupObservers();
        setupClicks();

        Intent intent = getIntent();
        gameId = intent.getLongExtra("gameId", 123L);
        setNumber = intent.getIntExtra("setNumber", 1);
        int opponentSets = intent.getIntExtra("opponentSets", 0);
        int userSets = intent.getIntExtra("userSets", 0);
        String nextFirst = intent.getStringExtra("nextFirstServer");
        this.nextFirstServer = nextFirst != null ? Player.valueOf(nextFirst) : Player.USER1;
        boolean isUser1 = intent.getBooleanExtra("localIsUser1", true);

        viewModel.initSets(userSets, opponentSets);
        viewModel.initPlayer(isUser1);
        Player firstServer = (setNumber == 1) ? Player.USER1 : nextFirstServer; // 1세트 서브 시작은 user1
        viewModel.prepareSet(setNumber, firstServer);

        // web socket
        String baseUrl = BuildConfig.API_BASE_URL;
        baseUrl = baseUrl.replace("http://", "").replace("https://", "").replaceAll("/$", "");
        String url = "ws://" + baseUrl + "/ws-score?gameId=" + gameId;
        client = new WsRealtimeClient(url);
        client.subscribe("/topic/game."+gameId, json -> {
            runOnUiThread(() -> {
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(json);
                    String type = obj.optString("type", "");
                    org.json.JSONObject p = obj.optJSONObject("payload");

                    viewModel.applyIncoming(json);  // viewModel 적용, ui 갱신

                    if ("game_finish".equals(type) && p != null) {
                        String winner = p.optString("winner", "");
                        org.json.JSONArray sets = p.optJSONArray("sets");
                        long totalElapsedSec = p.optLong("totalElapsedSec", 0L);

                        Intent i = new Intent(ScoreRecordActivity.this, GameFinishActivity.class);
                        i.putExtra("gameId", gameId);
                        i.putExtra("winner", winner);
                        i.putExtra("sets_json", sets != null ? sets.toString() : "[]");
                        i.putExtra("totalElapsedSec", totalElapsedSec);

                        i.putExtra("user1Name", tvUserName.getText().toString());
                        i.putExtra("user2Name", tvOpponentName.getText().toString());

                        startActivity(i);
                        // 현재 스코어 기록 화면은 끝내고 결과 화면으로 넘기고 싶으면 finish()
                        finish();
                        return;
                    }
                } catch (Throwable t) {
                    Log.e("ScoreRecordActivity", "game_finsh: ws parse error", t);
                }
            });
        });
        client.connect();

        tvOpponentName.setText("상대");
        tvUserName.setText("나");
        btnScore.setText("경기 시작");
        btnScore.setEnabled(false);
        isSetFinished = true;    // 대기
        tvTimer.setText("00:00:00");
        btnBack.setColorFilter(ContextCompat.getColor(this, R.color.white));
        updateButtonsForState();
    }

    private void bindViews() {
        tvSetNumber = findViewById(R.id.tv_set_number);
        tvOpponentName = findViewById(R.id.tv_opponent_name);
        tvUserName = findViewById(R.id.tv_user_name);
        tvOpponentScore = findViewById(R.id.tv_opponent_score);
        tvUserScore = findViewById(R.id.tv_user_score);
        tvOpponentSets = findViewById(R.id.tv_opponent_sets);
        tvUserSets = findViewById(R.id.tv_user_sets);
        tvTimer = findViewById(R.id.tv_timer);
        ivServeLeft = findViewById(R.id.iv_opponent_serve);
        ivServeRight = findViewById(R.id.iv_user_serve);
        btnScore = findViewById(R.id.btn_score);
        tvUndo = findViewById(R.id.tv_undo);
        tvPause = findViewById(R.id.tv_pause);
        btnBack = findViewById(R.id.btn_back);
    }

    private void setupObservers() {
        viewModel.getSetNumber().observe(this, n -> {
            int v = (n == null) ? 1 : n;
            tvSetNumber.setText(v + "세트");
        });
        viewModel.getOpponentSets().observe(this, v -> tvOpponentSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getOpponentScore().observe(this, v -> tvOpponentScore.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getUserSets().observe(this, v -> tvUserSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getUserScore().observe(this, v -> {
            tvUserScore.setText(String.valueOf(v == null ? 0 : v));
            checkMatchStates();
        });
        viewModel.getElapsed().observe(this, sec -> tvTimer.setText(formatTime(sec==null?0:sec)));

        // 세트 대기/진행 상태
        viewModel.getIsSetFinished().observe(this, finished -> {
            boolean fin = finished != null && finished;
            isSetFinished = fin;
            btnScore.setText(fin ? "경기 시작" : "득점");
            viewModel.resetStopwatch();
            updateButtonsForState();
        });

        // 서버 아이콘 갱신: currentServer 또는 isUser1 변경 시마다
        androidx.lifecycle.Observer<Object> serveObserver = o -> updateServeIcons();
        viewModel.getCurrentServer().observe(this, s -> serveObserver.onChanged(null));
        viewModel.getIsUser1().observe(this, isU1 -> serveObserver.onChanged(null));

        // 일시정지 버튼 텍스트 갱신
        viewModel.getIsPaused().observe(this, paused -> {
            boolean p = paused != null && paused;
            tvPause.setText(p ? "경기 재개" : "경기 일시정지");
            updateButtonsForState();
        });
    }

    // 버튼 비활성화
    private void updateButtonsForState() {
        // 세트 시작 전
        if (isSetFinished) {
            btnScore.setEnabled(true);
            tvUndo.setEnabled(false);
            tvPause.setEnabled(false);
        } else {
            // 세트 진행 중 pause 시 득점, 되돌리기 버튼 비활성화
            boolean isPaused = viewModel.getIsPaused().getValue();
            tvPause.setEnabled(true);
            btnScore.setEnabled(!isPaused);
            tvUndo.setEnabled(!isPaused);
        }
    }

    // 버튼 클릭 이벤트 처리
    private void setupClicks() {
        // 득점 <-> 세트 시작 버튼
        btnScore.setOnClickListener(v -> {
            if (isSetFinished) {
                //  다음 세트 시작
                int sn = viewModel.getSetNumber().getValue() == null ? 1 : viewModel.getSetNumber().getValue();
                Player fs = viewModel.getCurrentServer().getValue() == null ? Player.USER1 : viewModel.getCurrentServer().getValue();

                long now = System.currentTimeMillis();
                var msg = viewModel.buildSetStart(gameId, sn, fs, now);
                if (msg != null) {
                    client.send(msg.toString());
                    viewModel.applyIncoming(msg.toString());   // ui 반영
                }
            } else {
                // 득점
                String to = Boolean.TRUE.equals(viewModel.getIsUser1().getValue()) ? "user1" : "user2";
                var msg = viewModel.buildScoreAdd(gameId, to);
                if (msg != null) {
                    client.send(msg.toString());
                    viewModel.applyIncoming(msg.toString());
                }
            }
        });
        // 점수 되돌리기 버튼
        tvUndo.setOnClickListener(v -> {
            if (!viewModel.canUndoMyLastScore()) {
                Toast.makeText(this, "마지막 득점이 내가 아니라서 되돌릴 수 없어요.", Toast.LENGTH_SHORT).show();
                return;
            }
            String from = Boolean.TRUE.equals(viewModel.getIsUser1().getValue()) ? "user1" : "user2";
            var msg = viewModel.buildScoreUndo(gameId, from);
            if (msg != null) {
                client.send(msg.toString());      // 서버 전송
                viewModel.applyIncoming(msg.toString()); // ui업데이트
            }
        });
        // 일시정지 <-> 경기 재개 버튼
        tvPause.setOnClickListener(v -> {
            boolean paused = Boolean.TRUE.equals(viewModel.getIsPaused().getValue());
            long now = System.currentTimeMillis();
            if (!paused) {
                var msg = viewModel.buildSetPause(gameId, now);
                if (msg != null) {
                    client.send(msg.toString());
                    viewModel.applyIncoming(msg.toString()); // ui반영
                }
            } else {
                var msg = viewModel.buildSetResume(gameId, now);
                if (msg != null) {
                    client.send(msg.toString());
                    viewModel.applyIncoming(msg.toString());
                }
            }
        });
    }

    private void updateServeIcons() {
        Player currentServe = viewModel.getCurrentServer().getValue();
        Boolean isUser1 = viewModel.getIsUser1().getValue();
        if (currentServe == null) currentServe = Player.USER1;
        if (isUser1 == null) isUser1 = true;
        boolean serveOnRight = (currentServe == Player.USER1 && isUser1) || (currentServe == Player.USER2 && !isUser1);
        ivServeRight.setVisibility(serveOnRight ? View.VISIBLE : View.INVISIBLE);
        ivServeLeft.setVisibility(serveOnRight ? View.INVISIBLE : View.VISIBLE);
    }

    private String formatTime(long seconds) {
        long h = seconds / 3600L;
        long m = (seconds % 3600L) / 60L;
        long s = seconds % 60L;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private void checkMatchStates() {
        int user = viewModel.getUserScore().getValue() == null ? 0 : viewModel.getUserScore().getValue();
        int opponent = viewModel.getOpponentScore().getValue() == null ? 0 : viewModel.getOpponentScore().getValue();

        // 세트 종료
        if (checkSetWin(opponent, user)) {
            Toast.makeText(this, "세트 종료", Toast.LENGTH_SHORT).show();

            // 승자
            boolean iAmUser1 = Boolean.TRUE.equals(viewModel.getIsUser1().getValue());
            String winner = (user > opponent)
                    ? (iAmUser1 ? "user1" : "user2")
                    : (iAmUser1 ? "user2" : "user1");

            var msg = viewModel.buildSetFinish(gameId, winner);
            if (msg != null) {
                client.send(msg.toString());          // 서버 전송
                viewModel.applyIncoming(msg.toString()); // ui 반영
            }
            return;
        }
        // 매치포인트
        if (checkMatchPoint(opponent, user)) {
            int mySets = viewModel.getUserSets().getValue() == null ? 0 : viewModel.getUserSets().getValue();
            boolean isMatchPoint = (mySets == 1);
            Toast.makeText(this, "Match Point!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkMatchPoint(int opponentScore, int playerScore) {
        return checkSetWin(opponentScore, playerScore+1);
    }

    private boolean checkSetWin( int opponentScore, int playerScore) {
        // 20점 이상 + 2점 선취 또는 30점 먼저 도달
        return (playerScore >= 21 && (playerScore - opponentScore) >= 2) || (playerScore == 30);
    }
}