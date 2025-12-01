package com.example.rally.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.GeneralSignupRequest;
import com.example.rally.dto.SignupResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// SIG_005
public class SetPrimaryFragment extends Fragment {

    private static final String ARG_ID = "id";
    private static final String ARG_PW = "pw";
    private static final String ARG_NAME = "name";
    private static final String ARG_IMAGE_BYTES = "imageBytes";
    private static final String ARG_GENDER = "gender";
    private String userId, userPw, name, gender, fcmToken;
    private byte[] imageBytes;
    private StorageReference storage;
    private ImageButton btnBack;
    private Button btnNext;
    private RadioGroup radioGroup;
    private RadioButton rbSkill, rbLocation, rbTime, rbStyle;
    private int checkedIndex = -1;

    public SetPrimaryFragment(){
        super(R.layout.fragment_signup_primary);
    }

    public static SetPrimaryFragment newInstance(String id, String pw, String name, byte[] selectedImg, String gender) {
        SetPrimaryFragment fragment = new SetPrimaryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, id);
        args.putString(ARG_PW, pw);
        args.putString(ARG_NAME, name);
        args.putByteArray(ARG_IMAGE_BYTES, selectedImg);
        args.putString(ARG_GENDER, gender);
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
            gender = getArguments().getString(ARG_GENDER);
        }
    }

    @Override
    public void onViewCreated (@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnBack = view.findViewById(R.id.btn_back);
        btnNext = view.findViewById(R.id.btn_next);
        radioGroup = view.findViewById(R.id.radio_group);
        rbSkill = view.findViewById(R.id.rb_skill);
        rbLocation = view.findViewById(R.id.rb_location);
        rbTime = view.findViewById(R.id.rb_time);
        rbStyle = view.findViewById(R.id.rb_style);
        btnNext.setEnabled(false);

        btnBack.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).backToGender();
            }
        });

        com.google.firebase.FirebaseApp.initializeApp(requireContext().getApplicationContext());

        storage = FirebaseStorage.getInstance().getReference();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener( task -> {
            if (!task.isSuccessful()) {
                Log.w("FCM", "FCM 토큰 가져오기 실패", task.getException());
                return;
            }
            fcmToken = task.getResult();
            Log.d("FCM", "FCM 토큰: " + fcmToken);
        });

        final ApiService apiService = RetrofitClient
                .getClient(BuildConfig.API_BASE_URL)
                .create(ApiService.class);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            btnNext.setEnabled(true);
            btnNext.setTextColor(Color.parseColor("#FFFFFF"));

            checkedIndex = radioGroup.indexOfChild(
                    view.findViewById(checkedId)
            );
        });

        btnNext.setOnClickListener(v -> {
            if (checkedIndex < 0) {
                Toast.makeText(getContext(), "하나의 옵션을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            imageUpload(apiService);
        });
    }

    private void imageUpload(ApiService apiService){
        // 가입 전이라 서버 PK가 없으니, 입력한 userId를 폴더 키로 사용
        String fileName = System.currentTimeMillis() + ".jpg";
        String objectPath = "images/" + userId + "/profile/" + fileName;

        StorageReference ref = storage.child(objectPath);
        StorageMetadata meta = new StorageMetadata.Builder()
                .setContentType("image/jpeg") // 기본/갤러리 모두 JPEG로 맞춘다는 전제
                .build();

        ref.putBytes(imageBytes, meta)
                .continueWithTask(t -> {
                    if (!t.isSuccessful()) throw t.getException();
                    return ref.getDownloadUrl();
                })
                .addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    callSignup(apiService, downloadUrl);
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    btnNext.setText("다음");
                    Log.e("SignUp", "Firebase 업로드 실패", e);
                    Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void callSignup(ApiService apiService, @NonNull String imageUrl) {
        GeneralSignupRequest request = new GeneralSignupRequest();
        request.setUserId(userId);
        request.setPassword(userPw);
        request.setName(name);
        request.setGender(gender);
        request.setPrimaryThing(String.valueOf(checkedIndex));
        request.setImageUrl(imageUrl);
        request.setFcmToken(fcmToken);

            apiService.signup(request)
                    .enqueue(new Callback<SignupResponse>() {
                        @Override
                        public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                SignupResponse body = response.body();

                                try{
                                    TokenStore tokenStore = new TokenStore(requireContext().getApplicationContext());
                                    tokenStore.saveTokens(body.getAccessToken(), body.getRefreshToken(), body.getUserPk());
                                }catch(Exception e){
                                    Log.e("SignUp", "토큰 저장 실패", e);
                                }

                                if (getActivity() instanceof AuthActivity) {
                                    ((AuthActivity) getActivity()).showComplete(name, imageBytes);
                                }
                            } else {
                                try {
                                    String rawJson = response.errorBody().string();

                                    Log.e("SignUp", "서버 에러 응답: " + rawJson);
                                    Toast.makeText(
                                            getContext(),
                                            rawJson,
                                            Toast.LENGTH_LONG
                                    ).show();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<SignupResponse> call, Throwable t) {
                            Log.e("SignUp", t.getMessage());
                            Toast.makeText(getContext(),
                                    "네트워크 오류: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
    }
}