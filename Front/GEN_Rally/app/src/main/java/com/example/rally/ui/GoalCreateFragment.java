package com.example.rally.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.GoalCreateRequest;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GoalCreateFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goal_create, container, false);

        TextView tvTarget = v.findViewById(R.id.tv_target);
        TextView tvTheme = v.findViewById(R.id.tv_theme);
        TextView tvGoal = v.findViewById(R.id.tv_goal);
        TextView tvCount = v.findViewById(R.id.tv_count);
        MaterialButton btnConfirm = v.findViewById(R.id.btn_confirm);

        GoalCreateActivity activity = (GoalCreateActivity) requireActivity();

        String goalName = activity.getGoalName();
        String goalTheme = activity.getGoalTheme();
        String goalType = activity.getGoalType();
        Integer targetWeeksCount = activity.getTargetWeeks() != null ? activity.getTargetWeeks() : activity.getTargetCount();
        Integer calorie = activity.getTargetCalorie();

        if (goalName != null) {
            tvGoal.setText(goalName);
        }

        tvTheme.setText(toDisplayTheme(goalTheme));

        if ("기간".equals(goalType) && targetWeeksCount != null) {
            if (targetWeeksCount == 4) {
                tvTarget.setText("한달");
            } else {
                tvTarget.setText(targetWeeksCount + "주");
            }
        } else if ("횟수".equals(goalType) && targetWeeksCount != null) {
            tvTarget.setText(targetWeeksCount + "회");
        }

        if (targetWeeksCount != null) {
            tvCount.setText("/" + targetWeeksCount);
        } else if (targetWeeksCount != null) {
            tvCount.setText("/" + targetWeeksCount);
        } else {
            tvCount.setText("/0");
        }

        btnConfirm.setOnClickListener(view -> {
            GoalCreateRequest request = new GoalCreateRequest(
                    goalName, goalTheme, goalType, targetWeeksCount, calorie
            );

            ApiService api = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);
            Call<ResponseBody> call = api.createGoal(request);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "목표가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                        // todo 기록 화면으로 이동
                    } else {
                        Toast.makeText(requireContext(), "목표 저장에 실패했습니다. (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "서버 통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        return v;
    }
    private String toDisplayTheme(String theme) {
        if ("실력증진".equals(theme))  return "실력 증진";
        if ("건강관리".equals(theme))  return "건강 관리";
        if ("대인관계".equals(theme))  return "대인 관계";
        return theme != null ? theme : "";
    }
}
