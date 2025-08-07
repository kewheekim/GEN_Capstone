package com.example.rally.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.CheckIdRequest;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends Fragment {
    private EditText etSetId, etSetPw, etConfirmPw;
    private TextView tvSetId, tvSetPw, tvConfirmPw;
    private Button btnDouble, btnNext;
    private ImageButton btnBack;
    private RetrofitClient retrofitClient;

    public SignupFragment() {
        super(R.layout.fragment_signup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final int defaultLabel = Color.parseColor("#AAAAAA");

        etSetId     = view.findViewById(R.id.et_set_id);
        btnDouble   = view.findViewById(R.id.btn_double);
        etSetPw     = view.findViewById(R.id.et_set_pw);
        etConfirmPw = view.findViewById(R.id.et_confirm_pw);
        btnNext     = view.findViewById(R.id.btn_next);
        btnBack = view.findViewById(R.id.btn_back);
        tvSetId     = view.findViewById(R.id.tv_set_id);
        tvSetPw     = view.findViewById(R.id.tv_set_pw);
        tvConfirmPw = view.findViewById(R.id.tv_confirm_pw);

        btnNext.setEnabled(false);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).backToLogin();
            }
        });

        // 포커스 변경 시 유효성 검사 & 에러 표시
        etSetId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String id = etSetId.getText().toString().trim();
                boolean valid = isValidId(id);
                etSetId.setActivated(!valid);
                tvSetId.setTextColor(valid ? defaultLabel : Color.RED);
            }
        });

        etSetPw.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String pw = etSetPw.getText().toString().trim();
                boolean valid = isValidPassword(pw);
                etSetPw.setActivated(!valid);
                tvSetPw.setTextColor(valid ? defaultLabel : Color.RED);
            }
        });

        etConfirmPw.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String pw        = etSetPw.getText().toString().trim();
                String confirmPw = etConfirmPw.getText().toString().trim();
                boolean match = pw.equals(confirmPw);
                etConfirmPw.setActivated(!match);
                tvConfirmPw.setTextColor(match ? defaultLabel : Color.RED);
            }
        });

        // 비밀번호 확인 필드만 실시간 검사
        etConfirmPw.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                String pw        = etSetPw.getText().toString().trim();
                String confirmPw = s.toString().trim();
                boolean match    = pw.equals(confirmPw);

                etConfirmPw.setActivated(!match);
                if (match) {
                    tvConfirmPw.setText(" ");
                    tvConfirmPw.setTextColor(defaultLabel);
                } else {
                    tvConfirmPw.setText("비밀번호가 일치하지 않습니다.");
                    tvConfirmPw.setTextColor(Color.RED);
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 중복확인 임시 메서드
        btnDouble.setOnClickListener(v -> {
            String id = etSetId.getText().toString().trim();
            CheckIdRequest request = new CheckIdRequest(id);

            if (!isValidId(id)) {
                etSetId.setActivated(true);
                tvSetId.setTextColor(Color.parseColor("red"));
                return;
            }
            checkIdDuplicate(request);
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String id        = etSetId.getText().toString().trim();
                String pw        = etSetPw.getText().toString().trim();
                String confirmPw = etConfirmPw.getText().toString().trim();

                boolean valid = isValidId(id) && isValidPassword(pw) && pw.equals(confirmPw);
                btnNext.setEnabled(valid); // 조건 만족 시 버튼 활성화
                btnNext.setTextColor(valid ? Color.parseColor("#FFFFFF") : Color.parseColor("#AAAAAA"));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        etSetId.addTextChangedListener(watcher);
        etSetPw.addTextChangedListener(watcher);
        etConfirmPw.addTextChangedListener(watcher);

        // 다음 단계
        btnNext.setOnClickListener(v -> {
            String id        = etSetId.getText().toString().trim();
            String pw        = etSetPw.getText().toString().trim();
            String confirmPw = etConfirmPw.getText().toString().trim();

            if (!isValidId(id)) {
                etSetId.setActivated(true);
                tvSetId.setTextColor(Color.parseColor("red"));
                return;
            }
            if (!isValidPassword(pw)) {
                etSetPw.setActivated(true);
                tvSetPw.setTextColor(Color.parseColor("red"));
                return;
            }
            if (!pw.equals(confirmPw)) {
                etConfirmPw.setActivated(true);
                tvConfirmPw.setTextColor(Color.parseColor("red"));
                return;
            }

           if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity())
                        .showSetNickname(id, pw); // id, pw 담아서 다음 setNicknameFragment로
            }
        });
    }

    // 아이디 형식(6~20자 영문+숫자) 검사
    private boolean isValidId(String id) {
        return id.matches("^[A-Za-z0-9]{6,20}$");
    }

    // 비밀번호 형식(8~16자 영문 대소문자, 숫자, .!@#~) 검사
    private boolean isValidPassword(String pw) {
        return pw.matches("^[A-Za-z0-9.!@#~]{8,16}$");
    }

    // 서버에 ID 중복 체크 요청 TODO: 토스트 대신 텍스트필드로, 중복된 아이디일 때 빨강 / 통과 시 회색으로 응답 구분
    private void checkIdDuplicate(CheckIdRequest request) {
        ApiService apiService = RetrofitClient.getClient("http://10.0.2.2:8080/").create(ApiService.class);

        apiService.checkUserId(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), responseBody, Toast.LENGTH_SHORT).show();
                    } else {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "에러 없음";
                        JSONObject obj = new JSONObject(errorBody);
                        String message = obj.optString("message", "에러 발생");
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "응답 처리 중 오류 발생", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
