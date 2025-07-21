package com.example.rally.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

// MAT_001
public class MatchFragment extends Fragment {

    private MatchRequestDto userInput;

    public static MatchFragment newInstance(MatchRequestDto input) {
        MatchFragment fragment = new MatchFragment();
        Bundle args = new Bundle();
        args.putSerializable("userInput", input);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_match, container, false);

        if (getArguments() != null) {
            userInput = (MatchRequestDto) getArguments().getSerializable("userInput");
        }

        if (userInput == null) return view;  // null 방지

        TextView tvDday = view.findViewById(R.id.tv_dday);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvPlace = view.findViewById(R.id.tv_location);
        TextView tvType = view.findViewById(R.id.tv_type);

        LocalDate today = LocalDate.now();
        int dday = (int) ChronoUnit.DAYS.between(today, LocalDate.parse(userInput.getGameDate()));
        tvDday.setText("매칭 마감까지 D-" + dday);

        LocalDate game_date = LocalDate.parse(userInput.getGameDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일(E)");
        String formatted_date = game_date.format(formatter);
        tvDate.setText(formatted_date);

        tvTime.setText(userInput.getStartTime() + ":00 ~ " + userInput.getEndTime() + ":00");

        tvPlace.setText(userInput.getPlace());
        tvType.setText(userInput.getGameType() == 0 ? "단식" : "복식");

        return view;
    }
}