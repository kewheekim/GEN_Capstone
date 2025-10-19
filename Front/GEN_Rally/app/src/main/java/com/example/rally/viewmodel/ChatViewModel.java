package com.example.rally.viewmodel;

import android.text.format.DateFormat;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.rally.dto.ChatMessageDto;
import com.example.rally.dto.ChatMessageRequest;
import com.example.rally.dto.ChatRoomDto;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatViewModel extends ViewModel {
    private final Map<Long, ChatRoomDto> profileCache = new HashMap<>();
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>(new ArrayList<>());

    private final Gson gson = new Gson();

    private static class JsonPayload {
        private String type;
        private Map<String, String> data;

        public String getType() { return type; }
        public Map<String, String> getData() { return data; }
    }

    private static final DateTimeFormatter SERVER_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .toFormatter();

    // 채팅방 입장 시 호출하여 참가자 프로필 정보를 캐싱
    public void cacheParticipantProfiles(ChatRoomDto chatRoomDetails) {
        if (chatRoomDetails == null) {
            return;
        }
        Long opponentId = chatRoomDetails.getOpponentId();
        if (opponentId != null) {
            profileCache.put(opponentId, chatRoomDetails);
        }
    }

    public void initializeMessages(List<ChatMessageDto> dtos, Long myUserId) {
        if (dtos == null || dtos.isEmpty()) {
            messages.setValue(new ArrayList<>());
            return;
        }

        List<ChatMessage> finalMessageList = new ArrayList<>();
        Long lastTimestamp = null; // 이전 메시지의 타임스탬프 (날짜 비교용)

        for (ChatMessageDto dto : dtos) {
            int viewType = dto.getSenderId().equals(myUserId) ?
                    ChatMessage.VIEW_TYPE_SENT : ChatMessage.VIEW_TYPE_RECEIVED;
            long currentTimestamp = convertToTimestamp(dto.getCreatedAt());

            ChatMessage newMsg = convertDtoToMessage(dto, myUserId, currentTimestamp);

            if (lastTimestamp == null || !isSameDay(lastTimestamp, currentTimestamp)) {
                // 첫 메시지이거나 이전 메시지와 날짜가 다르면 날짜 라벨 추가
                finalMessageList.add(ChatMessage.dateLabel(formatDateLabel(currentTimestamp)));
            }

            finalMessageList.add(newMsg);
            lastTimestamp = currentTimestamp;
        }

        messages.setValue(finalMessageList);
    }

    // 웹소켓 메시지 수신 및 LiveData 업데이트 함수
    public void addIncomingMessage(ChatMessageDto dto, Long myUserId) {
        List<ChatMessage> currentList = messages.getValue() == null ?
                new ArrayList<>() : messages.getValue();

        int viewType = dto.getSenderId().equals(myUserId) ?
                ChatMessage.VIEW_TYPE_SENT : ChatMessage.VIEW_TYPE_RECEIVED;

        long timestamp = convertToTimestamp(dto.getCreatedAt());

        ChatMessage newMsg = convertDtoToMessage(dto, myUserId, timestamp);

        List<ChatMessage> updatedList = new ArrayList<>(currentList);

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

    private long convertToTimestamp(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            Log.e("ChatViewModel", "DateTime is null, returning current time.");
            return System.currentTimeMillis();
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, SERVER_DATETIME_FORMATTER);

            return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            Log.e("ChatViewModel", "Failed to parse date string: " + dateTime, e);
            return System.currentTimeMillis();
        }
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

    public String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREA);
        return sdf.format(new Date(timeMillis));    }

    private ChatMessage convertDtoToMessage(ChatMessageDto dto, Long myUserId, long timestamp) {
        int viewType;
        boolean isMyMessage = dto.getSenderId().equals(myUserId);

        if (dto.getType() != null && dto.getType().toString().equals("MATCH_CARD")) {
            viewType = isMyMessage ?
                    ChatMessage.VIEW_TYPE_MATCH_SENT :
                    ChatMessage.VIEW_TYPE_MATCH_RECEIVED;
        } else {
            // 일반 메시지
            viewType = dto.getSenderId().equals(myUserId) ?
                    ChatMessage.VIEW_TYPE_SENT : ChatMessage.VIEW_TYPE_RECEIVED;
        }

        ChatMessage.MatchInfo matchInfo = null;
        if (viewType == ChatMessage.VIEW_TYPE_MATCH_SENT || viewType == ChatMessage.VIEW_TYPE_MATCH_RECEIVED) {
            matchInfo = parseCardJson(dto.getContent());
        }

        return new ChatMessage(
                dto.getId(),
                viewType,
                dto.getContent(),
                timestamp,
                dto.getSenderId(),
                formatTime(timestamp),
                matchInfo
        );
    }

    public LiveData<List<ChatMessage>> getMessages() { return messages; }
    public ChatRoomDto getProfile(Long senderId) {
        return profileCache.get(senderId);
    }

    public void sendMatchCardMessage(Long chatRoomId, Long senderId, String title, String date, String time, String place) {
        final String status = "CREATED";
        String contentJson = createCardJson(title, date, time, place, status);

        ChatMessageRequest request = new ChatMessageRequest();
        request.setSenderId(senderId);
        request.setContent(contentJson);

        long currentTime = System.currentTimeMillis();
        ChatMessage cardMessage = createCardMessage(
                null,
                senderId,
                status,
                title,
                date,
                time,
                place,
                currentTime
        );
        addLocally(cardMessage);
    }

    public String createCardJson(String title, String date, String time, String place, String status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MATCH_CARD");

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("date", date);
        data.put("time", time);
        data.put("place", place);
        data.put("status", status);

        payload.put("data", data);

        return gson.toJson(payload);
    }

    private ChatMessage.MatchInfo parseCardJson(String contentJson) {
        Long tempId = System.currentTimeMillis();

        try {
            // JSON 문자열 전체 구조를 JsonPayload로 파싱
            JsonPayload payload = gson.fromJson(contentJson, JsonPayload.class);
            if (payload == null || payload.getData() == null) return null;

            Map<String, String> data = payload.getData();

            // MatchInfo 객체에 필요한 데이터를 Map에서 추출
            return new ChatMessage.MatchInfo(
                    String.valueOf(tempId),
                    null,
                    data.get("status"),
                    data.get("title"),
                    data.get("date"),
                    data.get("time"),
                    data.get("place")
            );
        } catch (Exception e) {
            // 파싱 실패 시, null 반환하여 카드 뷰를 띄우지 않도록 처리
            Log.e("ChatViewModel", "경기 카드 JSON 파싱 오류: " + contentJson, e);
            return null;
        }
    }

    private ChatMessage createCardMessage(Long messageId, Long senderId, String status, String title, String date, String time, String place, long timestamp) {

        ChatMessage.MatchInfo matchInfo = new ChatMessage.MatchInfo(
                String.valueOf(timestamp), // tempId
                senderId,
                status,
                title,
                date,
                time,
                place
        );

        return new ChatMessage(
                messageId,
                ChatMessage.VIEW_TYPE_MATCH_SENT,
                title,
                timestamp,
                senderId,
                formatTime(timestamp),
                matchInfo
        );
    }

    private void addLocally(ChatMessage newMsg) {
        List<ChatMessage> currentList = messages.getValue() == null ?
                new ArrayList<>() : messages.getValue();
        List<ChatMessage> updatedList = new ArrayList<>(currentList);

        Long newMsgTimestamp = newMsg.getTimestamp();
        ChatMessage lastMessage = null;

        for (int i = currentList.size() - 1; i >= 0; i--) {
            if (currentList.get(i).getViewType() != ChatMessage.VIEW_TYPE_DATE) {
                lastMessage = currentList.get(i);
                break;
            }
        }

        if (lastMessage == null || !isSameDay(lastMessage.getTimestamp(), newMsgTimestamp)) {
            updatedList.add(ChatMessage.dateLabel(formatDateLabel(newMsgTimestamp)));
        }

        updatedList.add(newMsg);
        messages.postValue(updatedList);
    }
}
