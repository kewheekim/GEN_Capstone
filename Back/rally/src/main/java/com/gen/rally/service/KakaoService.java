package com.gen.rally.service;

import com.gen.rally.config.jwt.JwtProvider;
import com.gen.rally.dto.KakaoLoginResponse;
import com.gen.rally.dto.KakaoTokenResponse;
import com.gen.rally.dto.KakaoUserInfoDto;
import com.gen.rally.dto.TokenResponse;
import com.gen.rally.entity.User;
import com.gen.rally.enums.LoginType;
import com.gen.rally.exception.CustomException;
import com.gen.rally.exception.ErrorCode;
import com.gen.rally.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://kauth.kakao.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();


    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoTokenResponse getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        return webClient.post()
                .uri("/oauth/token")
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INVALID_KAKAO_CODE)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)))
                .bodyToMono(KakaoTokenResponse.class)
                .block(); // 동기 방식으로 대기
    }

    public KakaoUserInfoDto getUserInfo(String accessToken) {
        return WebClient.create("https://kapi.kakao.com")
                .get()
                .uri("/v2/user/me")
                .header(HttpHeaders.AUTHORIZATION,"Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INVALID_KAKAO_CODE)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)))
                .bodyToMono(KakaoUserInfoDto.class)
                .block();
    }

    public KakaoLoginResponse loginOrSignup(KakaoUserInfoDto userInfo){
        String id = userInfo.getId();
        String name = userInfo.getKakaoAccount().getProfile().getNickname();

        Optional<User> optionalUser = userRepository.findBySocialId(id);

        User user;
        boolean isNew = false;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new User();
            user.setSocialId(id);
            user.setName(name);
            user.setLoginType(LoginType.KAKAO);
            userRepository.save(user);
            isNew = true;
        }
        String accessToken = jwtProvider.generateAccessToken(user.getSocialId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getSocialId());

        TokenResponse token = new TokenResponse(accessToken, refreshToken);

        return new KakaoLoginResponse(token, isNew, user.getName(), user.getSocialId());
    }
}
