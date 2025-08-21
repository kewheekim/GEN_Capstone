package com.example.rally.auth;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

// 헤더에 자동으로 토큰 붙이는 인터셉터
public class AuthInterceptor implements Interceptor {
    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        /* 무인증 엔드포인트는 패스, 현재는 무인증이 더 많아서 인증을 구분해두기로 함
        String path = original.url().encodedPath();
        if (path.startsWith("/auth") || path.startsWith("/oauth") || path.startsWith("/public")) {
            return chain.proceed(original);
        }*/

        // 마커 헤더 확인
        boolean needsAuth = "true".equals(original.header("Requires-Auth"));
        // 마커 헤더 제거
        Request.Builder builder = original.newBuilder().removeHeader("Requires-Auth");

        if (!needsAuth) {
            // 인증 불필요: 그대로 진행
            return chain.proceed(builder.build());
        }

        // 인증 필요: 토큰 있으면 Authorization 붙이기
        String at = tokenStore.getAccessToken();
        if (at != null && !at.isEmpty()) {
            builder.header("Authorization", "Bearer " + at);
        }
        // 토큰이 없으면 그냥 진행
        return chain.proceed(builder.build());
    }
}
