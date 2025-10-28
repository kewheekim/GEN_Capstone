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
import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
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
    private Call<List<CandidateResponseDto>> candidatesCall;
    private Call<Long> requestCall;
    private Long requestId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        gifView = findViewById(R.id.iv_loading_gif);
        Glide.with(this).asGif().load(R.drawable.loading).into(gifView);

        startTime = System.currentTimeMillis();

        Button cancelButton = findViewById(R.id.btn_cancel);
        cancelButton.setEnabled(true);
        cancelButton.setText("매칭 취소하기");
        cancelButton.setOnClickListener(v -> showCancelDialog());

        Intent prev = getIntent();
        MatchRequestDto matchRequest = (MatchRequestDto) prev.getSerializableExtra("matchRequest");

        ApiService service = RetrofitClient.getSecureClient(this, BuildConfig.API_BASE_URL).create(ApiService.class);

        // 1) 요청 저장 → requestId 수신
        requestCall = service.requestMatch(matchRequest);
        requestCall.enqueue(new retrofit2.Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, retrofit2.Response<Long> resp) {
                if (!resp.isSuccessful() || resp.body() == null) {
                    // 서버 에러 처리
                    goToErrorOrNull(matchRequest);
                    return;
                }
                requestId = resp.body();

                // 2) 후보 조회
                candidatesCall = service.getCandidates(matchRequest);
                candidatesCall.enqueue(new retrofit2.Callback<List<CandidateResponseDto>>() {
                    @Override
                    public void onResponse(Call<List<CandidateResponseDto>> call2,
                                           retrofit2.Response<List<CandidateResponseDto>> response) {
                        long elapsed = System.currentTimeMillis() - startTime;
                        long delay = Math.max(2000 - elapsed, 0); // 2초 유지

                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .postDelayed(() -> {
                                    List<CandidateResponseDto> candidates = response.body();
                                    if (response.isSuccessful() && candidates != null && !candidates.isEmpty()) {
                                        // 후보 있음
                                        Intent intent = new Intent(LoadingActivity.this, CandidateActivity.class);
                                        intent.putExtra("userInput", matchRequest);
                                        intent.putExtra("candidates", new ArrayList<>(candidates));
                                        intent.putExtra("requestId", requestId);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // 후보 없음
                                        Intent intent = new Intent(LoadingActivity.this, CandidateNullActivity.class);
                                        intent.putExtra("userInput", matchRequest);
                                        intent.putExtra("requestId", requestId);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, delay);
                    }

                    @Override
                    public void onFailure(Call<List<CandidateResponseDto>> call2, Throwable t) {
                        Log.d("후보 api", "네트워크 오류: " + t.getMessage());
                        goToErrorOrNull(matchRequest);
                    }
                });
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.d("요청 api", "네트워크 오류: " + t.getMessage());
                goToErrorOrNull(matchRequest);
            }
        });

        // 뒤로가기 막기
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { /* block */ }
        });
    }

    private void goToErrorOrNull(MatchRequestDto matchRequest) {
        Intent intent = new Intent(LoadingActivity.this, CandidateNullActivity.class);
        intent.putExtra("userInput", matchRequest);
        if (requestId != null) intent.putExtra("requestId", requestId);
        startActivity(intent);
        finish();
    }

    private void showCancelDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.activity_popup_match_cancel, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.PopupTheme)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_back).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            // 네트워크 취소
            if (candidatesCall != null && !candidatesCall.isCanceled()) candidatesCall.cancel();
            if (requestCall != null && !requestCall.isCanceled()) requestCall.cancel();
            // todo 생성된 요청 삭제

            startActivity(new Intent(LoadingActivity.this, MainActivity.class));
            finish();
        });

        dialog.show();
    }
}
