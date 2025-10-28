package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchInvite;
import com.example.rally.dto.MatchInviteResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// MAT_AP_S_005
public class PopupInviteActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_invite);

        TextView info = findViewById(R.id.tv_title);
        ImageButton xButton = findViewById(R.id.btn_x);
        Button backButton = findViewById(R.id.btn_back);
        Button goButton = findViewById(R.id.go_btn);

        Intent prev = getIntent();
        String name = prev.getStringExtra("opponentName");
        info.setText("'" + name + "' 님에게\n매칭 요청을 보내시겠어요?");
        String receiverId = getIntent().getStringExtra("opponentId");
        long receiverRequestId = getIntent().getLongExtra("opponentRequestId", -1);
        long senderRequestId = getIntent().getLongExtra("myRequestId", -1);

        // X 버튼: 닫기
        xButton.setOnClickListener(v -> finish());
        // 돌아가기 버튼
        backButton.setOnClickListener(v -> finish());
        // 요청 보내기 버튼
        goButton.setOnClickListener(v -> {
            goButton.setEnabled(false);
            MatchInvite body = new MatchInvite(senderRequestId, receiverRequestId, receiverId);
            ApiService api = RetrofitClient.getSecureClient( this, BuildConfig.API_BASE_URL).create(ApiService.class);

            api.sendInvitation(body).enqueue(new Callback<MatchInviteResponse>() {
                @Override
                public void onResponse(Call<MatchInviteResponse> call, Response<MatchInviteResponse> resp) {
                    goButton.setEnabled(true);
                    if (resp.isSuccessful() && resp.body() != null) {
                        MatchInviteResponse response = resp.body();

                        Intent intent = new Intent(PopupInviteActivity.this, MainActivity.class);
                        intent.putExtra("navigateTo", "invitation_sent");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(PopupInviteActivity.this, "전송 실패: " + resp.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MatchInviteResponse> call, Throwable t) {
                    goButton.setEnabled(true);
                    Toast.makeText(PopupInviteActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}