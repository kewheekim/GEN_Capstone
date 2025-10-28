package com.gen.rally.service;

import com.gen.rally.dto.InvitationAcceptResponse;
import com.gen.rally.dto.InvitationItem;
import com.gen.rally.entity.ChatRoom;
import com.gen.rally.entity.Game;
import com.gen.rally.entity.MatchInvitation;
import com.gen.rally.entity.MatchRequest;
import com.gen.rally.enums.State;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.ChatRoomRepository;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.MatchInvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InvitationService {
    private final MatchInvitationRepository invitationRepo;
    private final GameRepository gameRepo;
    private final ChatRoomRepository chatRoomRepo;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("M월 d일(E)");

    public List<InvitationItem> findReceived(String myUserId) {
        return invitationRepo.findReceivedByUserId(myUserId).stream().map(mi -> {
            var myReq = mi.getReceiverRequest();
            var oppReq = mi.getSenderRequest();
            var opponent = mi.getSender();
            return new InvitationItem(
                    mi.getInvitationId(),
                    myReq != null ? myReq.getRequestId() : null,
                    oppReq != null ? oppReq.getRequestId() : null,
                    myReq != null && myReq.getGameDate()!=null ? myReq.getGameDate().format(DATE_FMT) : null,
                    myReq != null && myReq.getGameType()!=null ? myReq.getGameType().name() : null,
                    opponent != null ? opponent.getUserId() : null,
                    opponent != null ? opponent.getName() : null,
                    opponent != null ? opponent.getImageUrl() : null,
                    mi.getState() != null ? mi.getState().name() : null,
                    mi.getRefusal()
            );
        }).collect(toList());
    }

    public List<InvitationItem> findSent(String myUserId) {
        return invitationRepo.findSentByUserId(myUserId).stream().map(mi -> {
            var myReq = mi.getSenderRequest();
            var oppReq = mi.getReceiverRequest();
            var opponent = mi.getReceiver();
            return new InvitationItem(
                    mi.getInvitationId(),
                    myReq != null ? myReq.getRequestId() : null,
                    oppReq != null ? oppReq.getRequestId() : null,
                    myReq != null && myReq.getGameDate()!=null ? myReq.getGameDate().format(DATE_FMT) : null,
                    myReq != null && myReq.getGameType()!=null ? myReq.getGameType().name() : null,
                    opponent != null ? opponent.getUserId() : null,
                    opponent != null ? opponent.getName() : null,
                    opponent != null ? opponent.getImageUrl() : null,
                    mi.getState() != null ? mi.getState().name() : null,
                    mi.getRefusal()
            );
        }).collect(toList());
    }

    @Transactional
    public InvitationAcceptResponse accept(String myUserId, Long invitationId){
        MatchInvitation inv = invitationRepo.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("초대 없음"));

        // 수신자 존재/일치 검증
        if ( inv.getReceiver() == null || inv.getReceiver().getUserId() == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if(!inv.getReceiver().getUserId().equals(myUserId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        //  이미 수락 했거나 해당 Game 존재 시 그대로 반환
        Game existing = gameRepo.findByRequests(inv.getSenderRequest(), inv.getReceiverRequest()).orElse(null);
        if (inv.getState() == State.수락 && existing != null) {
            ChatRoom room = chatRoomRepo.findByGame(existing).orElse(null);
            return new InvitationAcceptResponse(existing.getGameId(), room != null ? room.getId() : null);
        }
        if (inv.getState() == State.거절) {
            throw new IllegalStateException("이미 거절된 초대");
        }

        MatchRequest senderReq = inv.getSenderRequest();
        MatchRequest receiverReq = inv.getReceiverRequest();
        if (senderReq == null || receiverReq == null) {
            throw new IllegalStateException("초대에 연결된 요청이 없습니다");
        }

        // 상태 업데이트
        inv.setState(State.수락);
        senderReq.setState(State.수락);
        receiverReq.setState(State.수락);

        Game game = existing;
        if (game == null) {
            game = new Game();
            game.setRequestId1(senderReq);
            game.setRequestId2(receiverReq);
            game.setUser1(inv.getSender());
            game.setUser2(inv.getReceiver());
            game.setDate(senderReq.getGameDate() != null ? senderReq.getGameDate() : receiverReq.getGameDate());
            game.setGameType(senderReq.getGameType() != null ? senderReq.getGameType() : receiverReq.getGameType());
            game.setState(State.수락);
            gameRepo.save(game);
        }

        ChatRoom room = chatRoomRepo.findByGame(game).orElse(null);
        if (room == null) {
            room = ChatRoom.create(game);
            chatRoomRepo.save(room);
        }

        return new InvitationAcceptResponse(game.getGameId(), room.getId());
    }
}
