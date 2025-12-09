package com.example.rally.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;
import com.example.rally.auth.TokenStore;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MyPageFragment extends Fragment {

    Button btnProfile;
    ImageButton btnChat, btnNoti, btnTier, btnHistory, btnNotiSet, btnLogout;

    public MyPageFragment(){

    }

    private TokenStore tokenStore;
    private long currentUserId = -1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        btnChat = view.findViewById(R.id.btn_chat);
        btnNoti = view.findViewById(R.id.btn_noti);

        btnProfile = view.findViewById(R.id.btn_update_profile);
        btnTier = view.findViewById(R.id.btn_go_tier);
        btnHistory = view.findViewById(R.id.btn_match_history);
        btnNotiSet = view.findViewById(R.id.btn_go_noti);
        btnLogout = view.findViewById(R.id.btn_logout);

        btnChat.setOnClickListener(v -> { // 채팅
            Intent intent = new Intent(getActivity(),ChatListActivity.class);
            startActivity(intent);
        });

        btnNoti.setOnClickListener(v -> { // 알림 확인
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            navigateToFragment(R.layout.fragment_mypage_profile); // 프로필 수정
        });

        btnTier.setOnClickListener(v -> {
            navigateToFragment(R.layout.fragment_mypage_tier); // 티어 상세 화면
        });

        btnHistory.setOnClickListener(v -> {
            navigateToFragment(R.layout.fragment_mypage_history); // 매칭 내역
        });

        btnNotiSet.setOnClickListener(v -> {
            navigateToFragment(R.layout.fragment_mypage_noti); // 알림 설정
        });

        btnLogout.setOnClickListener(v -> {
            showLogoutPopup();
        });

        try {
            tokenStore = new TokenStore(requireContext());

            currentUserId = tokenStore.getUserId();

            if (currentUserId == -1L) {
                startActivity(new Intent(requireActivity(), AuthActivity.class));
                requireActivity().finish();
                return view;
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e("MyPageFragment", "TokenStore 초기화 실패", e);
            requireActivity().finish();
            return view;
        }



        return view;
    }

    private void navigateToFragment(int layoutId) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, MyPageNavigateFragment.newInstance(layoutId))
                .addToBackStack(null) // 뒤로가기 누르면 다시 마이페이지로
                .commit();
    }

    private void showLogoutPopup() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_popup_match_cancel, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tv_title);
        TextView tvContent = dialogView.findViewById(R.id.tv_content);
        Button btnLeft = dialogView.findViewById(R.id.btn_back);
        Button btnRight = dialogView.findViewById(R.id.btn_cancel);
        ImageButton btnX = dialogView.findViewById(R.id.btn_x);

        tvTitle.setText("잠깐만요!");
        tvContent.setText("로그아웃하시면 Rally의\n배드민턴 매칭 시스템을 이용할 수 없어요.\n정말 로그아웃하시겠어요?");
        btnRight.setText("로그아웃"); // 버튼 글씨 교체

        btnRight.setOnClickListener(v -> {
            dialog.dismiss(); // 팝업 닫기
            tokenStore.clear();
        });

        btnLeft.setOnClickListener(v -> dialog.dismiss());
        btnX.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
