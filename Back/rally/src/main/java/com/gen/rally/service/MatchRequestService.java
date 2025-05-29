package com.gen.rally.service;

import com.gen.rally.dto.CandidatesResponseDto;
import com.gen.rally.dto.MatchRequestCreateDto;
import com.gen.rally.entity.MatchRequest;
import com.gen.rally.entity.User;
import com.gen.rally.repository.MatchRequestRepository;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchRequestService {
    private final MatchRequestRepository matchRequestRepository;
    private final UserRepository userRepository;

    public Long createMatchRequest(String userId, MatchRequestCreateDto dto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        MatchRequest matchRequest = new MatchRequest();
        matchRequest.setUser(user);
        matchRequest.setGameType(dto.getGameType());
        matchRequest.setGameStyle(dto.getGameStyle());
        matchRequest.setSameGender(dto.isSameGender());
        matchRequest.setGameDate(dto.getGameDate());
        matchRequest.setStartTime(dto.getStartTime());
        matchRequest.setEndTime(dto.getEndTime());
        matchRequest.setPlace(dto.getPlace());
        matchRequest.setLatitude(dto.getLatitude());
        matchRequest.setLongitude(dto.getLongitude());

        // User에서 가져온 정보 저장
        matchRequest.setSkill(user.getSkill());
        matchRequest.setGender(user.getGender());

        MatchRequest saved = matchRequestRepository.save(matchRequest);
        return saved.getRequestId();
    }


    public List<CandidatesResponseDto> findCandidates(MatchRequestCreateDto dto) {
        User user = userRepository.findByUserId(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));
        int userSkill = user.getSkill();

        List<MatchRequest> matchedRequests = matchRequestRepository.findByGameDateAndGameTypeAndSameGender(
                dto.getGameDate(),
                dto.getGameType(),
                dto.isSameGender()
        );

        return matchedRequests.stream()
                .map(req -> CandidatesResponseDto.builder()
                        .name(req.getUser().getName())
                        .profileImage(req.getUser().getProfileImageUrl()) // 예: String 형태로 가정
                        .gender(req.getUser().getGender())
                        .tier(req.getUser().getTier())
                        .winningRate(0.8) // TODO: 실제 계산 필요
                        .skillGap(Math.abs(userSkill- req.getSkill()))
                        .time(req.getStartTime() + "~" + req.getEndTime())
                        .isSameTime(/* 비교 로직 필요 */ false)
                        .place(req.getPlace())
                        .isSamePlace(dto.getPlace().equals(req.getPlace()))
                        .gameStyle(req.getGameStyle())
                        .mannerScore(req.getUser().getManner())
                        .build()
                )
                .collect(Collectors.toList());
    }
}
