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

    @PostMapping("/create")
    public ResponseEntity<Void> create(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody EvaluationCreateRequest request) {
        evaluationService.createEvaluation(request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}