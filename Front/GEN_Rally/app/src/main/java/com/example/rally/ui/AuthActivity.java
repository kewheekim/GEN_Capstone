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

    // LoginFragment -> SignupFragment
    public void showSignup() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_fragment_container, new SignupFragment())
                .addToBackStack(null)  // 뒤로가기 시 LoginFragment로 복귀
                .commit();
    }

    // SignupFragment -> LoginFragment  (뒤로가기)
    public void backToLogin() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    // SignupFragment -> SetNicknameFragment
    public void showSetNickname(String id, String pw) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, SetNicknameFragment.newInstance(id, pw))
                .addToBackStack(null)
                .commit();
    }

    // SetNicknameFragment -> SignupFragment (뒤로가기)
    public void backToSignup(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    // SetNicknameFragment -> SetProfileImageFragment
    public void showSetProfile(String id, String pw, String name){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, SetProfileImageFragment.newInstance(id, pw, name))
                .addToBackStack(null)
                .commit();
    }

    // SetProfileImageFragment -> SetNicknameFragment (뒤로가기)
    public void backToNickname(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    // SetProfileImageFragment -> SetGenderFragment
    public void showSetGender(String id, String pw, String name, byte[] image){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, SetGenderFragment.newInstance(id, pw, name, image))
                .addToBackStack(null)
                .commit();
    }

    // SetGenderFragment -> SetProfileImageFragment (뒤로가기)
    public void backToImage(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    // SetGenderFragment -> SetPrimaryFragment
    public void showSetPrimary(String id, String pw, String name, byte[] image, String gender){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, SetPrimaryFragment.newInstance(id, pw, name, image, gender))
                .addToBackStack(null)
                .commit();
    }

    // SetPrimaryFragment -> SetGenderFragment (뒤로가기)
    public void backToGender(){
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        }
    }

    // SignupCompleteFragment
    public void showComplete(String name, byte[] image){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_fragment_container, SignupCompleteFragment.newInstance(name, image))
                .addToBackStack(null)
                .commit();
    }
}