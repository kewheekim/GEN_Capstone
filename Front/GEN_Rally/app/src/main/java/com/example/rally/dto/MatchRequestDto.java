package com.example.rally.dto;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;

@Getter
public class MatchRequestDto implements Serializable {
    private int gameType;
    private int gameStyle;
    private boolean sameGender;

    private String gameDate;
    private int startTime;
    private int endTime;

    private String place;
    private double latitude;
    private double longitude;

    public MatchRequestDto(int gameType, int gameStyle, boolean sameGender,
                           LocalDate date, int startTime, int endTime, String place,double lat, double lon) {

        this.gameType=gameType;
        this.gameStyle = gameStyle;
        this.sameGender=sameGender;

        this.gameDate = date.toString();
        this.startTime = startTime;
        this.endTime=endTime;

        this.place=place;
        this.latitude=lat;
        this.longitude=lon;
    }
}