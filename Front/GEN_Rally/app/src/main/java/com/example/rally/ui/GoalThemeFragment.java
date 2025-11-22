package com.example.rally.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rally.R;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class GoalThemeFragment extends Fragment {
    private List<MaterialButton> goalBtns;
    private MaterialButton selectedBtn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goal_theme, container, false);

        MaterialButton btnPractice = v.findViewById(R.id.btn_practice);
        MaterialButton btnGrip = v.findViewById(R.id.btn_grip);
        MaterialButton btnCall = v.findViewById(R.id.btn_call);
        MaterialButton btnCalorie = v.findViewById(R.id.btn_calorie);
        MaterialButton btnWater = v.findViewById(R.id.btn_water);
        MaterialButton btnStretch = v.findViewById(R.id.btn_stretch);
        MaterialButton btnMeeting = v.findViewById(R.id.btn_meeting);
        MaterialButton btnCheer = v.findViewById(R.id.btn_cheer);
        MaterialButton btnCompliment = v.findViewById(R.id.btn_compliment);

        goalBtns = Arrays.asList( btnPractice, btnGrip, btnCall, btnCalorie, btnWater, btnStretch, btnMeeting, btnCheer, btnCompliment );

        View.OnClickListener goalBtnsListener = view -> {
            MaterialButton clicked = (MaterialButton) view;
            updateSelection(clicked);
        };

        for (MaterialButton b : goalBtns) {
            b.setOnClickListener(goalBtnsListener);
        }
        updateSelection(btnPractice);

        MaterialButton btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(view -> {
            if (selectedBtn == null) return;

            String goalText = selectedBtn.getText().toString();
            String goalTheme = resolveGoalTheme(selectedBtn.getId());

            GoalCreateActivity activity = (GoalCreateActivity) requireActivity();
            activity.onThemeSelected(goalTheme, goalText);
            activity.showTargetFragment();
        });
        return v;
    }

    private void updateSelection(@NonNull MaterialButton clicked) {
        for (MaterialButton b : goalBtns) {
            boolean isSelected = (b == clicked);
            b.setChecked(isSelected);
            if (isSelected) {
                selectedBtn = b;
            }
        }
    }

    private String resolveGoalTheme(int viewId) {
        if (viewId == R.id.btn_practice
                || viewId == R.id.btn_grip
                || viewId == R.id.btn_call) {
            return "실력증진";
        }
        if (viewId == R.id.btn_calorie
                || viewId == R.id.btn_water
                || viewId == R.id.btn_stretch) {
            return "건강관리";
        }
        if (viewId == R.id.btn_meeting
                || viewId == R.id.btn_cheer
                || viewId == R.id.btn_compliment) {
            return "대인관계";
        }
        return "실력증진";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        goalBtns = null;
        selectedBtn = null;
    }
}
