package com.example.rally.viewmodel;

import androidx.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
        public static final int VIEW_TYPE_SENT = 1; // 내가 보냄
        public static final int VIEW_TYPE_RECEIVED = 2; // 남이 보냄
        public static final int VIEW_TYPE_DATE = 3; // 날짜
        public static final int VIEW_TYPE_MATCH_CARD = 4;

        private final Long messageId;
        private final int viewType;
        private final String text;
        private final long timestamp;

        private final Long senderId;

        @Nullable
        private final MatchInfo matchInfo;

        // 일반 메시지 생성자
        public ChatMessage(Long messageId, int viewType, String text, long timestamp,
                           Long senderId,
                           @Nullable MatchInfo matchInfo) {
            this.messageId = messageId;
            this.viewType = viewType;
            this.text = text;
            this.timestamp = timestamp;
            this.senderId = senderId;
            this.matchInfo = matchInfo;
        }

        // 날짜 라벨 생성자
        public static ChatMessage dateLabel(String label) {
            return new ChatMessage(null, VIEW_TYPE_DATE, null,
                    System.currentTimeMillis(), null, null);
        }

        // 경기 약속 카드
        public static class MatchInfo {
            public final String title;
            public final String dateText;
            public final String timeText;
            public final String place;

            public MatchInfo(String title, String dateText, String timeText, String place) {
                this.title = title;
                this.dateText = dateText;
                this.timeText = timeText;
                this.place = place;
            }
        }
}
