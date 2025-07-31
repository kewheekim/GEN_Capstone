package com.example.rally.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.rally.R;

import javax.annotation.Nullable;

// LOG_001, SIG_001~
public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // 최초 실행 시 LoginFragment 표시
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_fragment_container, new LoginFragment())
                    .commit();
        }
    }

    // LoginFragment → SignupFragment
    public void showSignup() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new SignupFragment())
                .addToBackStack(null)  // 뒤로가기 시 LoginFragment로 복귀
                .commit();
    }

    // SignupFragment → LoginFragment 전환
    public void showLogin() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    public void showSignupInfo(String id, String pw) {
        // 예: 기본 정보 입력 Fragment 로 전환
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, SetNicknameFragment.newInstance(id, pw))
                .addToBackStack(null)
                .commit();
    }
}