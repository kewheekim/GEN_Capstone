package com.example.rally.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.RecordCommentAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.CommentDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordCommentActivity extends AppCompatActivity {

    private RecyclerView rvComments;
    private RecordCommentAdapter recordCommentAdapter;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        rvComments = findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        recordCommentAdapter = new RecordCommentAdapter(new ArrayList<>());
        rvComments.setAdapter(recordCommentAdapter);

        apiService = RetrofitClient.getSecureClient(getApplicationContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

        apiService.getComments().enqueue(new Callback<List<CommentDto>>() {
            @Override
            public void onResponse(Call<List<CommentDto>> call, Response<List<CommentDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CommentDto> data = response.body();

                    if (!data.isEmpty()) {
                        recordCommentAdapter.setItems(data);
                    } else {
                        Log.e("CommentActivity", "칭찬 없음" + response.code());
                    }

                } else {
                    Log.e("CommentActivity", "칭찬 데이터 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CommentDto>> call, Throwable t) {
                Log.e("CommentActivity", "서버 통신 오류", t);

            }
        });
    }
}
