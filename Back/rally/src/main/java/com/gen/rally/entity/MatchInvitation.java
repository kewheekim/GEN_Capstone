package com.gen.rally.entity;

import com.gen.rally.enums.State;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity @Setter @Getter
public class MatchInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invitationId;
    @ManyToOne @JoinColumn(name = "sender_request_id")
    private MatchRequest senderRequest;
    @ManyToOne @JoinColumn(name = "receiver_request_id")
    private MatchRequest receiverRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    private User sender;    // 요청을 보낸 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", referencedColumnName = "user_id")
    private User receiver;    // 요청을 받은 사용자
    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State state;
    @Column(length = 100)
    private String refusal;
}
