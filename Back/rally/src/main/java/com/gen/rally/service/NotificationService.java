package com.gen.rally.service;

import com.gen.rally.entity.Notification;
import com.gen.rally.entity.User;
import com.gen.rally.enums.State;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.NotificationRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Transactional
    public void sendInvitationNotification(String targetUserId, Long invitationId) {
        User target = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Notification notification = new Notification();
        notification.setUser(target);
        notification.setType(State.요청중);
        notification.setTitle("매칭 요청이 들어왔어요!");
        notification.setBody("매칭 요청을 수락하고, 상대와 함께 경기를 즐겨보세요.");
        notification.setDataJson("{\"invitationId\": " + invitationId + "}");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        String fcmToken = target.getFcmToken();
        if (fcmToken != null && !fcmToken.isBlank()) {
            fcmService.sendInvitation(
                    fcmToken,
                    invitationId,
                    notification.getId(),
                    notification.getTitle(),
                    notification.getBody()
            );
        }
    }

    @Transactional
    public void sendMatchAcceptedNotification(String myNickname, String targetUserId, Long gameId) {
        User target = userRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Notification notification = new Notification();
        notification.setUser(target);
        notification.setType(State.수락);
        notification.setTitle(myNickname + "님과 매칭되었어요!");
        notification.setBody("채팅을 통해 함께 경기 일정을 확정지어보세요.");
        notification.setDataJson("{\"gameId\": " + gameId + "}");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // fcm 전송
        String fcmToken = target.getFcmToken();
        if (fcmToken != null && !fcmToken.isBlank()) {

            fcmService.sendInvitation(
                    fcmToken,
                    gameId,
                    notification.getId(),
                    notification.getTitle(),
                    notification.getBody()
            );
        }
    }


    // 최근 알림 50개
    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(String userId) {
        return notificationRepository.findTop50ByUserUserIdOrderByCreatedAtDesc(userId);
    }

    // 받은 요청 읽음표시
    @Transactional
    public void markAsReadInvitation(String userId) {
        notificationRepository.markAsRead(userId, State.요청중);
    }

    // 안 읽은 요청 알림 개수
    public int getUnreadInvitationCount(String userId) {
        return notificationRepository.countByUserUserIdAndTypeAndIsReadFalse( userId, State.요청중);
    }
}

