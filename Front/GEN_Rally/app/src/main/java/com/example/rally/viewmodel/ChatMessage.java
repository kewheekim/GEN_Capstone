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
        public static final int VIEW_TYPE_MATCH_SENT = 4;
        public static final int VIEW_TYPE_MATCH_RECEIVED = 5;

        private final Long messageId;
        private final int viewType;
        private final String content;
        private final long timestamp;

        private final Long senderId;
        private final String formattedTime;

        @Nullable
        private final MatchInfo matchInfo;

        private long tempId;
        private boolean isSending; // 전송 상태 (true: 전송 중, false: 완료)

        // 일반 메시지 생성자
        public ChatMessage(Long id, int viewType, String content, long timestamp,
                           Long senderId, String formattedTime,
                           @Nullable MatchInfo matchInfo) {
            this.messageId = id;
            this.viewType = viewType;
            this.content = content;
            this.timestamp = timestamp;
            this.senderId = senderId;
            this.formattedTime = formattedTime;
            this.matchInfo = matchInfo;
            this.tempId = (id != null) ? id : 0; // id가 null이면 0 등으로 초기화 (임시 메시지에서는 id가 tempId로 사용됨)
            this.isSending = false;
        }

    // 전송 전용 생성자
    public ChatMessage(long tempId, String content, long timestamp,
                       String formattedTime, Long senderId) {
        this.messageId = null; // 실제 ID는 서버에서 받음
        this.tempId = tempId;
        this.viewType = VIEW_TYPE_SENT;
        this.content = content;
        this.timestamp = timestamp;
        this.senderId = senderId;
        this.isSending = true;
        this.formattedTime = formattedTime;
        this.matchInfo = null;
    }

        // 날짜 라벨 생성자
        public static ChatMessage dateLabel(String label) {
            return new ChatMessage(
                    null,                                     // messageId: 날짜 라벨은 ID 없음
                    VIEW_TYPE_DATE,                           // viewType: 날짜 타입
                    label,                                    // content: 날짜 문자열 ('2025년 10월 12일')
                    System.currentTimeMillis(),               // timestamp: 날짜 비교용으로 사용되므로, 뷰모델에서 전달받은 값 중 하나를 넣거나 현재 시간을 넣어도 무방
                    null,                                     // senderId: 없음
                    null,                                     // formattedTime: 없음
                    null                                      // matchInfo: 없음
            );
        }

        // 경기 약속 카드
        public static class MatchInfo {
            public final String tempId;
            public final Long senderId;

            public final String status; // CREATED 또는 CONFIRMED
            public final String title;
            public final String dateText;
            public final String timeText;
            public final String place;

            public MatchInfo(String tempId, Long senderId, String status, String title, String dateText, String timeText, String place) {
                this.tempId = tempId;
                this.senderId = senderId;
                this.status = status;
                this.title = title;
                this.dateText = dateText;
                this.timeText = timeText;
                this.place = place;
            }
        }
}
