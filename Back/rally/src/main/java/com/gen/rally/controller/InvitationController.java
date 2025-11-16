package com.gen.rally.controller;

import com.gen.rally.dto.*;
import com.gen.rally.entity.CustomUserDetails;
import com.gen.rally.entity.MatchInvitation;
import com.gen.rally.entity.MatchRequest;
import com.gen.rally.entity.User;
import com.gen.rally.enums.State;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.MatchInvitationRepository;
import com.gen.rally.repository.MatchRequestRepository;
import com.gen.rally.repository.UserRepository;
import com.gen.rally.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/invitation")
public class InvitationController {
    private final MatchInvitationRepository inviteRepo;
    private final UserRepository userRepo;
    private final MatchRequestRepository requestRepo;
    private final InvitationService invitationService;

    @PostMapping("/invite")
    public ResponseEntity<MatchInviteResponse> invite( @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MatchInviteRequest req
    ) {
        User sender = userRepo.findByUserId(userDetails.getUsername()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        User receiver = userRepo.findByUserId(req.getReceiverId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        MatchRequest senderReq = requestRepo.findById(req.getSenderRequestId())
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_REQUEST_NOT_FOUND));
        MatchRequest receiverReq = requestRepo.findById(req.getReceiverRequestId())
                .orElseThrow(() -> new CustomException(ErrorCode.MATCH_REQUEST_NOT_FOUND));

        // 소유자 검증
        if (!senderReq.getUser().getUserId().equals(sender.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN); // 내 요청이 아님
        }
        if (!receiverReq.getUser().getUserId().equals(receiver.getUserId())) {
            throw new CustomException(ErrorCode.FORBIDDEN); // 상대의 요청과 receiverId 불일치
        }
        // 상태 검증 (대기 상태만 요청 가능)
        if (senderReq.getState() != State.대기 || receiverReq.getState() != State.대기) {
            throw new CustomException(ErrorCode.INVALID_STATE);
        }

        senderReq.setState(State.요청중);
        MatchInvitation inv = new MatchInvitation();
        inv.setSender(sender);
        inv.setReceiver(receiver);
        inv.setSenderRequest(senderReq);
        inv.setReceiverRequest(receiverReq);
        inv.setState(State.요청중);

        inviteRepo.save(inv);

        return ResponseEntity.ok(new MatchInviteResponse(inv.getInvitationId(), inv.getState().name()));
    }

    // 받은 요청 목록
    @GetMapping("/received")
    public List<InvitationItem> received(@AuthenticationPrincipal CustomUserDetails userDetails ) {
        return invitationService.findReceived(userDetails.getUsername());
    }

    // 보낸 요청 목록
    @GetMapping("/sent")
    public List<InvitationItem> sent(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return invitationService.findSent(userDetails.getUsername());
    }

    // 요청 수락
    @PostMapping("/accept")
    public InvitationAcceptResponse accept(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody InvitationAcceptRequest req) {
        if (req == null || req.getInvitationId() == null) {
            throw new IllegalArgumentException("invitationId 누락");
        }
        return invitationService.accept(userDetails.getUsername(), req.getInvitationId());
    }

    // 요청 거절
    @PostMapping("/refuse")
    public ResponseEntity<Void> refuse(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody InvitationRefuseRequest req) {
        String userId = userDetails.getUsername();
        invitationService.refuse(userId, req.getInvitationId(), req.getRefusal());
        return ResponseEntity.ok().build();
    }
}