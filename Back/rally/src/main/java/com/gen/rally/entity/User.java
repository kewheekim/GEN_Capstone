package com.gen.rally.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@Entity
public class User {

    @Id
    @GeneratedValue
    private String user_id;
    private String password;
    private String name;
    private String profile_image;
    private Gender gender;
    private Tier tier;
    private int skill;
    private double manner_score;
    private LocalDateTime created_at;
}

