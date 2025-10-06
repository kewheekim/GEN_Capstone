package com.example.rally.viewmodel;

import android.text.format.DateFormat;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rally.dto.ChatMessageDto;
import com.example.rally.dto.ChatRoomDto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatViewModel extends ViewModel {

    // Key: SenderId (Long), Value: ChatRoomDto (프로필 정보)
    private final Map<Long, ChatRoomDto> profileCache = new HashMap<>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());

    // 채팅방 입장 시 호출하여 참가자 프로필 정보를 캐싱
    public void cacheParticipantProfiles(List<ChatRoomDto> participants) {
        // 기존 캐시를 비우거나 (선택적)
        // profileCache.clear();

        for (ChatRoomDto dto : participants) {
            // 참가자 ID를 키로 사용하여 Map에 저장
            profileCache.put(dto.getId(), dto);
        }
    }

    // 웹소켓 메시지 수신 및 LiveData 업데이트 함수
    public void addIncomingMessage(ChatMessageDto dto, Long myUserId) {
        List<ChatMessage> currentList = messages.getValue() == null ?
                new ArrayList<>() : messages.getValue();

        // 1. DTO를 ChatMessage로 변환
        int viewType = dto.getSenderId().equals(myUserId) ?
                ChatMessage.VIEW_TYPE_SENT : ChatMessage.VIEW_TYPE_RECEIVED;

        long timestamp = convertToTimestamp(dto.getCreatedAt());

        ChatMessage newMsg = new ChatMessage(
                dto.getMessageId(),
                viewType,
                dto.getContent(),
                timestamp,
                dto.getSenderId(),
                null // MatchInfo가 있다면 여기서 파싱하여 추가
        );

        List<ChatMessage> updatedList = new ArrayList<>(currentList);

        // 2. 날짜 라벨 전처리 로직
        if (!currentList.isEmpty()) {
            ChatMessage lastMessage = null;

            // 리스트의 마지막 요소가 날짜 라벨이 아닐 때까지 역순으로 찾아야 함
            for (int i = currentList.size() - 1; i >= 0; i--) {
                if (currentList.get(i).getViewType() != ChatMessage.VIEW_TYPE_DATE) {
                    lastMessage = currentList.get(i);
                    break;
                }
            }

            // 마지막 메시지의 타임스탬프와 새로운 메시지의 타임스탬프를 비교
            if (lastMessage != null) {
                if (!isSameDay(lastMessage.getTimestamp(), newMsg.getTimestamp())) {
                    // 날짜가 다르면 날짜 라벨 먼저 추가
                    updatedList.add(ChatMessage.dateLabel(formatDateLabel(newMsg.getTimestamp())));
                }
            }
        } else {
            // 리스트가 비어있다면 (첫 메시지) 날짜 라벨 추가
            updatedList.add(ChatMessage.dateLabel(formatDateLabel(newMsg.getTimestamp())));
        }

        updatedList.add(newMsg);
        messages.setValue(updatedList);
    }

    private long convertToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            Log.e("ChatViewModel", "LocalDateTime is null, returning current time.");
            return System.currentTimeMillis();
        }
        return localDateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli();
    }

    private String formatDateLabel(long timeMillis) {
        return DateFormat.format("yyyy년 M월 d일", timeMillis).toString();
    }

    // 날짜 비교 유틸
    private boolean isSameDay(long t1, long t2) {

        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(t1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(t2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);

    }

    private String formatTime(long timeMillis) {
        return DateFormat.format("a h:mm", timeMillis).toString(); // 오전 8:10 형식
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public ChatRoomDto getProfile(Long senderId) {
        return profileCache.get(senderId);
    }
}
