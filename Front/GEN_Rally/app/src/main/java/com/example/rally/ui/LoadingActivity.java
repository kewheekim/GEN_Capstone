package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

// MAT_LO_001, MAT_LO_C_001
public class LoadingActivity extends AppCompatActivity {
    ImageView gifView;
    private long startTime;
    private Call<List<CandidateResponseDto>> matchCall;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 로딩 gif 불러오기
        gifView= findViewById(R.id.iv_loading_gif);
        Glide.with(this).asGif().load(R.drawable.loading)
                .into(gifView);
        // 시작 시간 기록
        startTime=System.currentTimeMillis();

        // 취소 버튼 텍스트 수정
        Button cancelButton =findViewById(R.id.btn_cancel);
        cancelButton.setEnabled(true);
        cancelButton.setText("매칭 취소하기");
        cancelButton.setOnClickListener(r -> {
            showCancelDialog();
        });

        // api/match/request 요청
        Intent prev= getIntent();
        MatchRequestDto matchRequest = (MatchRequestDto) prev.getSerializableExtra("matchRequest");

        MatchService service = RetrofitClient.getClient("http://172.19.46.132:8080/")
                .create(MatchService.class);
        matchCall=service.requestMatch(matchRequest);    // Call 저장


        matchCall.enqueue(new retrofit2.Callback<List<CandidateResponseDto>>() {
            @Override
            public void onResponse(Call<List<CandidateResponseDto>> call, Response<List<CandidateResponseDto>> response) {
                long elapsed=System.currentTimeMillis() - startTime;
                long delay= Math.max(2000 - elapsed, 0);  // 2초 유지

                new Handler().postDelayed(() -> {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // 후보 있는 경우
                        List<CandidateResponseDto> candidates = response.body();
                        Intent intent = new Intent(LoadingActivity.this, CandidateActivity.class);
                        intent.putExtra("userInput", matchRequest);
                        intent.putExtra("candidates", new ArrayList<>(candidates));
                        startActivity(intent);
                        finish();
                    } else {
                        // 후보 없음 또는 응답 body null
                        Intent intent = new Intent(LoadingActivity.this, CandidateNullActivity.class);
                        intent.putExtra("userInput", matchRequest);
                        startActivity(intent);
                        finish();
                    }
                }, delay);
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
        View dialogView = getLayoutInflater().inflate(R.layout.activity_popup_match_cancel, null);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.PopupTheme)
                .setView(dialogView)
                .create();

        // 돌아가기 버튼
        dialogView.findViewById(R.id.btn_back).setOnClickListener(v -> dialog.dismiss());

        // 취소 버튼
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            // 매칭 취소
            if (matchCall != null && !matchCall.isCanceled()) {
                matchCall.cancel();
            }
            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            startActivity(intent);
        });

        dialog.show();
    }
}
