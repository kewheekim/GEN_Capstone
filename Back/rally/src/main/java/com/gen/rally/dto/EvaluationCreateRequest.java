package com.gen.rally.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationCreateRequest {
        Long gameId;
        Double mannerScore;
        String comment;
}
