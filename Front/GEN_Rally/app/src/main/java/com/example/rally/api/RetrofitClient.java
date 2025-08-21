package com.example.rally.api;

import android.content.Context;

import com.example.rally.auth.AuthInterceptor;
import com.example.rally.auth.TokenStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private RetrofitClient() {}
    private static final Map<String, Retrofit> PUBLIC = new ConcurrentHashMap<>();
    private static final Map<String, Retrofit> SECURE = new ConcurrentHashMap<>();

    // 기존 코드 호환, 인증 없을 때
    public static Retrofit getClient(String baseUrl) {
        return PUBLIC.computeIfAbsent(baseUrl, url -> {
            OkHttpClient ok = new OkHttpClient.Builder().build();
            return new Retrofit.Builder()
                    .baseUrl(url)
                    .client(ok)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        });
    }

    // 인증용, 헤더에 자동으로 인터셉터가 토큰 붙임
    public static Retrofit getSecureClient(Context con, String baseUrl) {
        return SECURE.computeIfAbsent(baseUrl, url -> {
            OkHttpClient.Builder ok = new OkHttpClient.Builder();
            try {
                TokenStore store = new TokenStore(con.getApplicationContext());
                ok.addInterceptor(new AuthInterceptor(store));
            } catch (Exception ignored) {
                // TokenStore 초기화 실패 시에도 요청은 진행(Authorization 헤더만 미첨부)
            }
            return new Retrofit.Builder()
                    .baseUrl(url)
                    .client(ok.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        });
    }
}
