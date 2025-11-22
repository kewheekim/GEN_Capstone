package com.example.rally.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.rally.R;
import com.google.android.material.button.MaterialButton;

public class GoalCalorieFragment extends Fragment {
    private static final int MIN_CALORIE = 50;
    private static final int MAX_CALORIE = 600;
    private EditText etCalorie;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goal_calorie, container, false);

        etCalorie = v.findViewById(R.id.et_calorie);
        etCalorie.addTextChangedListener(new TextWatcher() {
            private boolean isEditing = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(isEditing) return;
                String text = s.toString();
                if(text.isEmpty()) return;
                int value;
                try {
                    value= Integer.parseInt(text);
                } catch (NumberFormatException e) {
                    return;
                }
                int fixed = value;
                if (value> MAX_CALORIE) fixed = MAX_CALORIE;

                if(fixed != value) {
                    isEditing = true;
                    etCalorie.setText(String.valueOf(fixed));
                    etCalorie.setSelection(etCalorie.getText().length());
                    isEditing = false;
                }
            }
        });
        MaterialButton btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(view -> {
            String text = etCalorie.getText().toString().trim();
            if (text.isEmpty()) {
                etCalorie.setError("목표 칼로리를 입력해 주세요.");
                return;
            }

            int value = Integer.parseInt(text);
            if (value < MIN_CALORIE || value > MAX_CALORIE) {
                etCalorie.setError(MIN_CALORIE + " ~ " + MAX_CALORIE + " 사이로 입력해 주세요.");
                return;
            }

            ((GoalCreateActivity) requireActivity()).onCalorieInputed(value);
            ((GoalCreateActivity) requireActivity()).showCreateFragment();
        });
        return v;
    }
}