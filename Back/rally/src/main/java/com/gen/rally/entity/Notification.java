package com.gen.rally.entity;

import com.gen.rally.enums.State;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    private State type;
    private String title;
    private String body;
    @Column(columnDefinition = "TEXT")
    private String dataJson;

    private boolean isRead;
    private LocalDateTime createdAt;
}
