package com.example.rally.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rally.R;
import com.google.android.material.button.MaterialButton;

public class GoalThemeFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goal_theme, container, false);

        MaterialButton btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(view -> {
            ((GoalCreateActivity) requireActivity()).showTargetFragment();
        });

        return v;
    }
}