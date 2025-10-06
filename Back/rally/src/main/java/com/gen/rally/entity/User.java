package com.gen.rally.entity;

import com.gen.rally.enums.Gender;
import com.gen.rally.enums.LoginType;
import com.gen.rally.enums.Primary;
import com.gen.rally.enums.Tier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Base64;

@Entity @Setter @Getter @Table(name = "`user`")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;

    // TODO: 사용자 삭제 시 다른 속성 모두 삭제?
    private String socialId; // 소셜 로그인 시 받아오는 id
    private String password;
    private String name;

    @Enumerated(EnumType.STRING)
    private Primary primaryThing;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(name= "image_url",length = 2048)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Tier tier;
    private int skill;
    private double manner;
    private LocalDateTime createdAt = LocalDateTime.now();
}