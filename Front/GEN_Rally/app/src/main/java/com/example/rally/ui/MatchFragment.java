package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;
import com.example.rally.wear.PhoneDataLayerClient;

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
        Button btnStart =view.findViewById(R.id.btn_start);

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

        btnStart.setOnClickListener(v -> {
            PhoneDataLayerClient.sendMatchSetup(
                    requireContext(),
                    "match-123",
                    "너무어려워요",      // user1
                    "나",      // user2
                    false,          // 워치에서 로컬을 USER1로 취급할지
                    new PhoneDataLayerClient.SendCallback() {
                        @Override public void onSuccess() {
                            requireActivity().runOnUiThread(() -> {
                                btnStart.setEnabled(true);
                                android.widget.Toast.makeText(requireContext(), "워치로 전송 완료", android.widget.Toast.LENGTH_SHORT).show();
                            });
                        }
                        @Override public void onNoNode() {
                            requireActivity().runOnUiThread(() -> {
                                btnStart.setEnabled(true);
                                android.widget.Toast.makeText(requireContext(), "연결된 워치가 없어요", android.widget.Toast.LENGTH_SHORT).show();
                            });
                        }
                        @Override public void onError(Exception e) {
                            requireActivity().runOnUiThread(() -> {
                                btnStart.setEnabled(true);
                                android.widget.Toast.makeText(requireContext(), "전송 실패: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
            );
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startBtn = view.findViewById(R.id.btn_start);
        startBtn.setEnabled(true);
        startBtn.setClickable(true);
        startBtn.bringToFront(); // 혹시나 위에 다른 뷰가 덮고 있으면

        startBtn.setOnClickListener(v -> {
            android.util.Log.d("MatchFragment", "btn_start CLICK");
            android.widget.Toast.makeText(requireContext(), "시작 버튼 클릭!", android.widget.Toast.LENGTH_SHORT).show();

            PhoneDataLayerClient.sendMatchSetup(
                    requireContext(),
                    "match-123",
                    "너무어려워요",
                    "나",
                    false,
                    new PhoneDataLayerClient.SendCallback() {
                        @Override public void onSuccess() {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(requireContext(), "워치로 전송 완료", android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                        @Override public void onNoNode() {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(requireContext(), "연결된 워치가 없어요", android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                        @Override public void onError(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(requireContext(), "전송 실패: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
            );
        });

        // 터치가 들어오는지 확인용
        startBtn.setOnTouchListener((v, ev) -> {
            android.util.Log.d("MatchFragment", "btn_start TOUCH action=" + ev.getAction());
            return false; // false여야 클릭으로도 전달됨
        });
    }
}