package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

// HOM_001
public class HomeFragment extends Fragment {
    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 매칭 버튼
        ImageButton requestBtn = view.findViewById(R.id.btn_request);
        requestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MatTypeActivity.class);
            startActivity(intent);
        });

        // 뒤로가기 막기
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // 동작x
                    }
                }
        );
        return view;
    }
}
