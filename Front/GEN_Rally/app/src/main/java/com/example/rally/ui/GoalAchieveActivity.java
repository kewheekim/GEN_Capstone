package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.GoalCheckAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.GoalActiveItem;
import com.example.rally.dto.GoalCheckDto;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalAchieveActivity extends AppCompatActivity {

    private RecyclerView rvGoalList;
    private MaterialButton btnNext;
    private GoalCheckAdapter adapter;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_achieve);

        rvGoalList = findViewById(R.id.rv_goals);
        rvGoalList.setLayoutManager(new LinearLayoutManager(this));
        btnNext = findViewById(R.id.btn_next);
        Long gameId = getIntent().getLongExtra("gameId", 0L);

        adapter = new GoalCheckAdapter(new ArrayList<>(), (item, position, isChecked) -> {
            updateNextButtonState();
        });
        rvGoalList.setAdapter(adapter);

        apiService = RetrofitClient.getSecureClient(this, BuildConfig.API_BASE_URL).create(ApiService.class);

        loadActiveGoals();
        btnNext.setOnClickListener(v -> {
            List<GoalActiveItem> achievedItems = adapter.getAchievedItems();
            List<Long> goalIds = new ArrayList<>(); // goalId 추출
            for (GoalActiveItem item : achievedItems) {
                goalIds.add(item.getId());
            }
            GoalCheckDto dto = new GoalCheckDto(goalIds, gameId);
            Call<Void> callCheck = apiService.checkGoals(dto);
            callCheck.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    Intent intent = new Intent(GoalAchieveActivity.this, GameResultActivity.class);
                    intent.putExtra("gameId", gameId);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(GoalAchieveActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void updateNextButtonState() {
        btnNext.setEnabled(true);
    }

    private void loadActiveGoals() {
        Call<List<GoalActiveItem>> call = apiService.getActiveGoals();
        call.enqueue(new Callback<List<GoalActiveItem>>() {
            @Override
            public void onResponse(Call<List<GoalActiveItem>> call, Response<List<GoalActiveItem>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }
                List<GoalActiveItem> goalItems = response.body();
                List<GoalActiveItem> activeItems = new ArrayList<>();

                for (GoalActiveItem g : goalItems) {
                    activeItems.add(new GoalActiveItem(
                            g.getId(),
                            g.getType(),
                            g.getTheme(),
                            g.getTargetWeeksCount(),
                            g.getProgressCount(),
                            g.getName(),
                            false
                    ));
                }

                adapter.setItems(activeItems);
                updateNextButtonState();
            }

            @Override
            public void onFailure(Call<List<GoalActiveItem>> call, Throwable t) {
                Toast.makeText(GoalAchieveActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
