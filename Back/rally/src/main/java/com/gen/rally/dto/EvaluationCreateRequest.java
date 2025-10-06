package com.gen.rally.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class EvaluationCreateRequest {
        Long gameId;
        String evaluator;
        String subject;
        Double mannerScore;
        String comment;
}
