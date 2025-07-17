package com.gen.rally.service;

import com.gen.rally.dto.CandidatesResponseDto;
import com.gen.rally.dto.MatchRequestCreateDto;
import com.gen.rally.entity.*;
import com.gen.rally.enums.GameStyle;
import com.gen.rally.enums.GameType;
import com.gen.rally.enums.State;
import com.gen.rally.repository.GameRepository;
import com.gen.rally.repository.MatchRequestRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchRequestService {
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public Long createMatchRequest(MatchRequestCreateDto dto) {
        User user = userRepository.findByUserId(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        MatchRequest matchRequest = new MatchRequest();
        matchRequest.setUser(user);
        matchRequest.setGameType(GameType.fromCode(dto.getGameType()));
        matchRequest.setGameStyle(GameStyle.fromCode(dto.getGameStyle()));
        matchRequest.setSameGender(dto.isSameGender());
        matchRequest.setGameDate(dto.getGameDate());
        matchRequest.setStartTime(dto.getStartTime());
        matchRequest.setEndTime(dto.getEndTime());
        matchRequest.setPlace(dto.getPlace());
        matchRequest.setLatitude(dto.getLatitude());
        matchRequest.setLongitude(dto.getLongitude());

        // User에서 가져온 정보 저장
        matchRequest.setSkill(user.getSkill());
        matchRequest.setGender(user.getGender().getCode());

        // 상태 저장
        matchRequest.setState(State.대기);

        MatchRequest saved = matchRequestRepository.save(matchRequest);
        return saved.getRequestId();
    }


    public List<CandidatesResponseDto> findCandidates(MatchRequestCreateDto userInput) {
        User user = userRepository.findByUserId(userInput.getUserId()).orElseThrow();
        GameStyle enumGameStyle = GameStyle.fromCode(userInput.getGameStyle());       // one-hot 인코딩 사용하기 위한 enum 변환

//        // 1. 과거에 사용자가 동일한 날짜, 겹치는 시간의 신청을 했는지 확인
//        List<MatchRequest> duplicates = matchRequestRepository.findOverlappingRequests(
//                userInput.getUserId(),
//                userInput.getGameDate(),
//                userInput.getStartTime(),
//                userInput.getEndTime()
//        );
//
//        // 2. 이미 해당 조건의 신청이 있으면 에러 발생
//        if (!duplicates.isEmpty()) {
//            throw new IllegalStateException("동일 조건의 매칭 신청 이력이 존재합니다.");
//        }

        List<MatchRequest> candidates = matchRequestRepository.findAll().stream()
                // (1)  대기 상태이고 날짜, 경기 유형 일치
                .filter(r -> r.getGameDate().equals(userInput.getGameDate()) && r.getGameType().getCode() == userInput.getGameType() && r.getState()==State.대기)

                // (2) sameGender 조건 필터링
                .filter(r -> {
                    // 신청자가 같은 성별을 원할 경우
                    if (userInput.isSameGender() == true) {
                        return r.getUser().getGender() == user.getGender();
                    } else {
                        // 신청자가 상대 성별 상관없음 택한 경우 -> 똑같이 상관없음을 택했거나, 동일한 성별의 상대만을 남김
                        return r.isSameGender() == false || (r.isSameGender() == true && r.getUser().getGender() == user.getGender());
                    }
                })

                // (3) 거리 10km 이내
                .filter(r -> haversine(userInput.getLatitude(), userInput.getLongitude(),
                        r.getLatitude(), r.getLongitude()) <= 10)

                // (4) 시간 1시간 이상 겹침
                .filter(r -> Math.max(0, Math.min(userInput.getEndTime(), r.getEndTime()) -
                        Math.max(userInput.getStartTime(), r.getStartTime())) >= 1)

                // (5) 기술 점수 10점 이내 차이
                .filter(r -> Math.abs(r.getUser().getSkill() - user.getSkill()) <= 10)

                .collect(Collectors.toList());

        // 코사인 유사도 기반 유사도 계산 ( startTime + endTime + skill + gameStyle)
        List<MatchRequest> topCandidates = candidates.stream()
                .map(r -> Map.entry(r, cosineSimilarity(
                        new double[]{
                                user.getSkill() / 100.0,
                                userInput.getStartTime() / 24.0,
                                userInput.getEndTime() / 24.0,
                                // GameStyle One-hot Encoding
                                (userInput.getGameStyle() == 1 || userInput.getGameStyle() == 0) ? 1.0 : 0.0, // "편하게" 또는 "상관없음"
                                (userInput.getGameStyle() == 2 || userInput.getGameStyle() == 0) ? 1.0 : 0.0  // "열심히" 또는 "상관없음"
                        },
                        new double[]{
                                r.getUser().getSkill() / 100.0,
                                r.getStartTime() / 24.0,
                                r.getEndTime() / 24.0,
                                (r.getGameStyle().getCode() == 1 || r.getGameStyle().getCode() == 0) ? 1.0 : 0.0,
                                (r.getGameStyle().getCode() == 2 || r.getGameStyle().getCode() == 0) ? 1.0 : 0.0
                        })))
                .sorted(Map.Entry.<MatchRequest, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

         // dto 변환
        return topCandidates.stream()
                .map(r -> {
                    double distance = haversine(userInput.getLatitude(), userInput.getLongitude(),
                            r.getLatitude(), r.getLongitude());
                    double winningRate = 0;
                    int skillGap = 0;
                    if(userInput.getGameType() == 0)
                        winningRate = calculateWinningRate(r.getUser().getUserId());
                    else
                        skillGap=Math.abs(r.getUser().getSkill()*2-r.getSkill()*2);
                    boolean isSameTier;
                    isSameTier= (user.getTier()==r.getUser().getTier()) ? true : false;

                    return new CandidatesResponseDto(r, userInput, distance, winningRate, skillGap, isSameTier);
                })
                .collect(Collectors.toList());
    }

    // cosine 유사도
    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Haversine 거리 계산 (단위: km)
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 최근 5경기 승률 계산
    private double calculateWinningRate(String userId) {
        List<Game> games = gameRepository.findRecentGamesByUserId(userId, PageRequest.of(0, 5));
        long wins = games.stream()
                .filter(g -> g.getWinner().getUserId().equals(userId))
                .count();

        return games.isEmpty() ? 0.0 : (wins * 100.0 / games.size());
    }
}
