package com.example.rally.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

// SIG_003
public class SetProfileImageFragment extends Fragment {
    private static final String ARG_ID = "id";
    private static final String ARG_PW = "pw";
    private static final String ARG_NAME = "name";
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final String TAG = "SetProfileImageFrag";
    private byte[] selectedImage;
    private String userId, userPw, name;
    private ImageButton imgBtn, btnBack;
    private ImageView imgView;
    private Button btnNext;

    public SetProfileImageFragment(){
        super(R.layout.fragment_signup_image);
    }

    // 이전에서 인스턴스 받아옴 (id, pw, nickname)
    public static SetProfileImageFragment newInstance(String id, String pw, String name) {
        SetProfileImageFragment fragment = new SetProfileImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_PW, pw);
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_ID);
            userPw = getArguments().getString(ARG_PW);
            name = getArguments().getString(ARG_NAME);
        }
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState){
        imgBtn = view.findViewById(R.id.btn_select_img);
        imgView = view.findViewById(R.id.view_img);
        btnNext = view.findViewById(R.id.btn_next);
        btnBack = view.findViewById(R.id.btn_back);
        btnNext.setEnabled(false);

        imgBtn.setOnClickListener(v -> showProfileMenu(v));

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).backToNickname();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (selectedImage == null) {
                Toast.makeText(getContext(), "프로필 사진을 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).showSetGender(userId, userPw, name, selectedImage);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imgView.setImageURI(imageUri);

            try {
                selectedImage = compressImage(imageUri);
                btnNext.setEnabled(true);
                btnNext.setTextColor(Color.parseColor("#FFFFFF"));
            } catch (Exception e) {
                Log.e(TAG, "이미지 압축 중 예외 발생" + e);
                Toast.makeText(getContext(),
                        "이미지를 처리 중 오류가 발생했습니다:\n" + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showProfileMenu(View anchor){
        PopupMenu menu = new PopupMenu(getContext(),anchor);
        menu.getMenuInflater().inflate(R.menu.profile_option_menu, menu.getMenu());

        menu.setOnMenuItemClickListener(item ->{
            int id = item.getItemId();
            if(id == R.id.normal_profile){
                // 기본 프로필 설정
                imgView.setImageResource(R.drawable.ic_default_profile1);

                Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_profile1);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                selectedImage = baos.toByteArray();

                btnNext.setEnabled(true);
                btnNext.setTextColor(Color.parseColor("#FFFFFF"));
                return true;

            } else if(id == R.id.personal_profile){
                openGallery();
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private byte[] compressImage(Uri imageUri) throws IOException {
        // 1) 원본 이미지 크기 파악
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        try (InputStream is = requireContext()
                .getContentResolver()
                .openInputStream(imageUri)) {
            BitmapFactory.decodeStream(is, null, opts);
        }

        // 2) 다운샘플링 비율 계산 (최대 400px)
        final int MAX_DIM = 400;
        int inSample = 1;
        int height = opts.outHeight, width = opts.outWidth;
        if (height > MAX_DIM || width > MAX_DIM) {
            final int halfH = height / 2;
            final int halfW = width / 2;
            while ((halfH / inSample) >= MAX_DIM
                    && (halfW / inSample) >= MAX_DIM) {
                inSample *= 2;
            }
        }

        // 3) 실제 디코딩 (downsampling 적용)
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = inSample;
        Bitmap sampled;
        try (InputStream is2 = requireContext()
                .getContentResolver()
                .openInputStream(imageUri)) {
            sampled = BitmapFactory.decodeStream(is2, null, opts);
        }

        // 4) JPEG 80% 품질로 압축
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sampled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        sampled.recycle();
        return baos.toByteArray();
    }
}