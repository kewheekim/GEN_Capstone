package com.example.rally.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class SignupCompleteFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_IMAGE_BYTES = "imageBytes";
    private String name;
    private byte[] imageBytes;

    private ImageView ivProfile;
    private TextView tvWelcome;
    private Button btnNext;

    public SignupCompleteFragment(){
        super(R.layout.fragment_signup_complete);
    }

    // 이전에서 인스턴스 받아옴 (nickname, img)
    public static SignupCompleteFragment newInstance(String name, byte[] image) {
        SignupCompleteFragment fragment = new SignupCompleteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putByteArray(ARG_IMAGE_BYTES, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            imageBytes = getArguments().getByteArray(ARG_IMAGE_BYTES);
        }
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        ivProfile = view.findViewById(R.id.iv_profile);
        tvWelcome = view.findViewById(R.id.tv_welcome);
        btnNext = view.findViewById(R.id.btn_next);

        tvWelcome.setText(String.format("반갑습니다 %s님!", name));
        if (imageBytes != null && imageBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    imageBytes, 0, imageBytes.length);
            ivProfile.setImageBitmap(bitmap);
        }

    }
}
