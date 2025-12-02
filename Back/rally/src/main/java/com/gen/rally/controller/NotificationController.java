package com.gen.rally.controller;

import com.gen.rally.dto.NotificationItem;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/recent")
    public List<NotificationItem> getNotifications( @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        String userId = userDetails.getUsername();
        return notificationService.getRecentNotifications(userId)
                .stream()
                .map(NotificationItem::from)
                .toList();
    }
}

