package com.gen.rally.service;

import com.gen.rally.config.jwt.JwtProvider;
import com.gen.rally.dto.auth.TokenResponse;
import com.gen.rally.dto.auth.NaverLoginResponse;
import com.gen.rally.dto.auth.NaverTokenResponse;
import com.gen.rally.dto.auth.NaverUserInfoDto;
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
public class NaverService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://nid.naver.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    public NaverTokenResponse getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);

        return webClient.post()
                .uri("/oauth2.0/token")
                .bodyValue(formData)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INVALID_NAVER_CODE)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)))
                .bodyToMono(NaverTokenResponse.class)
                .block();
    }

    public NaverUserInfoDto getUserInfo(String accessToken) {
        return WebClient.create("https://openapi.naver.com")
                .get()
                .uri("/v1/nid/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INVALID_NAVER_CODE)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR)))
                .bodyToMono(NaverUserInfoDto.class)
                .block();
    }

    public NaverLoginResponse loginOrSignup(NaverUserInfoDto userInfo) {
        String id = userInfo.getResponse().getId();
        String name = userInfo.getResponse().getNickname();

        Optional<User> optionalUser = userRepository.findBySocialIdAndLoginType(id, LoginType.NAVER);

        User user;
        boolean isNew = false;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            user = new User();
            user.setSocialId(id);
            user.setUserId(id);
            user.setName(name);
            user.setLoginType(LoginType.NAVER);
            userRepository.save(user);
            isNew = true;
        }

        String subject = LoginType.NAVER + ":" + user.getSocialId();
        String accessToken = jwtProvider.generateAccessToken(subject);
        String refreshToken = jwtProvider.generateRefreshToken(subject);

        TokenResponse token = new TokenResponse(accessToken, refreshToken);
        return new NaverLoginResponse(token, isNew, user.getName(), user.getSocialId());
    }
}
