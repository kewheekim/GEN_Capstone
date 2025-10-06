package com.example.rally.ui;

import com.example.rally.R;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import com.example.rally.api.websocket.RealtimeClient;
import com.example.rally.api.websocket.WsRealtimeClient;
import com.example.rally.viewmodel.Player;
import com.example.rally.viewmodel.ScoreViewModel;
import com.example.rally.wear.PhoneDataLayerClient;
import com.example.rally.wear.PhoneDataLayerListener;
import org.json.JSONObject;
import java.util.Locale;
import java.util.UUID;

public class ScoreMonitorActivity extends AppCompatActivity {

    private static final String PATH_EVENT_SET_START = "/rally/event/set_start";
    private static final String PATH_EVENT_SCORE = "/rally/event/score";
    private static final String PATH_EVENT_UNDO   = "/rally/event/undo";
    private static final String PATH_EVENT_PAUSE  = "/rally/event/pause";
    private static final String PATH_EVENT_RESUME = "/rally/event/resume";
    private static final String PATH_EVENT_SET_FINISH = "/rally/event/set_finish";
    private static final String PATH_EVENT_GAME_FINISH = "/rally/event/game_finish";
    private ScoreViewModel viewModel;
    private String matchId;
    private RealtimeClient client;

    ImageButton btnBack;
    private TextView tvSetNumber, tvOpponentName, tvUserName,
            tvOpponentScore, tvUserScore, tvOpponentSets, tvUserSets, tvTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_monitor);

        tvSetNumber = findViewById(R.id.tv_set_number);
        tvOpponentName = findViewById(R.id.tv_opponent_name);
        tvOpponentScore = findViewById(R.id.tv_opponent_score);
        tvOpponentSets = findViewById(R.id.tv_opponent_sets);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserScore = findViewById(R.id.tv_user_score);
        tvUserSets = findViewById(R.id.tv_user_sets);
        tvTimer = findViewById(R.id.tv_timer);
        btnBack = findViewById(R.id.btn_back);
        btnBack.setColorFilter(ContextCompat.getColor(this, R.color.white));

        Intent intent = getIntent();
        tvOpponentName.setText(intent.getStringExtra("opponentName"));
        tvUserName.setText(intent.getStringExtra("userName"));

        viewModel = new ViewModelProvider(this).get(ScoreViewModel.class);
        viewModel.getUserScore().observe(this, v -> tvUserScore.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getOpponentScore().observe(this, v -> tvOpponentScore.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getUserSets().observe(this, v -> tvUserSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getOpponentSets().observe(this, v -> tvOpponentSets.setText(String.valueOf(v == null ? 0 : v)));
        viewModel.getElapsed().observe(this, sec -> tvTimer.setText(formatTime(sec == null ? 0 : sec)));
        viewModel.getSetNumber().observe(this, num -> {
            tvSetNumber.setText(num== null ? "1세트" : num+"세트");
        });

        boolean localIsUser1 = getIntent().getBooleanExtra("localIsUser1", false);
        viewModel.initPlayer(localIsUser1);
        viewModel.prepareSet(1, Player.USER1);

        // websocket 연결
        matchId = intent.getStringExtra("matchId");
        String url = "ws://172.19.20.49:8080/ws-score?matchId=" + matchId;
        client = new WsRealtimeClient(url);
        client.subscribe("/topic/match."+matchId, json -> {
            runOnUiThread( () -> {
                viewModel.applyIncoming(json); // 모니터링 화면 ui 반영
                // 워치로 데이터 전송
                try {
                    JSONObject obj = new JSONObject(json);
                    if ("watch".equals(obj.optString("origin",""))) return; // 루프 방지
                    String type = obj.optString("type", "");
                    JSONObject p = obj.optJSONObject("payload");
                    long ts = obj.optLong("eventTime", System.currentTimeMillis());

                    switch (type) {
                        case "set_start": {
                            int sn = p.optInt("setNumber", 1);
                            String fs = p.optString("firstServer", "USER1");
                            long startAt = p.optLong("startAt", ts);

                            // 워치 표준 env로 전송
                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "SET_START")
                                    .put("eventId", java.util.UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", new JSONObject()
                                            .put("setNumber", sn)
                                            .put("firstServer", fs)
                                            .put("startAt", startAt)
                                    );
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_SET_START, env.toString());
                            break;
                        }
                        case "score_add": {
                            int sn = viewModel.getSetNumber().getValue() == null ? 1 : viewModel.getSetNumber().getValue();
                            boolean iAmUser1 = Boolean.TRUE.equals(viewModel.getIsUser1().getValue());
                            int my = viewModel.getUserScore().getValue() == null ? 0 : viewModel.getUserScore().getValue();
                            int opp = viewModel.getOpponentScore().getValue() == null ? 0 : viewModel.getOpponentScore().getValue();
                            int u1 = iAmUser1 ? my : opp;
                            int u2 = iAmUser1 ? opp : my;

                            String scoreTo = p.optString("scoreTo", "");
                            String scorer = scoreTo == null ? "" : scoreTo.toLowerCase(Locale.ROOT);
                            if(!"user1".equals(scorer) && !"user2".equals(scorer)) {
                                if ("user1".equalsIgnoreCase(scoreTo) || "USER1".equals(scoreTo)) scorer = "user1";
                                else if ("user2".equalsIgnoreCase(scoreTo) || "USER2".equals(scoreTo)) scorer = "user2";
                                else scorer = "";
                            }

                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "SCORE")
                                    .put("eventId", java.util.UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", new JSONObject()
                                            .put("setNumber", sn)
                                            .put("user1Score", u1)
                                            .put("user2Score", u2)
                                            .put("scorer", scorer)
                                    );
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_SCORE, env.toString());
                            break;
                        }
                        case "score_undo": {
                            // 스냅샷 전송
                            int sn = viewModel.getSetNumber().getValue() == null ? 1 : viewModel.getSetNumber().getValue();
                            boolean iAmUser1 = Boolean.TRUE.equals(viewModel.getIsUser1().getValue());
                            int my = viewModel.getUserScore().getValue() == null ? 0 : viewModel.getUserScore().getValue();
                            int opp = viewModel.getOpponentScore().getValue() == null ? 0 : viewModel.getOpponentScore().getValue();
                            int u1 = iAmUser1 ? my : opp;
                            int u2 = iAmUser1 ? opp : my;

                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "SCORE")
                                    .put("eventId", java.util.UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", new JSONObject()
                                            .put("setNumber", sn)
                                            .put("user1Score", u1)
                                            .put("user2Score", u2)
                                    );
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_SCORE, env.toString());
                            break;
                        }
                        case "set_pause": {
                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "PAUSE")
                                    .put("eventId", java.util.UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", new JSONObject()
                                            .put("timeStamp", ts)
                                    );
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_PAUSE, env.toString());
                            break;
                        }
                        case "set_resume": {
                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "RESUME")
                                    .put("eventId", java.util.UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", new JSONObject()
                                            .put("timeStamp", ts)
                                    );
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_RESUME, env.toString());
                            break;
                        }
                        case "set_finish": {
                            int sn = viewModel.getSetNumber().getValue() == null ? 1 : viewModel.getSetNumber().getValue();
                            boolean iAmUser1 = Boolean.TRUE.equals(viewModel.getIsUser1().getValue());
                            int my = viewModel.getUserScore().getValue() == null ? 0 : viewModel.getUserScore().getValue();
                            int opp = viewModel.getOpponentScore().getValue() == null ? 0 : viewModel.getOpponentScore().getValue();
                            int u1 = iAmUser1 ? my : opp;
                            int u2 = iAmUser1 ? opp : my;

                            int mySets  = viewModel.getUserSets().getValue() == null ? 0 : viewModel.getUserSets().getValue();
                            int oppSets = viewModel.getOpponentSets().getValue() == null ? 0 : viewModel.getOpponentSets().getValue();
                            int u1Sets = iAmUser1 ? mySets : oppSets;
                            int u2Sets = iAmUser1 ? oppSets : mySets;
                            String winner = (u1 > u2) ? "user1" : "user2";

                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "SET_FINISH")
                                    .put("eventId", java.util.UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", new JSONObject()
                                            .put("setNumber", sn)
                                            .put("user1Score", u1)
                                            .put("user2Score", u2)
                                            .put("user1Sets", u1Sets)
                                            .put("user2Sets", u2Sets)
                                            .put("winner", winner)
                                            .put("elapsed", viewModel.getElapsed().getValue() == null ? 0 : viewModel.getElapsed().getValue())
                                            .put("isGameFinished", p.optBoolean("isGameFinished", false))
                                    );
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_SET_FINISH, env.toString());
                            break;
                        }
                        case "game_finish": {
                            JSONObject env = new JSONObject()
                                    .put("version", 1)
                                    .put("type", "GAME_FINISH")
                                    .put("eventId", UUID.randomUUID().toString())
                                    .put("createdAtUtc", System.currentTimeMillis())
                                    .put("matchId", matchId)
                                    .put("payload", p);

                            // 워치로 전달
                            PhoneDataLayerClient.sendPhoneEventToWatch(this, PATH_EVENT_GAME_FINISH, env.toString());

                            Intent intentToEvaluate = new Intent(this, GameFinishActivity.class);
                            startActivity(intentToEvaluate);
                            break;
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("ScoreMonitorActivity", "relay to watch failed", e);
                }
            });
        });
        client.connect();
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
                long timeStamp = obj.optLong("timeStamp", System.currentTimeMillis());

                switch (path) {
                    case PATH_EVENT_SET_START: {
                        // 현재 세트번호/선서버로 set_start 이벤트
                        int sn = viewModel.getSetNumber().getValue() == null ? 1 : viewModel.getSetNumber().getValue();
                        Player fs = viewModel.getCurrentServer().getValue() == null ? Player.USER1 : viewModel.getCurrentServer().getValue();
                        var msg = viewModel.buildSetStart(matchId, sn, fs, timeStamp);
                        if (msg != null) {
                            client.send(msg.toString());
                            viewModel.applyIncoming(msg.toString());
                        }
                        break;
                    }
                    case PATH_EVENT_SCORE: {
                        String who = payload.optString("who", "");
                        String source = payload.optString("source", "");

                        String to;
                        if ("me".equals(who)) {
                            boolean watchIsUser1 = payload.optBoolean("watchIsUser1", true); // 없으면 true로 가정
                            to = watchIsUser1 ? "user1" : "user2";
                        } else if ("opponent".equals(who)) {
                            boolean watchIsUser1 = payload.optBoolean("watchIsUser1", true);
                            to = watchIsUser1 ? "user2" : "user1";
                        } else if ("user1".equals(source) || "user2".equals(source)) {
                            to = source;
                        } else {
                            to = Boolean.TRUE.equals(viewModel.getIsUser1().getValue()) ? "user1" : "user2";
                        }

                        var msg = viewModel.buildScoreAdd(matchId, to);
                        if (msg != null) {
                            try { msg.put("origin", "watch"); } catch (Exception ignore) {}
                            client.send(msg.toString());
                            viewModel.applyIncoming(msg.toString());
                        }
                        break;
                    }
                    case PATH_EVENT_UNDO: {
                        if (!viewModel.canUndoMyLastScore()) break;
                        String from = Boolean.TRUE.equals(viewModel.getIsUser1().getValue()) ? "user1" : "user2";
                        var msg = viewModel.buildScoreUndo(matchId, from);
                        if (msg != null) { client.send(msg.toString()); viewModel.applyIncoming(msg.toString()); }
                        break;
                    }
                    case PATH_EVENT_PAUSE: {
                        var msg = viewModel.buildSetPause(matchId, timeStamp);
                        if (msg != null) { client.send(msg.toString()); viewModel.applyIncoming(msg.toString()); }
                        break;
                    }
                    case PATH_EVENT_RESUME: {
                        var msg = viewModel.buildSetResume(matchId, timeStamp);
                        if (msg != null) { client.send(msg.toString()); viewModel.applyIncoming(msg.toString()); }
                        break;
                    }
                    case PATH_EVENT_SET_FINISH: {
                        String winner = payload.optString("winner", "");
                        if (winner.isEmpty()) {
                            int u = viewModel.getUserScore().getValue() == null ? 0 : viewModel.getUserScore().getValue();
                            int o = viewModel.getOpponentScore().getValue() == null ? 0 : viewModel.getOpponentScore().getValue();
                            boolean iAmUser1 = Boolean.TRUE.equals(viewModel.getIsUser1().getValue());
                            winner = (u > o) ? (iAmUser1 ? "user1" : "user2")
                                    : (iAmUser1 ? "user2" : "user1");
                        }
                        var msg = viewModel.buildSetFinish(matchId, winner);
                        if (msg != null) { client.send(msg.toString()); viewModel.applyIncoming(msg.toString()); }
                        break;
                    }
                }
            } catch (Throwable t) {
                android.util.Log.e("ScoreMonitorActivity", "watch event parse error", t);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) client.disconnect();
    }
    private static String formatTime(long seconds) {
        long h = seconds / 3600L;
        long m = (seconds % 3600L) / 60L;
        long s = seconds % 60L;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    }
}