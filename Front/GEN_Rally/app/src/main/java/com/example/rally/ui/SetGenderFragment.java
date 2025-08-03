package com.example.rally.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class SetGenderFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String ARG_PW = "pw";
    private static final String ARG_NAME = "name";
    private static final String ARG_IMAGE_BYTES = "imageBytes";
    private static final String ARG_GENDER = "gender";
    private String userId, userPw, name;
    private byte[] imageBytes;
    private String selectedGender = null;
    private ImageButton btnMale, btnFemale, btnBack;
    private Button btnNext;
    private TextView tvMale, tvFemale;

    public SetGenderFragment(){
        super(R.layout.fragment_signup_gender);
    }
    public static SetGenderFragment newInstance(String id, String pw, String name, byte[] selectedImg) {
        SetGenderFragment fragment = new SetGenderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_PW, pw);
        args.putString(ARG_NAME, name);
        args.putByteArray(ARG_IMAGE_BYTES, selectedImg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_ID);
            userPw = getArguments().getString(ARG_PW);
            name = getArguments().getString(ARG_NAME);
            imageBytes = getArguments().getByteArray(ARG_IMAGE_BYTES);
        }
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState){
        btnBack = view.findViewById(R.id.btn_back);
        btnNext = view.findViewById(R.id.btn_next);
        btnMale = view.findViewById(R.id.btn_male);
        btnFemale = view.findViewById(R.id.btn_female);
        tvMale = view.findViewById(R.id.tv_male);
        tvFemale = view.findViewById(R.id.tv_female);
        btnNext.setEnabled(false);

        int green = ContextCompat.getColor(requireContext(), R.color.green_active);
        int gray  = ContextCompat.getColor(requireContext(), R.color.gray_border);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).backToImage();
            }
        });

        // 남자 선택
        btnMale.setOnClickListener(v -> {
            selectedGender = "남성";
            btnMale.setSelected(true);
            btnFemale.setSelected(false);

            btnMale.setImageTintList(ColorStateList.valueOf(green));
            btnFemale.setImageTintList(ColorStateList.valueOf(gray));
            tvMale.setTextColor(ColorStateList.valueOf(green));
            tvFemale.setTextColor(ColorStateList.valueOf(gray));
            btnNext.setEnabled(true);
            btnNext.setTextColor(Color.parseColor("#FFFFFF"));
        });

        // 여자 선택
        btnFemale.setOnClickListener(v -> {
            selectedGender = "여성";
            btnFemale.setSelected(true);
            btnMale.setSelected(false);

            btnMale.setImageTintList(ColorStateList.valueOf(gray));
            btnFemale.setImageTintList(ColorStateList.valueOf(green));
            tvMale.setTextColor(ColorStateList.valueOf(gray));
            tvFemale.setTextColor(ColorStateList.valueOf(green));
            btnNext.setEnabled(true);
            btnNext.setTextColor(Color.parseColor("#FFFFFF"));
        });

        btnNext.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showSetPrimary(userId, userPw, name, imageBytes, selectedGender);
            }
        });
    }
}
