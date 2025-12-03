package com.gen.rally.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentDto {
    private String nickname;
    private String comment;
    private String date;
    private String gameStyle;
    private String tier;
}
