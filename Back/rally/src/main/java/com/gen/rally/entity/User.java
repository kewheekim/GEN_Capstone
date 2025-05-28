package com.gen.rally.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

@Entity @Setter @Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String userId;
    private String password;
    private String name;
    @Lob
    @Column(name= "profile_image")
    private byte[] profileImage;
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

