package com.gen.rally.controller;

import com.gen.rally.dto.EvaluationCreateRequest;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/evaluation")
public class EvaluationController {
    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody EvaluationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) throw new CustomException(ErrorCode.UNAUTHORIZED);

        request.setEvaluator(userDetails.getUsername());
        evaluationService.createEvaluation(request);

        return ResponseEntity.status(201).build();
    }
}