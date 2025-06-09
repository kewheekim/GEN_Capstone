package com.example.rally.ui;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.api.MatchService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.CandidateResponseDto;
import com.example.rally.dto.MatchRequestDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class LoadingActivity extends AppCompatActivity {
    ImageView gifView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 로딩 gif 불러오기
        gifView= findViewById(R.id.loadingGif);
        Glide.with(this).asGif().load(R.drawable.dot_animation)
                .into(gifView);

        // 취소 버튼 텍스트 수정
        View includeView = findViewById(R.id.include_next_button);
        Button cancelButton = includeView.findViewById(R.id.next_button);
        cancelButton.setText("매칭 취소하기");

        // api/match/request 요청
        Intent prev= getIntent();
        MatchRequestDto matchRequest = (MatchRequestDto) prev.getSerializableExtra("matchRequest");

        MatchService service = RetrofitClient.getClient("http://172.19.10.92:8080/")
                .create(MatchService.class);

        service.requestMatch(matchRequest).enqueue(new retrofit2.Callback<List<CandidateResponseDto>>() {
            @Override
            public void onResponse(Call<List<CandidateResponseDto>> call, Response<List<CandidateResponseDto>> response) {
                if (response.body() != null && !response.body().isEmpty()) {
                    // 후보 있는 경우
                    List<CandidateResponseDto> candidates = response.body();
                    Intent intent = new Intent(LoadingActivity.this, PartnerActivity.class);
                    intent.putExtra("userInput", matchRequest);
                    intent.putExtra("candidates", new ArrayList<>(candidates));
                    startActivity(intent);
                    finish();
                } else {
                    // 후보 없음 또는 응답 body null
                    Intent intent = new Intent(LoadingActivity.this, PartnerNullActivity.class);
                    intent.putExtra("userInput", matchRequest);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<CandidateResponseDto>> call, Throwable t) {
                Log.d("후보 api", "네트워크 오류: " + t.getMessage());
            }
        });

        // 뒤로가기 버튼 막기
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 처리x
            }
        });
    }

    private void showCancelDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_popup_cancel, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // 버튼 처리
        dialogView.findViewById(R.id.back_Btn).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.go_Btn).setOnClickListener(v -> {
            dialog.dismiss();
//            cancelPendingRequest();
//            goToHome();
        });

        dialog.show();
    }
}
