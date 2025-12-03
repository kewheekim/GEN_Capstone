package com.gen.rally.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class FcmService {
    public void sendInvitationAccepted(String fcmToken, Long gameId, Long notificationId, String title, String body) {

        Message message = Message.builder()
                .setToken(fcmToken)
                .putData("type", "수락")
                .putData("gameId", String.valueOf(gameId))
                .putData("notificationId", String.valueOf(notificationId))
                .putData("title", title)
                .putData("body", body)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {}
    }

    public void sendInvitation(String fcmToken, Long invitationId, Long notificationId, String title, String body) {
        Message msg = Message.builder()
                .setToken(fcmToken)
                .putData("type", "요청중")
                .putData("invitationId", String.valueOf(invitationId))
                .putData("notificationId", String.valueOf(notificationId))
                .putData("title", title)
                .putData("body", body)
                .build();

        try {
            FirebaseMessaging.getInstance().send(msg);
        } catch (FirebaseMessagingException e) {}
    }
}
