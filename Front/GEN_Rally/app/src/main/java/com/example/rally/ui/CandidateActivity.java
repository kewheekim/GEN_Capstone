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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// MAT_REC_S_001
public class CandidateActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_result);

        Intent prev = getIntent();
        MatchRequestDto userInput = (MatchRequestDto)getIntent().getSerializableExtra("userInput");
        LocalDate game_date= LocalDate.parse(userInput.getGameDate());
        DateTimeFormatter formatter= DateTimeFormatter.ofPattern("M월 d일");
        String formatted_date = game_date.format(formatter);

        @SuppressWarnings("unchecked")
        ArrayList<CandidateResponseDto> candidates = (ArrayList<CandidateResponseDto>) getIntent().getSerializableExtra("candidates");

        TextView date= findViewById(R.id.tv_date);
        date.setText(formatted_date);
        TextView time= findViewById(R.id.tv_time);
        time.setText(userInput.getStartTime()+":00 ~ "+userInput.getEndTime()+":00");
        TextView location= findViewById(R.id.tv_location);
        location.setText(userInput.getPlace());

        // 리사이클러뷰에 전달할 데이터 가공
        List<CandidateItem> displayList = new ArrayList<>();
        Map<Integer, List<CandidateResponseDto>> grouped = new TreeMap<>();

        for (CandidateResponseDto dto : candidates) {
            grouped.computeIfAbsent(dto.getTier(), k -> new ArrayList<>()).add(dto);
        }

        Map<Integer, Boolean> isSameTierMap = new HashMap<>();

        // 동일 티어 그룹 먼저 추가
        for (Map.Entry<Integer, List<CandidateResponseDto>> entry : grouped.entrySet()) {
            int tier = entry.getKey();
            List<CandidateResponseDto> group = entry.getValue();

            boolean hasSameTier = group.stream().anyMatch(CandidateResponseDto::isSameTier);
            if (hasSameTier) {
                isSameTierMap.put(tier, true);
                displayList.add(new CandidateItem.TierHeader(tier));
                for (CandidateResponseDto user : group) {
                    displayList.add(new CandidateItem.UserCard(user));
                }
            }
        }

        // 나머지 티어 그룹 추가
        for (Map.Entry<Integer, List<CandidateResponseDto>> entry : grouped.entrySet()) {
            int tier = entry.getKey();
            if (isSameTierMap.containsKey(tier)) continue;  // 이미 추가된 동일 티어는 제외

            List<CandidateResponseDto> group = entry.getValue();
            isSameTierMap.put(tier, false);
            displayList.add(new CandidateItem.TierHeader(tier));
            for (CandidateResponseDto user : group) {
                displayList.add(new CandidateItem.UserCard(user));
            }
        }


        // RecyclerView에 어댑터 연결
        RecyclerView recyclerView=findViewById(R.id.rv_candidates);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CandidateAdapter(displayList, isSameTierMap));


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
