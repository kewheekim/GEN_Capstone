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
    private Primary primaryThing;
    private LoginType loginType;

    @Lob
    @Column(name= "profile_image", columnDefinition = "MEDIUMBLOB")
    private byte[] profileImage; // TODO: byte 대신 imageurl 쓰는 게 어떤지...
    private Gender gender;
    private Tier tier;
    private int skill;
    private double manner;
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getProfileImageUrl() {
        if (this.profileImage == null) return null;
        return Base64.getEncoder().encodeToString(this.profileImage);
    }
}

