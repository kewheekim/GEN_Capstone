package com.example.rally.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.CheckNicknameResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private TextView tvError;
    private RetrofitClient retrofitClient;

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
        tvError = view.findViewById(R.id.tv_error);
        btnNext.setEnabled(false);
        btnNext.setTextColor(Color.WHITE);
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

                if (valid) {
                    nameBox.setBackgroundResource(R.drawable.bg_view_active); // 초록색 박스
                    tvError.setVisibility(View.GONE);
                } else {
                    nameBox.setBackgroundResource(R.drawable.bg_button_inactive);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("닉네임은 2~12자의 한글, 영문, 숫자로 설정해 주세요");
                    tvError.setTextColor(Color.parseColor("red"));
                }

                btnNext.setEnabled(valid);
                btnNext.setTextColor(valid ? Color.WHITE : Color.parseColor("#AAAAAA"));
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };

        etNickname.addTextChangedListener(watcher);

        btnNext.setOnClickListener(v -> {
            ApiService apiService = RetrofitClient.getClient("http://10.0.2.2:8080/").create(ApiService.class);
            String nickname = etNickname.getText().toString().trim();

            if (!isValidName(nickname)) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText("닉네임은 2~12자 이내로 입력해 주세요");
                tvError.setTextColor(Color.parseColor("red"));
                return;
            }

            apiService.checkNickname(nickname).enqueue(new Callback<CheckNicknameResponse>() {
                @Override
                public void onResponse(Call<CheckNicknameResponse> call, Response<CheckNicknameResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Toast.makeText(getContext(), "네트워크 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    CheckNicknameResponse body = response.body();
                    if (body.available) {
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).showSetProfile(userId, userPw, nickname);
                        }
                    } else {
                        nameBox.setBackgroundResource(R.drawable.bg_button_inactive);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText(body.message);
                        tvError.setTextColor(Color.parseColor("red"));
                    }
                }

                @Override
                public void onFailure(Call<CheckNicknameResponse> call, Throwable t) {
                    Log.e("NickCheck", "failed", t);
                    Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
    public boolean isValidName(String name){
        if (name == null) return false;
        name = name.trim();
        return name.length() >= 2 && name.length() <= 12;
    }
}
