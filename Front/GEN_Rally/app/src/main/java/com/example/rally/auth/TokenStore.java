package com.example.rally.auth;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class TokenStore {
    private static final String PREF = "encrypted_prefs";
    private static final String KEY_AT = "access_token";
    private static final String KEY_RT = "refresh_token";

    private final SharedPreferences prefs;

    public TokenStore(Context ctx) throws GeneralSecurityException, IOException {
        MasterKey masterKey = new MasterKey.Builder(ctx)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
        this.prefs = EncryptedSharedPreferences.create(
                ctx,
                PREF,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    // 토큰 저장
    public synchronized void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_AT, accessToken)
                .putString(KEY_RT, refreshToken)
                .apply();
    }

    public synchronized void saveAccessToken(String accessToken) {
        prefs.edit().putString(KEY_AT, accessToken).apply();
    }

    public synchronized String getAccessToken() { return prefs.getString(KEY_AT, null); }
    public synchronized String getRefreshToken() { return prefs.getString(KEY_RT, null); }

    // 전체 삭제
    public synchronized void clear() { prefs.edit().clear().apply(); }

}
