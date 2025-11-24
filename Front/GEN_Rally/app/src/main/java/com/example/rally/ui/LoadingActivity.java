package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
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

// MAT_LO_001, MAT_LO_C_001
public class LoadingActivity extends AppCompatActivity {
    ImageView gifView;
    private long startTime;
    private Call<List<CandidateResponseDto>> candidatesCall;
    private Call<Long> requestCall;
    private Long requestId;
    private String date;
    private String gameType;
    private String gameStyle;
    private String time;
    private String placeName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        gifView = findViewById(R.id.iv_loading_gif);
        Glide.with(this).asGif().load(R.drawable.loading).into(gifView);

        Button cancelButton = findViewById(R.id.btn_cancel);
        cancelButton.setEnabled(true);
        cancelButton.setText("매칭 취소하기");
        cancelButton.setOnClickListener(v -> showCancelDialog());

        Intent prev = getIntent();
        requestId = prev.getLongExtra("requestId", -1L);
        date = prev.getStringExtra("date");
        gameType  = prev.getStringExtra("gameType");
        gameStyle = prev.getStringExtra("gameStyle");
        time = prev.getStringExtra("time");
        placeName = prev.getStringExtra("placeName");
        if (requestId == null || requestId <= 0) {
            finish();
            return;
        }
        startTime = System.currentTimeMillis();

        ApiService api = RetrofitClient.getSecureClient(this, BuildConfig.API_BASE_URL).create(ApiService.class);
        candidatesCall = api.getCandidates(requestId);
        candidatesCall.enqueue(new retrofit2.Callback<List<CandidateResponseDto>>() {
            @Override
            public void onResponse(Call<List<CandidateResponseDto>> call,
                                   retrofit2.Response<List<CandidateResponseDto>> response) {
                long elapsed = System.currentTimeMillis() - startTime;
                long delay = Math.max(2000 - elapsed, 0); // 2초 로딩

                new android.os.Handler(android.os.Looper.getMainLooper())
                        .postDelayed(() -> {
                            List<CandidateResponseDto> candidates = response.body();

                            if (response.isSuccessful() && candidates != null && !candidates.isEmpty()) {
                                // 후보 있음 CandidateActivity
                                Intent intent = new Intent(LoadingActivity.this, CandidateActivity.class);
                                intent.putExtra("candidates", new ArrayList<>(candidates));
                                intent.putExtra("requestId", requestId);
                                // UI 구성용 부가 정보
                                intent.putExtra("date", date);
                                intent.putExtra("gameType", gameType);
                                intent.putExtra("gameStyle", gameStyle);
                                intent.putExtra("time", time);
                                intent.putExtra("placeName", placeName);

                                startActivity(intent);
                                finish();
                            } else {
                                // 후보 없음 CandidateNullActivity
                                Intent intent = new Intent(LoadingActivity.this, CandidateNullActivity.class);
                                intent.putExtra("requestId", requestId);
                                intent.putExtra("date", date);
                                intent.putExtra("gameType", gameType);
                                intent.putExtra("gameStyle", gameStyle);
                                intent.putExtra("time", time);
                                intent.putExtra("placeName", placeName);
                                startActivity(intent);
                                finish();
                            }
                        }, delay);
            }

            @Override
            public void onFailure(Call<List<CandidateResponseDto>> call, Throwable t) {
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {}
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

            // 콜 중단
            if (candidatesCall != null && !candidatesCall.isCanceled()) candidatesCall.cancel();
            if (requestCall != null && !requestCall.isCanceled()) requestCall.cancel();

            if (requestId != null) {
                ApiService service = RetrofitClient.getSecureClient(this, BuildConfig.API_BASE_URL)
                        .create(ApiService.class);
                // todo: 매칭 신청 취소 api

            } else {
                // 홈 화면으로 이동
            }
        });

        dialog.show();
    }
}
