package com.example.rally.viewmodel;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScoreViewModel extends ViewModel {

    private final MutableLiveData<Integer> opponentScore = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> opponentSets = new MutableLiveData<>(0);
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
    public LiveData<Integer> getUserScore() { return userScore; }
    public LiveData<Integer> getUserSets() { return userSets; }
    public LiveData<Integer> getOpponentScore() { return opponentScore; }
    public LiveData<Integer> getOpponentSets() { return opponentSets; }
    public LiveData<Boolean> getIsUser1() { return isUser1; }
    public LiveData<Player> getCurrentServer() { return currentServer; }
    public LiveData<Boolean> getIsPaused() { return isPaused; }
    public LiveData<Long> getElapsed() { return elapsed; }

    // user1인지 판단
    public void initPlayer(boolean localisUser1) { isUser1.setValue(localisUser1); }

    public void initSets(int user, int opponent) {
        userSets.setValue(user);
        opponentSets.setValue(opponent);
    }

    public void startSet(int setNumber, Player firstServer) {
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
}