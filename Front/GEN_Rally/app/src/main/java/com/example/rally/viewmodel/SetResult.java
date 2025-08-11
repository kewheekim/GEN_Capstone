package com.example.rally.viewmodel;

public class SetResult {
    public final int nextSetNumber;
    public final int userScore;
    public final int userSets;
    public final int opponentSets;
    public final int opponentScore;
    public final Player currentServer;
    public final boolean isGameFinished;

    public SetResult(int nextSetNumber, int userScore, int userSets, int opponentSets,
                     int opponentScore, Player currentServer, boolean isGameFinished) {
        this.nextSetNumber = nextSetNumber;
        this.userScore = userScore;
        this.userSets = userSets;
        this.opponentSets = opponentSets;
        this.opponentScore = opponentScore;
        this.currentServer = currentServer;
        this.isGameFinished = isGameFinished;
    }
}
