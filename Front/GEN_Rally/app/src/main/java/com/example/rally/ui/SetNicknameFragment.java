package com.example.rally.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

// SIG_002
public class SetNicknameFragment extends Fragment {
    private static final String ARG_ID = "id";
    private static final String ARG_PW = "pw";

    private String userId;
    private String userPw;

    private EditText etNickname;
    private View nameBox;
    private Button btnNext;
    private ImageButton btnBack;

    public SetNicknameFragment() {
        super(R.layout.fragment_signup_nickname);
    }

    // 이전에서 인스턴스 받아옴 (id, pw)
    public static SetNicknameFragment newInstance(String id, String pw) {
        SetNicknameFragment fragment = new SetNicknameFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_PW, pw);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_ID);
            userPw = getArguments().getString(ARG_PW);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etNickname = view.findViewById(R.id.et_nickname);
        btnNext = view.findViewById(R.id.btn_next);
        nameBox = view.findViewById(R.id.et_box);
        btnNext.setEnabled(false);
        btnBack = view.findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).backToSignup();
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String nickname = etNickname.getText().toString().trim();

                boolean valid = isValidName(nickname);
                btnNext.setEnabled(valid); // 조건 만족 시 버튼 활성화
                btnNext.setTextColor(valid ? Color.parseColor("#FFFFFF") : Color.parseColor("#AAAAAA"));
                nameBox.setBackgroundResource(valid ? R.drawable.bg_view_active : R.drawable.bg_button_inactive);
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        etNickname.addTextChangedListener(watcher);

        btnNext.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            if (!isValidName(nickname)) {
                Toast.makeText(getContext(), "닉네임은 2~12자 이내로 입력해 주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showSetProfile(userId, userPw, nickname);
            }
        });
    }
    public boolean isValidName(String name){
        if (name == null) return false;
        name = name.trim();
        return name.length() >= 2 && name.length() <= 12;
    }
}
