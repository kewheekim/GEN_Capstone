package com.gen.rally.entity;

import jakarta.persistence.JoinColumn;

public class MatchState {
    private Long state_id;
    private Long request_id;

    @JoinColumn(name = "sender_id")
    private User sender;    // 요청을 보낸 사용자
    @JoinColumn(name = "receiver_id")
    private User receiver;    // 요청을 받은 사용자
    private State state;
}
