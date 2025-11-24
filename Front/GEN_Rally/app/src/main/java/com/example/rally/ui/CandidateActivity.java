package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.CandidateAdapter;
import com.example.rally.dto.CandidateItem;
import com.example.rally.dto.CandidateResponseDto;
import com.example.rally.dto.MatchRequestDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// MAT_REC_S_001
public class CandidateActivity extends AppCompatActivity {
    TextView tvDate;
    TextView tvTime;
    TextView tvLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_result);

        Intent prev = getIntent();
        long requestId = prev.getLongExtra("requestId", -1);
        MatchRequestDto userInput = (MatchRequestDto)prev.getSerializableExtra("userInput");
        String dateLabel = prev.getStringExtra("date");
        String timeLabel = prev.getStringExtra("time");
        String placeLabel = prev.getStringExtra("placeName");

        @SuppressWarnings("unchecked")
        ArrayList<CandidateResponseDto> candidates = (ArrayList<CandidateResponseDto>) getIntent().getSerializableExtra("candidates");

        tvDate= findViewById(R.id.tv_date);
        tvDate.setText(dateLabel);
        tvTime= findViewById(R.id.tv_time);
        tvTime.setText(timeLabel);
        tvLocation= findViewById(R.id.tv_place);
        tvLocation.setText(placeLabel);

        // 리사이클러뷰에 전달할 데이터 가공
        List<CandidateItem> displayList = new ArrayList<>();
        Map<Integer, List<CandidateResponseDto>> grouped = new TreeMap<>();

        for (CandidateResponseDto dto : candidates) {
            grouped.computeIfAbsent(dto.getTier(), k -> new ArrayList<>()).add(dto);
        }

        // 티어 헤더 표시
        Map<Integer, Integer> tierHeaderMap = new HashMap<>();

        // 동일 티어 그룹 먼저 추가
        for (Map.Entry<Integer, List<CandidateResponseDto>> entry : grouped.entrySet()) {
            int tier = entry.getKey();
            List<CandidateResponseDto> group = entry.getValue();

            boolean hasSameTier = group.stream().anyMatch(dto -> dto.getIsSameTier() ==1);
            if (hasSameTier) {
                tierHeaderMap.put(tier, 1);
                displayList.add(new CandidateItem.TierHeader(tier));
                for (CandidateResponseDto user : group) {
                    displayList.add(new CandidateItem.UserCard(user));
                }
            }
        }
        // 나머지 티어 그룹 추가
        for (Map.Entry<Integer, List<CandidateResponseDto>> entry : grouped.entrySet()) {
            int tier = entry.getKey();
            if (tierHeaderMap.containsKey(tier)) continue;  // 이미 추가된 동일 티어는 제외

            List<CandidateResponseDto> group = entry.getValue();
            boolean hasUpperTier = group.stream().anyMatch( dto -> dto.getIsSameTier() == 0);
            tierHeaderMap.put(tier, hasUpperTier? 0 : -1);
            displayList.add(new CandidateItem.TierHeader(tier));
            for (CandidateResponseDto user : group) {
                displayList.add(new CandidateItem.UserCard(user));
            }
        }

        // RecyclerView에 어댑터 연결
        RecyclerView recyclerView=findViewById(R.id.rv_candidates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CandidateAdapter(displayList, tierHeaderMap, requestId));

        // 뒤로가기 버튼 동작 막기
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 처리x
            }
        });

        // 다음에 고르기 버튼
        Button nextBtn = findViewById(R.id.btn_next);
        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(CandidateActivity.this, MainActivity.class);
            intent.putExtra("userInput", userInput);
            intent.putExtra("navigateTo", "matching");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }
}
