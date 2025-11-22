package com.example.rally.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rally.R;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class GoalTargetFragment extends Fragment {

    private List<MaterialButton> targetBtns;
    private MaterialButton selectedBtn;
    private TextView tvTitle;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goal_target, container, false);

        GoalCreateActivity activity = (GoalCreateActivity) requireActivity();
        String goalName  = activity.getGoalName();

        TextView tvTitle = v.findViewById(R.id.tv_title);
        if (goalName != null) {
            String title = "‘" + goalName + "’ 를\n얼마나 실천할까요?";
            tvTitle.setText(title);
        }

        MaterialButton btn2weeks = v.findViewById(R.id.btn_2weeks);
        MaterialButton btn1month = v.findViewById(R.id.btn_1month);
        MaterialButton btn2times  = v.findViewById(R.id.btn_2times);
        MaterialButton btn4times  = v.findViewById(R.id.btn_4times);
        MaterialButton btn6times  = v.findViewById(R.id.btn_6times);
        MaterialButton btn8times  = v.findViewById(R.id.btn_8times);
        MaterialButton btn10times = v.findViewById(R.id.btn_10times);
        MaterialButton btn12times = v.findViewById(R.id.btn_12times);

        targetBtns = Arrays.asList(
                btn2weeks, btn1month,
                btn2times, btn4times, btn6times,
                btn8times, btn10times, btn12times
        );

        View.OnClickListener targetClickListener = view -> {
            MaterialButton clicked = (MaterialButton) view;
            updateSelection(clicked);
        };

        for (MaterialButton b : targetBtns) {
            b.setOnClickListener(targetClickListener);
        }
        updateSelection(btn2weeks);

        MaterialButton btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(view -> {
            if(selectedBtn == null) {
                Toast.makeText(requireContext(), "기간 또는 횟수를 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            String goalType;
            Integer targetWeeks = null;
            Integer targetCount = null;

            int id = selectedBtn.getId();
            if (id == R.id.btn_2weeks) {
                goalType = "기간";
                targetWeeks = 2;
            } else if (id == R.id.btn_1month) {
                goalType = "기간";
                targetWeeks = 4;
            } else {
                goalType = "횟수";
                if (id == R.id.btn_2times) {
                    targetCount = 2;
                } else if (id == R.id.btn_4times) {
                    targetCount = 4;
                } else if (id == R.id.btn_6times) {
                    targetCount = 6;
                } else if (id == R.id.btn_8times) {
                    targetCount = 8;
                } else if (id == R.id.btn_10times) {
                    targetCount = 10;
                } else if (id == R.id.btn_12times) {
                    targetCount = 12;
                }
            }
            activity.onTargetSelected(goalType, targetWeeks, targetCount);  // 액티비티에 저장

            if ("목표 칼로리 달성하기".equals(goalName)) {
                activity.showCalorieFragment();
            } else {
                activity.showCreateFragment();
            }
        });
        return v;
    }

    private void updateSelection(@NonNull MaterialButton clicked) {
        for (MaterialButton b : targetBtns) {
            boolean isSelected = (b == clicked);
            b.setChecked(isSelected);
            if (isSelected) {
                selectedBtn = b;
            }
        }
    }

    public String getSelectedText() {
        return selectedBtn != null ? selectedBtn.getText().toString() : null;
    }
}
