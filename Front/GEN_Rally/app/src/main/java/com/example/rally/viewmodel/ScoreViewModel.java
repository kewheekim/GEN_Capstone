package com.example.rally.viewmodel;

import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ScoreViewModel extends ViewModel {
    private final MutableLiveData<Integer> setNumber = new MutableLiveData<>(1);
    private final MutableLiveData<String> opponentName = new MutableLiveData<>("상대");
    private final MutableLiveData<Integer> opponentScore = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> opponentSets = new MutableLiveData<>(0);
    private final MutableLiveData<String> userName = new MutableLiveData<>("나");
    private final MutableLiveData<Integer> userScore = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> userSets = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isUser1 = new MutableLiveData<>(true); // 로컬이 USER1?
    private final MutableLiveData<Player> currentServer = new MutableLiveData<>(Player.USER1);
    private final MutableLiveData<Boolean> isPaused = new MutableLiveData<>(false);
    private final MutableLiveData<Long> elapsed = new MutableLiveData<>(0L);

    private long startTime = 0L;
    private long totalPaused = 0L;
    private Long pauseStartedAt = null;

    private final android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override public void run() {
            Boolean paused = isPaused.getValue();
            if (paused == null) paused = false;
            if (!paused) {
                long now = System.currentTimeMillis();
                long e = now - startTime - totalPaused;
                elapsed.postValue(e / 1000L); // 초 단위
            }
            handler.postDelayed(this, 1000L);
        }
    };

    private int lastSeq = 0;
    private final Set<String> appliedMsgIds = new HashSet<>();

    // 점수 동작 내역
    private static class Action {
        final Player preServer;
        final Player scorer;
        Action(Player preServer, Player scorer) { this.preServer = preServer; this.scorer = scorer; }
    }
    private final Deque<Action> history = new ArrayDeque<>();

    private Player localPlayer() { return Boolean.TRUE.equals(isUser1.getValue()) ? Player.USER1 : Player.USER2; }
    private Player opponentPlayer() { return Boolean.TRUE.equals(isUser1.getValue()) ? Player.USER2 : Player.USER1; }

    // getter
    public LiveData<Integer> getSetNumber()      { return setNumber; }
    public LiveData<String>  getUserName()       { return userName; }
    public LiveData<String>  getOpponentName()   { return opponentName; }
    public LiveData<Integer> getUserScore() { return userScore; }
    public LiveData<Integer> getUserSets() { return userSets; }
    public LiveData<Integer> getOpponentScore() { return opponentScore; }
    public LiveData<Integer> getOpponentSets() { return opponentSets; }
    public LiveData<Boolean> getIsUser1() { return isUser1; }
    public LiveData<Player> getCurrentServer() { return currentServer; }
    public LiveData<Boolean> getIsPaused() { return isPaused; }
    public LiveData<Long> getElapsed() { return elapsed; }

    // 초기화
    public void initPlayer(boolean localisUser1) { isUser1.setValue(localisUser1); }
    public void initSets(int user, int opponent) {
        userSets.setValue(user);
        opponentSets.setValue(opponent);
    }
    public void setNames (String localUserName, String oppName) {
        userName.setValue(localUserName);
        opponentName.setValue(oppName);
    }

    public void startSet(int setNum, Player firstServer) {
        setNumber.setValue(setNum);
        currentServer.setValue(firstServer);
        userScore.setValue(0);
        opponentScore.setValue(0);
        isSetFinished.setValue(false);
        history.clear();
    }

    public void addUserScore() {
        Player before = currentServer.getValue();
        if (before == null) before = Player.USER1;
        history.addLast(new Action(before, localPlayer()));
        userScore.setValue((userScore.getValue() == null ? 0 : userScore.getValue()) + 1);
        currentServer.setValue(localPlayer());
    }

    public void undoUserScore() {
        Integer u = userScore.getValue();
        if (u == null || u <= 0 || history.isEmpty()) return;
        Action last = history.removeLast();
        if (last.scorer == localPlayer()) {
            userScore.setValue(u - 1);
            currentServer.setValue(last.preServer); // 서브권 직전 상태로 복원
        } else {
            // 마지막 기록이 상대 득점이면 되돌리지 않음
            history.addLast(last);
        }
    }

    private final MutableLiveData<Boolean> isSetFinished = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsSetFinished() { return isSetFinished; }

    public void setFinished() { isSetFinished.setValue(true); }

    private final MutableLiveData<Boolean> isGameFinished = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsGameFinished() { return isGameFinished; }

    public SetResult onSetFinished() {
        int user = userScore.getValue() == null ? 0 : userScore.getValue();
        int opp = opponentScore.getValue() == null ? 0 : opponentScore.getValue();

        String winner;
        if (user > opp) { userSets.setValue((userSets.getValue()==null?0:userSets.getValue()) + 1); winner = "user"; }
        else if (opp > user) { opponentSets.setValue((opponentSets.getValue()==null?0:opponentSets.getValue()) + 1); winner = "opponent"; }
        else { winner = "draw"; }

        // 다음 세트 첫 서브 = 세트 승자
        if ("user".equals(winner)) {
            currentServer.setValue(localPlayer());
        } else {
            currentServer.setValue(opponentPlayer());
        }

        if ((userSets.getValue()!=null && userSets.getValue()>=2) || (opponentSets.getValue()!=null && opponentSets.getValue()>=2)) {
            isGameFinished.setValue(true);
        }

        history.clear();

        int nextSetNum = (userSets.getValue()==null?0:userSets.getValue()) + (opponentSets.getValue()==null?0:opponentSets.getValue()) + 1;
        setNumber.setValue(nextSetNum);
        return new SetResult(
                nextSetNum,
                user,
                userSets.getValue()==null?0:userSets.getValue(),
                opponentSets.getValue()==null?0:opponentSets.getValue(),
                opp,
                currentServer.getValue()==null?Player.USER1:currentServer.getValue(),
                Boolean.TRUE.equals(isGameFinished.getValue())
        );
    }

    // 스톱워치
    public void startStopwatch() {
        startTime = System.currentTimeMillis();
        totalPaused = 0L;
        pauseStartedAt = null;
        isPaused.setValue(false);
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, 1000L);
    }
    public void pause() {
        if (!Boolean.TRUE.equals(isPaused.getValue())) {
            isPaused.setValue(true);
            pauseStartedAt = System.currentTimeMillis();
        }
    }
    public void resume() {
        if (Boolean.TRUE.equals(isPaused.getValue())) {
            long now = System.currentTimeMillis();
            if (pauseStartedAt != null) totalPaused += (now - pauseStartedAt);
            pauseStartedAt = null;
            isPaused.setValue(false);
        }
    }
    public void resetStopwatch() {
        handler.removeCallbacks(ticker);

        startTime = 0L;
        totalPaused = 0L;
        pauseStartedAt = null;

        elapsed.setValue(0L);
        isPaused.setValue(true);
    }
    // 스톱워치(시작/일시정지/재개): 원격 타임스탬프 기준 (딜레이 방지)
    public void startStopwatchAt(long epochMillisUtc) {
        startTime = epochMillisUtc;
        totalPaused = 0L;
        pauseStartedAt = null;
        isPaused.setValue(false);
        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, 1000L);
    }
    public void pauseAt(long epochMillisUtc) {
        if (!Boolean.TRUE.equals(isPaused.getValue())) {
            isPaused.setValue(true);
            // 원격 기준으로 정렬
            pauseStartedAt = epochMillisUtc;
        }
    }
    public void resumeAt(long epochMillisUtc) {
        if (Boolean.TRUE.equals(isPaused.getValue())) {
            if (pauseStartedAt != null) totalPaused += (epochMillisUtc - pauseStartedAt);
            pauseStartedAt = null;
            isPaused.setValue(false);
        }
    }
    // 점수 스냅샷 미러링(절대값 반영)
    public void applyScoreSnapshot(int user, int opp) {
        userScore.setValue(user);
        opponentScore.setValue(opp);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        handler.removeCallbacks(ticker);         // 타이머 정리
    }

    // web socket 서버 통신 처리
    // 서버에서 받은 json viewmodel에 반영
    public void applyIncoming(String json) {
        try {
            JSONObject root = new JSONObject(json);
            String type = root.optString("type", "");

            if ("snapshot".equals(type)) {
                JSONObject p = root.getJSONObject("payload");
                int u = p.getJSONObject("user1").optInt("score", 0);
                int o = p.getJSONObject("user2").optInt("score", 0);
                boolean iAmUser1 = Boolean.TRUE.equals(isUser1.getValue());
                userScore.setValue(iAmUser1 ? u : o);
                opponentScore.setValue(iAmUser1 ? o : u);
                lastSeq = p.optInt("lastAppliedSeq", lastSeq);
                return;
            }

            // 순서 처리
            String clientMsgId = root.optString("clientMsgId", "");
            Log.d("WS","in: type="+type+" id="+clientMsgId+" contains?="+appliedMsgIds.contains(clientMsgId));
            if (!clientMsgId.isEmpty()) {
                if (appliedMsgIds.contains(clientMsgId)) return;
                appliedMsgIds.add(clientMsgId);
            }
            int seq = root.optInt("seq", 0);
            if (seq > 0 && seq <= lastSeq) return;
            lastSeq = Math.max(lastSeq, seq);

            boolean iAmUser1 = Boolean.TRUE.equals(isUser1.getValue());

            if ("score_add".equals(type)) {
                String scoreTo = root.optJSONObject("payload").optString("scoreTo", "");
                boolean myScore = (iAmUser1 && "user1".equals(scoreTo)) || (!iAmUser1 && "user2".equals(scoreTo));

                //  히스토리 기록 (되돌리기 시 서브 복원용)
                Player before = currentServer.getValue() == null ? Player.USER1 : currentServer.getValue();
                history.addLast(new Action(before, myScore ? localPlayer() : opponentPlayer()));

                // 점수/서브 반영
                if (myScore) {
                    Integer v = userScore.getValue(); userScore.setValue((v==null?0:v)+1);
                    currentServer.setValue(localPlayer());
                } else {
                    Integer v = opponentScore.getValue(); opponentScore.setValue((v==null?0:v)+1);
                    currentServer.setValue(opponentPlayer());
                }
            }
            else if ("score_undo".equals(type)) {
                String from = root.optJSONObject("payload").optString("from", "");
                boolean undoMine = (iAmUser1 && "user1".equals(from)) || (!iAmUser1 && "user2".equals(from));

                // 점수 감소
                if (undoMine) {
                    Integer v = userScore.getValue(); if (v != null && v > 0) userScore.setValue(v-1);
                } else {
                    Integer v = opponentScore.getValue(); if (v != null && v > 0) opponentScore.setValue(v-1);
                }

                // 서브 복원: 직전 기록과 일치할 때만
                if (!history.isEmpty()) {
                    Action last = history.removeLast();
                    if ((undoMine && last.scorer == localPlayer()) ||
                            (!undoMine && last.scorer == opponentPlayer())) {
                        currentServer.setValue(last.preServer);
                    } else {
                        // 다른 사람이 마지막 득점자였다면 복원 보류
                        history.addLast(last);
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    // 로컬 사용자 점수 +1 (ui만 반영)
    private void addUserScoreLocalOnly() {
        Integer v = userScore.getValue();
        userScore.setValue((v==null?0:v)+1);
    }
    // 상대 점수 +1
    private void addOpponentScoreLocalOnly() {
        Integer v = opponentScore.getValue();
        opponentScore.setValue((v==null?0:v)+1);
    }

    // 서버 전송용 득점 이벤트 JSON 생성
    public JSONObject buildScoreAdd(String matchId, String to) {
        try {
            JSONObject payload = new JSONObject().put("scoreTo", to);
            JSONObject root = new JSONObject();
            root.put("type", "score_add");
            root.put("matchId", matchId);
            root.put("clientMsgId", UUID.randomUUID().toString());
            int nextSeq = lastSeq + 1;
            root.put("seq", nextSeq);
            root.put("eventTime", System.currentTimeMillis());
            root.put("actor", Boolean.TRUE.equals(isUser1.getValue()) ? "user1" : "user2");
            root.put("payload", payload);
            return root;
        } catch (Exception e) { return null; }
    }
    // 서버 전송용 되돌리기 이벤트 JSON 생성
    public JSONObject buildScoreUndo(String matchId, String from) {
        try {
            JSONObject root = new JSONObject();
            root.put("type", "score_undo");
            root.put("matchId", matchId);
            String id = java.util.UUID.randomUUID().toString();
            root.put("clientMsgId", id);
            int nextSeq = lastSeq + 1;
            root.put("seq", nextSeq);
            root.put("eventTime", System.currentTimeMillis());
            root.put("actor", Boolean.TRUE.equals(getIsUser1().getValue()) ? "user1" : "user2");
            root.put("payload", new JSONObject().put("from", from));
            return root;
        } catch (Exception e) { return null; }
    }

}