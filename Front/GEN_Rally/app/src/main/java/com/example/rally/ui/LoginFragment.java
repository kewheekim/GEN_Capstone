package com.example.rally.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.GeneralLoginRequest;
import com.example.rally.dto.GeneralLoginResponse;
import com.example.rally.dto.SocialLoginRequest;
import com.example.rally.dto.SocialLoginResponse;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.NaverIdLoginSDK;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.model.ClientError;
import com.kakao.sdk.common.model.ClientErrorCause;
import com.kakao.sdk.user.UserApiClient;


import java.io.IOException;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    private ApiService apiService; // 전역 변수로 선언

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView btnGoSignup = view.findViewById(R.id.tv_signup);
        EditText etId = view.findViewById(R.id.et_enter_id);
        EditText etPw = view.findViewById(R.id.et_enter_pw);
        Button btnLogin = view.findViewById(R.id.btn_login);
        ImageButton btnNaver = view.findViewById(R.id.btn_naver);
        ImageButton btnKakao = view.findViewById(R.id.btn_kakao);

        apiService = RetrofitClient.getClient(BuildConfig.API_BASE_URL).create(ApiService.class);

        btnGoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).showSignup();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = etId.getText().toString().trim();
                String pw = etPw.getText().toString().trim();

                if (id.isEmpty() || pw.isEmpty()) {
                    Toast.makeText(getContext(), "아이디와 비밀번호를 모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                } else {
                    GeneralLoginRequest request = new GeneralLoginRequest();
                    request.setUserId(id);
                    request.setPassword(pw);

                    login(request);
                }
            }
        });

        // 네이버 로그인
        btnNaver.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                NaverIdLoginSDK.INSTANCE.authenticate(requireContext(), new OAuthLoginCallback() {
                    @Override
                    public void onSuccess() {
                        String accessToken = NaverIdLoginSDK.INSTANCE.getAccessToken();
                        Log.d("Naver", "SDK 로그인 성공: " + accessToken);
                        sendSocialTokenToServer(accessToken, "NAVER");
                    }

                    @Override
                    public void onFailure(int httpStatus, String message) {
                        String errorCode = NaverIdLoginSDK.INSTANCE.getLastErrorCode().getCode();
                        String errorDesc = NaverIdLoginSDK.INSTANCE.getLastErrorDescription();
                        Log.e("Naver", "SDK 로그인 실패: " + errorCode + ", " + errorDesc);
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        onFailure(errorCode, message);
                    }
                });

            }
        });

        // 카카오 로그인
        btnKakao.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Function2<OAuthToken, Throwable, Unit> callback = (token, error) -> {
                    if (error != null) {
                        Log.e("Kakao", "카카오 로그인 실패", error);
                    } else if (token != null) {
                        Log.d("Kakao", "SDK 로그인 성공: " + token.getAccessToken());
                        sendSocialTokenToServer(token.getAccessToken(), "KAKAO");
                    }
                    return null;
                };

                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(requireContext())) {
                    UserApiClient.getInstance().loginWithKakaoTalk(requireContext(), (token, error) -> {
                        if (error != null) {
                            if (error instanceof ClientError && ((ClientError) error).getReason() == ClientErrorCause.Cancelled) {
                                return null; // 사용자 취소
                            }
                            // 앱 로그인 실패 시 웹으로 재시도
                            UserApiClient.getInstance().loginWithKakaoAccount(requireContext(), callback);
                        } else if (token != null) {
                            Log.d("Kakao", "카톡 앱 로그인 성공");
                            sendSocialTokenToServer(token.getAccessToken(), "KAKAO");
                        }
                        return null;
                    });
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(requireContext(), callback);
                }
            }
        });
    }

    private void sendSocialTokenToServer(String accessToken, String provider) {
        SocialLoginRequest request = new SocialLoginRequest(accessToken);
        Call<SocialLoginResponse> call;

        if (provider.equals("KAKAO")) {
            call = apiService.loginKakao(request);
        } else {
            call = apiService.loginNaver(request);
        }

        call.enqueue(new Callback<SocialLoginResponse>() {
            @Override
            public void onResponse(Call<SocialLoginResponse> call, Response<SocialLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SocialLoginResponse body = response.body();

                    try {
                        TokenStore tokenStore = new TokenStore(requireContext().getApplicationContext());
                        tokenStore.saveTokens(body.getToken().getAccessToken(), body.getToken().getRefreshToken(), 0L);
                    } catch (Exception e) {
                        Log.e("SocialLogin", "토큰 저장 실패", e);
                    }

                    if (body.isNew()) {
                        Log.d("SocialLogin", "신규 유저 -> 닉네임 설정으로 이동");
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).showNicknameSetting(body.getNickname());
                        }
                    } else {
                        Log.d("SocialLogin", "기존 유저 -> 메인으로 이동");
                        Toast.makeText(getContext(), body.getNickname() + "님 환영합니다!", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).loginSuccess();
                        }
                    }
                } else {
                    Log.e("SocialLogin", "서버 응답 오류: " + response.code());
                    try {
                        Log.e("SocialLogin", "에러 내용: " + response.errorBody().string());
                    } catch (IOException e) {}
                    Toast.makeText(getContext(), "로그인 실패: 서버 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SocialLoginResponse> call, Throwable t) {
                Log.e("SocialLogin", "통신 실패", t);
                Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login(GeneralLoginRequest request){
        ApiService apiService = RetrofitClient.getClient(BuildConfig.API_BASE_URL).create(ApiService.class);

        apiService.login(request).enqueue(new Callback<GeneralLoginResponse>() {
            @Override
            public void onResponse(Call<GeneralLoginResponse> call, Response<GeneralLoginResponse> response) {
                int code = response.code();
                GeneralLoginResponse body = response.body();

                if(code==200){
                    String name = body.getName();

                    try{
                        TokenStore tokenStore = new TokenStore(requireContext().getApplicationContext());
                        tokenStore.saveTokens(body.getAccessToken(), body.getRefreshToken(), body.getUserId());
                    }catch(Exception e){
                        Log.e("SignUp", "토큰 저장 실패", e);
                    }
                    Toast.makeText(getContext(),name+"님, 로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    if (getActivity() instanceof AuthActivity) {
                        ((AuthActivity) getActivity()).loginSuccess();
                    }
                }else{
                    try {
                        String rawJson = response.errorBody().string();
                        Log.e("LogIn", "서버 에러 응답: " + rawJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<GeneralLoginResponse> call, Throwable t) {
                Log.e("LogIn", t.getMessage());
                Toast.makeText(getContext(),
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
