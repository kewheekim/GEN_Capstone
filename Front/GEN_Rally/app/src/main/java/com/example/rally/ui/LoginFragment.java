package com.example.rally.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView btnGoSignup = view.findViewById(R.id.tv_signup);
        EditText etId = view.findViewById(R.id.et_enter_id);
        EditText etPw = view.findViewById(R.id.et_enter_pw);
        Button btnLogin = view.findViewById(R.id.btn_login);

        btnGoSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof AuthActivity) {
                    ((AuthActivity) getActivity()).showSignup();
                }
            }
        });

        // TODO: 로그인 로직 처리 후,
        // 신규 소셜 유저라면 ((AuthActivity)getActivity()).showSignup();
        // 기존 유저면 MainActivity로 이동
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
    }

    public void login(GeneralLoginRequest request){
        ApiService apiService = RetrofitClient.getClient(BuildConfig.API_BASE_URL).create(ApiService.class);

        apiService.login(request).enqueue(new Callback<GeneralLoginResponse>() {
            @Override
            public void onResponse(Call<GeneralLoginResponse> call, Response<GeneralLoginResponse> response) {
                int code = response.code();
                GeneralLoginResponse body = response.body();
                String name = body.getName();

                if(code==200){
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
