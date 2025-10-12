package com.gen.rally.controller;

import com.gen.rally.dto.EvaluationCreateRequest;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Value("${app.dev.anonymous-evaluator:}")
    private String devEvaluatorFallback;

    @PostMapping
    public ResponseEntity<Void> create(
            @Validated @RequestBody EvaluationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String evaluatorUserId = (userDetails != null) ? userDetails.getUsername() : null;

        if ((evaluatorUserId == null || "anonymousUser".equalsIgnoreCase(evaluatorUserId))
                && devEvaluatorFallback != null && !devEvaluatorFallback.isBlank()) {
            evaluatorUserId = devEvaluatorFallback;
        }

        if (evaluatorUserId == null || "anonymousUser".equalsIgnoreCase(evaluatorUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Long id = evaluationService.createEvaluation(request, evaluatorUserId);
        return ResponseEntity.created(URI.create("/api/evaluation/" + id)).build();
    }
}