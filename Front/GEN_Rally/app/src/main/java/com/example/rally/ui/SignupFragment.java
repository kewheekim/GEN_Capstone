package com.example.rally.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class SignupFragment extends Fragment {
    private EditText etSetId, etSetPw, etConfirmPw;
    private Button btnDouble, btnNext;

    public SignupFragment() {
        super(R.layout.fragment_signup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSetId     = view.findViewById(R.id.et_set_id);
        btnDouble   = view.findViewById(R.id.btn_double);
        etSetPw     = view.findViewById(R.id.et_set_pw);
        etConfirmPw = view.findViewById(R.id.et_confirm_pw);
        btnNext     = view.findViewById(R.id.btn_next);

        // 중복확인 임시 메서드
        btnDouble.setOnClickListener(v -> {
            String id = etSetId.getText().toString().trim();
            if (!isValidId(id)) {
                Toast.makeText(getContext(),
                        "6~20자 영문, 숫자로 입력해 주세요",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            checkIdDuplicate(id);
        });

        // 다음 단계
        btnNext.setOnClickListener(v -> {
            String id        = etSetId.getText().toString().trim();
            String pw        = etSetPw.getText().toString().trim();
            String confirmPw = etConfirmPw.getText().toString().trim();

            if (!isValidId(id)) {
                Toast.makeText(getContext(),
                        "6~20자 영문, 숫자로 입력해 주세요",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidPassword(pw)) {
                Toast.makeText(getContext(),
                        "8~16자 영문 대소문자, 숫자, 특수문자(.!@#~)로 입력해 주세요",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pw.equals(confirmPw)) {
                Toast.makeText(getContext(),
                        "비밀번호가 일치하지 않습니다",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity())
                        .showSignupInfo(id, pw); // 예: 기본 정보 입력 Fragment 로 전환
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

    // 서버에 ID 중복 체크 요청
    private void checkIdDuplicate(String id) {
        // TODO: Retrofit 등으로 API 호출
        // 응답 성공 시: Toast 또는 UI 변경해서 “사용 가능” 표시
        // 응답 실패 시: Toast로 “이미 사용 중” 안내
        Toast.makeText(getContext(),
                "중복 확인 API 호출: " + id,
                Toast.LENGTH_SHORT).show();
    }
}
