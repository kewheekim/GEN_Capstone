package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.rally.R;
import com.example.rally.viewmodel.Player;
import com.example.rally.viewmodel.ScoreViewModel;
import com.example.rally.viewmodel.SetResult;

public class ScoreRecordActivity extends AppCompatActivity {
    private ScoreViewModel viewModel;

    private TextView tvSetNumber, tvOpponentName, tvUserName,
            tvOpponentScore, tvUserScore, tvOpponentSets, tvUserSets, tvTimer;
    private ImageView ivServeLeft, ivServeRight;

    private int setNumber; // 현재 세트 번호

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
        Player nextFirstServer = nextFirst != null ? Player.valueOf(nextFirst) : Player.USER1;
        boolean localIsUser1 = intent.getBooleanExtra("localIsUser1", true);

        viewModel.initSets(userSets, opponentSets);
        viewModel.initPlayer(localIsUser1);
        Player firstServer = (setNumber == 1) ? Player.USER1 : nextFirstServer; // 1세트는 항상 USER1
        viewModel.startSet(setNumber, firstServer);
        viewModel.startStopwatch();

        tvSetNumber.setText(setNumber + "세트");
        tvOpponentName.setText("상대");
        tvUserName.setText("나");
    }

    private void bindViews() {
        tvSetNumber = findViewById(R.id.tvSetNumber);
        tvOpponentName = findViewById(R.id.tvOpponentName);
        tvUserName = findViewById(R.id.tvUserName);
        tvOpponentScore = findViewById(R.id.tvOpponentScore);
        tvUserScore = findViewById(R.id.tvUserScore);
        tvOpponentSets = findViewById(R.id.tvOpponentSets);
        tvUserSets = findViewById(R.id.tvUserSets);
        tvTimer = findViewById(R.id.tvTimer);
        ivServeLeft = findViewById(R.id.ivServeLeft);
        ivServeRight = findViewById(R.id.ivServeRight);
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
    }

    private void setupClicks() {
        findViewById(R.id.btnScore).setOnClickListener(v -> viewModel.addUserScore());
        findViewById(R.id.btnUndo).setOnClickListener(v -> viewModel.undoUserScore());
        findViewById(R.id.btnPause).setOnClickListener(v -> viewModel.pause());
    }

    private void updateServeIcons() {
        Player s = viewModel.getCurrentServer().getValue();
        Boolean isU1 = viewModel.getIsUser1().getValue();
        if (s == null) s = Player.USER1;
        if (isU1 == null) isU1 = true;
        boolean serveOnRight = (s == Player.USER1 && isU1) || (s == Player.USER2 && !isU1);
        ivServeRight.setVisibility(serveOnRight ? View.VISIBLE : View.INVISIBLE);
        ivServeLeft.setVisibility(serveOnRight ? View.INVISIBLE : View.VISIBLE);
    }

    private String formatTime(long seconds) {
        long m = seconds / 60L;
        long s = seconds % 60L;
        return String.format("%02d:%02d", m, s);
    }

    private void checkMatchStates() {
        int u = viewModel.getUserScore().getValue() == null ? 0 : viewModel.getUserScore().getValue();
        int o = viewModel.getOpponentScore().getValue() == null ? 0 : viewModel.getOpponentScore().getValue();

        if (checkSetWin(u, o)) {
            Toast.makeText(this, "세트 종료", Toast.LENGTH_SHORT).show();
            viewModel.setFinished();
            viewModel.pause();
            SetResult result = viewModel.onSetFinished();

            // 결과화면 넘어가는 로직 작성
            // Intent intent = new Intent(this, StartActivity.class);
            // startActivity(intent);
            // finish();
        } else if (checkMatchPoint(u, o)) {
            Toast.makeText(this, "Match Point!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkMatchPoint(int playerScore, int opponentScore) {
        return playerScore >= 20 && playerScore == opponentScore; // 20-20 상황
    }

    private boolean checkSetWin(int playerScore, int opponentScore) {
        return playerScore >= 21 && (playerScore - opponentScore) >= 2;
    }
}
