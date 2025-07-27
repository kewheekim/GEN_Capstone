package com.example.rally.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class LoginFragment extends Fragment {
    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button btnGoSignup = view.findViewById(R.id.tv_signup);
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
    }
}
