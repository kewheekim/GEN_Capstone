package com.gen.rally.controller;

import com.gen.rally.dto.MatchRequestCreateDto;
import com.gen.rally.service.MatchRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchRequestController {
    private final MatchRequestService matchRequestService;
}