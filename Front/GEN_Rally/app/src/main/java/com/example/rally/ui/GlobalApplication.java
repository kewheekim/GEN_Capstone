package com.example.rally.ui;

import android.app.Application;

import com.example.rally.BuildConfig;
import com.kakao.sdk.common.KakaoSdk;
import com.navercorp.nid.NaverIdLoginSDK;

public class GlobalApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this, BuildConfig.KAKAO_APP_KEY);
        NaverIdLoginSDK.INSTANCE.initialize(this,
                BuildConfig.NAVER_CLIENT_ID,
                BuildConfig.NAVER_CLIENT_SECRET,
                "Rally");
    }
}
