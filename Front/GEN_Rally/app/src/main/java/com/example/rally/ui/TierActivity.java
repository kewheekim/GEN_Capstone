package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.TierAssessRequest;
import com.example.rally.dto.TierAssessResponse;

import javax.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// SIG_012
public class TierActivity extends AppCompatActivity {

    private TextView tvName, tvTier;
    private FrameLayout frameTier;
    private Button btnNext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tier);

        tvName = findViewById(R.id.tv_name);
        tvTier = findViewById(R.id.tv_tier);
        frameTier = findViewById(R.id.iv_tier);
        btnNext = findViewById(R.id.btn_next);

        int canService = getIntent().getIntExtra("canService", -1);
        int canPass    = getIntent().getIntExtra("canPass", -1);
        int footWork   = getIntent().getIntExtra("footWork", -1);
        int count      = getIntent().getIntExtra("count", -1);
        int howLong    = getIntent().getIntExtra("howLong", -1);

        final ApiService apiService = RetrofitClient
                .getSecureClient( this, BuildConfig.API_BASE_URL)
                .create(ApiService.class);

        TierAssessRequest req = new TierAssessRequest();
        req.setQ1(canService);
        req.setQ2(canPass);
        req.setQ3(footWork);
        req.setQ4(count);
        req.setQ5(howLong);

        btnNext = findViewById(R.id.btn_next);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(TierActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        });

        apiService.getTier(req)
                .enqueue(new Callback<TierAssessResponse>() {
                    @Override
                    public void onResponse(Call<TierAssessResponse> call, Response<TierAssessResponse> response) {
                        Log.d("TierActivity", "onResponse ✅ code=" + response.code());
                        if(response.isSuccessful() && response.body()!=null){
                            TierAssessResponse body = response.body();
                            tvTier.setText(body.getTier());
                            switch (body.getTier()) {
                                case "입문자1" :
                                    frameTier.setBackgroundResource(R.drawable.ic_tier_bronze1_eff); break;
                                case "초보자1" :
                                    frameTier.setBackgroundResource(R.drawable.ic_tier_silver1_eff); break;
                                case "중급자1" :
                                    frameTier.setBackgroundResource(R.drawable.ic_tier_gold1_eff); break;
                                case "상급자1" :
                                    frameTier.setBackgroundResource(R.drawable.ic_tier_dia1_eff); break;
                            }
                            tvName.setText(body.getNickname()+"님은");
                        } else {
                            // 401이면 토큰 만료/부재 가능
                            if (response.code() == 401) {
                                Log.e("getTier","오류:"+ response.body());
                                Toast.makeText(TierActivity.this, "인증이 만료되었습니다", Toast.LENGTH_LONG).show();
                                // TODO: 토큰 삭제 및 로그인 화면 이동
                                // new TokenStore(getApplicationContext()).clear();
                                // startActivity(new Intent(TierActivity.this, AuthActivity.class));
                                finish();
                            } else {
                                Log.e("getTier","오류:"+ response.body());
                                Toast.makeText(TierActivity.this, "오류: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<TierAssessResponse> call, Throwable t) {
                        Log.e("getTier","onFailure " ,t);
                        Toast.makeText(TierActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
