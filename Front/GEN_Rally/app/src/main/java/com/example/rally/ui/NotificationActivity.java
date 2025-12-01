package com.example.rally.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.NotificationAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.NotificationItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        View toolbarBack = findViewById(R.id.toolbar_go);
        toolbarBack.setOnClickListener(v -> finish());

        rvNotifications = findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter();
        rvNotifications.setAdapter(adapter);

        apiService = RetrofitClient.getSecureClient(this, BuildConfig.API_BASE_URL).create(ApiService.class);
        loadRecentNotifications();
    }

    private void loadRecentNotifications() {
        apiService.getRecentNotifications()
                .enqueue(new Callback<List<NotificationItem>>() {
                    @Override
                    public void onResponse(Call<List<NotificationItem>> call,
                                           Response<List<NotificationItem>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setItems(response.body());
                        } else {
                            Toast.makeText(NotificationActivity.this, "알림을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<NotificationItem>> call, Throwable t) {
                        Toast.makeText(NotificationActivity.this,
                                "네트워크 오류: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
