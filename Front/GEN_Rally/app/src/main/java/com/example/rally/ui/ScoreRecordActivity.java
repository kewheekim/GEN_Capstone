package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.rally.R;
import com.example.rally.api.websocket.RealtimeClient;
import com.example.rally.api.websocket.WsRealtimeClient;
import com.example.rally.viewmodel.Player;
import com.example.rally.viewmodel.ScoreViewModel;
import com.example.rally.viewmodel.SetResult;

public class ScoreRecordActivity extends AppCompatActivity {
    private ScoreViewModel viewModel;
    private RealtimeClient client;
    private final String matchId = "match-123";


    private TextView tvSetNumber, tvOpponentName, tvUserName,
            tvOpponentScore, tvUserScore, tvOpponentSets, tvUserSets, tvTimer;
    private ImageView ivServeLeft, ivServeRight;
    private Button btnScore, btnUndo, btnPause;

    private int setNumber;
    private boolean isSetFinished = false;
    private final Handler uiHandler = new Handler(Looper.getMainLooper()); // 일시정지
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
        long startTime = intent.getLongExtra("startTime", System.currentTimeMillis());
        setNumber = intent.getIntExtra("setNumber", 1);
        int opponentSets = intent.getIntExtra("opponentSets", 0);
        int userSets = intent.getIntExtra("userSets", 0);
        String nextFirst = intent.getStringExtra("nextFirstServer");
        this.nextFirstServer = nextFirst != null ? Player.valueOf(nextFirst) : Player.USER1;
        boolean isUser1 = intent.getBooleanExtra("localIsUser1", true);

        viewModel.initSets(userSets, opponentSets);
        viewModel.initPlayer(isUser1);
        Player firstServer = (setNumber == 1) ? Player.USER1 : nextFirstServer; // 1세트 서브 시작은 user1
        viewModel.startSet(setNumber, firstServer);

        // web socket
        String url = "ws://172.19.14.52:8080/ws?matchId=" + matchId;
        client = new WsRealtimeClient(url);
        client.subscribe("/topic/match."+matchId, json -> viewModel.applyIncoming(json));
        client.connect();

        tvSetNumber.setText(setNumber + "세트");
        tvOpponentName.setText("상대");
        tvUserName.setText("나");
        btnScore.setText("경기 시작");
        btnScore.setEnabled(false);
        isSetFinished = true;    // 대기
        tvTimer.setText("00:00:00");
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
        btnUndo = findViewById(R.id.btn_undo);
        btnPause = findViewById(R.id.btn_pause);
    }

    private void setupObservers() {
        viewModel.getUserScore().observe(this, v -> {
            tvUserScore.setText(String.valueOf(v == null ? 0 : v));
            checkMatchStates();
        });
        viewModel.getOpponentScore().observe(this, v -> tvOpponentScore.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getUserSets().observe(this, v -> tvUserSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getOpponentSets().observe(this, v -> tvOpponentSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getElapsed().observe(this, sec -> tvTimer.setText(formatTime(sec==null?0:sec)));

        // 서버 아이콘 갱신: currentServer 또는 isUser1 변경 시마다
        androidx.lifecycle.Observer<Object> serveObserver = o -> updateServeIcons();
        viewModel.getCurrentServer().observe(this, s -> serveObserver.onChanged(null));
        viewModel.getIsUser1().observe(this, isU1 -> serveObserver.onChanged(null));

        // 일시정지 버튼 텍스트 갱신
        viewModel.getIsPaused().observe(this, paused -> {
            boolean p = paused != null && paused;
            btnPause.setText(p ? "경기 재개" : "경기 일시정지");
            updateButtonsForState();
        });
    }

    // 버튼 비활성화
    private void updateButtonsForState() {
        // 세트 시작 전
        if (isSetFinished) {
            btnScore.setEnabled(true);
            btnUndo.setEnabled(false);
            btnPause.setEnabled(false);
        } else {
            // 세트 진행 중 pause 시 득점, 되돌리기 버튼 비활성화
            boolean isPaused = viewModel.getIsPaused().getValue();
            btnPause.setEnabled(true);
            btnScore.setEnabled(!isPaused);
            btnUndo.setEnabled(!isPaused);
        }
    }

    // 버튼 클릭 이벤트 처리
    private void setupClicks() {
        // 득점 <-> 세트 시작 버튼
        btnScore.setOnClickListener(v -> {
            if (isSetFinished) {
                //  다음 세트 시작
                viewModel.startSet(setNumber, nextFirstServer);
                viewModel.startStopwatch();
                isSetFinished = false;
                btnScore.setText("득점");
                updateServeIcons();  // 서브 아이콘
                updateButtonsForState();
            } else {
                //  득점
                String to = Boolean.TRUE.equals(viewModel.getIsUser1().getValue())? "user1" : "user2";
                var msg = viewModel.buildScoreAdd(matchId, to);
                if(msg != null) {
                    client.send(msg.toString());
                    viewModel.applyIncoming(msg.toString());
                }
            }
        });
        // 점수 되돌리기 버튼
        btnUndo.setOnClickListener(v -> {
            String from = Boolean.TRUE.equals(viewModel.getIsUser1().getValue()) ? "user1" : "user2"; // 소문자
            var msg = viewModel.buildScoreUndo(matchId, from);
            if (msg != null) {
                client.send(msg.toString());                 // 서버 전송
                viewModel.applyIncoming(msg.toString());     // ui업데이트
            }
        });
        // 일시정지 <-> 경기 재개 버튼
        btnPause.setOnClickListener(v -> {
            Boolean paused = viewModel.getIsPaused().getValue();
            if (paused != null && paused) {
                viewModel.resume();   // 현재 일시정지 상태면 재개
            } else {
                viewModel.pause();    // 진행 중이면 일시정지
            }
            updateButtonsForState();
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
            viewModel.setFinished();
            viewModel.pause();

            // 2초 동안 최종 스코어를 그대로 보여줌
            uiHandler.postDelayed(() -> {
                // 2초 후 세트 마무리
                SetResult result = viewModel.onSetFinished();

                // 다음 세트 대기 화면으로
                setNumber = result.nextSetNumber;                 // 다음 세트 번호
                tvSetNumber.setText(setNumber + "세트");
                tvUserScore.setText("0");
                tvOpponentScore.setText("0");
                tvTimer.setText("00:00:00");
                ivServeLeft.setVisibility(View.INVISIBLE);
                ivServeRight.setVisibility(View.INVISIBLE);

                nextFirstServer = result.currentServer;           // 다음 세트 선서브
                isSetFinished = true;                             // 대기 진입
                btnScore.setText("경기 시작");
                updateButtonsForState();
            }, 2000);
            return;
        }

        //  매치포인트
        if (checkMatchPoint(opponent, user)) {
            int mySets = viewModel.getUserSets().getValue() == null ? 0 : viewModel.getUserSets().getValue();
            //  2선승제
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