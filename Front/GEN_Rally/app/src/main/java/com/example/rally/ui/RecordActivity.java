package com.example.rally.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TextView;
import com.example.rally.R;
import com.example.rally.viewmodel.ScoreViewModel;
import com.example.rally.wear.PhoneDataLayerListener;

import org.json.JSONObject;

import java.util.Locale;

public class RecordActivity extends AppCompatActivity {

    private static final String PATH_EVENT_START = "/rally/event/start";
    private static final String PATH_EVENT_SCORE = "/rally/event/score";
    private static final String PATH_EVENT_UNDO   = "/rally/event/undo";
    private static final String PATH_EVENT_PAUSE  = "/rally/event/pause";
    private static final String PATH_EVENT_RESUME = "/rally/event/resume";
    private ScoreViewModel viewModel;
    private TextView tvSetNumber, tvOpponentName, tvUserName,
            tvOpponentScore, tvUserScore, tvOpponentSets, tvUserSets, tvTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        tvSetNumber = findViewById(R.id.tv_set_number);
        tvOpponentName = findViewById(R.id.tv_opponent_name);
        tvOpponentScore = findViewById(R.id.tv_opponent_score);
        tvOpponentSets = findViewById(R.id.tv_opponent_sets);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserScore = findViewById(R.id.tv_user_score);
        tvUserSets = findViewById(R.id.tv_user_sets);
        tvTimer = findViewById(R.id.tv_timer);

        Intent intent = getIntent();
        tvOpponentName.setText(intent.getStringExtra("opponentName"));

        viewModel = new ViewModelProvider(this).get(ScoreViewModel.class);
        viewModel.getUserScore().observe(this, v -> tvUserScore.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getOpponentScore().observe(this, v -> tvOpponentScore.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getUserSets().observe(this, v -> tvUserSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getOpponentSets().observe(this, v -> tvOpponentSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getElapsed().observe(this, sec -> tvTimer.setText(formatTime(sec == null ? 0 : sec)));
    }

    private final BroadcastReceiver watchEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!PhoneDataLayerListener.ACTION_WATCH_EVENT.equals(intent.getAction())) return;
            String path = intent.getStringExtra(PhoneDataLayerListener.EXTRA_PATH);
            String json = intent.getStringExtra(PhoneDataLayerListener.EXTRA_JSON);

            try {
                JSONObject obj = new JSONObject(json == null ? "{}" : json);
                JSONObject payload = obj.optJSONObject("payload");
                if (payload == null) payload = new JSONObject();
                long timeStamp = obj.optLong("timeStamp", 0L);

                switch (path) {
                    case PATH_EVENT_START: {
                        if(timeStamp > 0) viewModel.startStopwatchAt(timeStamp);
                        else viewModel.startStopwatch();
                        break;
                    }
                    case PATH_EVENT_SCORE: {
                         viewModel.addUserScore();
                         break;
                    }
                    case PATH_EVENT_UNDO: {
                        viewModel.undoUserScore();
                        break;
                    }
                    case PATH_EVENT_PAUSE: {
                        if (timeStamp > 0) viewModel.pauseAt(timeStamp); else viewModel.pause();
                        break;
                    }
                    case PATH_EVENT_RESUME: {
                        if (timeStamp > 0) viewModel.resumeAt(timeStamp); else viewModel.resume();
                        break;
                    }
                }
            } catch (Throwable t) {
                // log만 출력
                android.util.Log.e("RecordActivity", "watch event parse error", t);
            }
        }
    };

    @Override protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(PhoneDataLayerListener.ACTION_WATCH_EVENT);

        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .registerReceiver(watchEventReceiver, filter);
    }

    @Override protected void onStop() {
        super.onStop();
        androidx.localbroadcastmanager.content.LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(watchEventReceiver);
    }
    private static String formatTime(long seconds) {
        long h = seconds / 3600L;
        long m = (seconds % 3600L) / 60L;
        long s = seconds % 60L;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }
}