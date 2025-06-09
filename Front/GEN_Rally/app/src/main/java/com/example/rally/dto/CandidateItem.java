package com.example.rally.dto;

public abstract class CandidateItem {
    public static class TierHeader extends CandidateItem {
        public final int tier;
        public TierHeader(int tier) { this.tier = tier; }
    }

    public static class UserCard extends CandidateItem {
        public final CandidateResponseDto user;
        public UserCard(CandidateResponseDto user) { this.user = user; }
    }
}

