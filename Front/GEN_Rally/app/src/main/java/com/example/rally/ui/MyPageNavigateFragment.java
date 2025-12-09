package com.example.rally.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class MyPageNavigateFragment extends Fragment {
    private static final String LAYOUT_ID = "layout_res_id";

    public static MyPageNavigateFragment newInstance(int layoutId) {
        MyPageNavigateFragment fragment = new MyPageNavigateFragment();
        Bundle args = new Bundle();
        args.putInt(LAYOUT_ID, layoutId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = getArguments().getInt(LAYOUT_ID);
        View view = inflater.inflate(layoutId, container, false);
        view.setClickable(true); // 뒤에 있는 버튼 클릭 방지
        View btnBack = view.findViewById(R.id.btn_back);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
        }

        setupTierTabLogic(view);
        return view;
    }

    // 티어 탭 변경하는 로직
    private void setupTierTabLogic(View view) {
        // 뷰 찾기 (없으면 null이 됨)
        TextView tvLevel = view.findViewById(R.id.tv_tier_level);
        View viewLevel = view.findViewById(R.id.view_tier_level);
        TextView tvCriteria = view.findViewById(R.id.tv_tier_criteria);
        View viewCriteria = view.findViewById(R.id.view_tier_criteria);

        View scrollLevel = view.findViewById(R.id.scroll_tier_level);
        View layoutCriteria = view.findViewById(R.id.scroll_tier_criteria);

        if (tvLevel != null && tvCriteria != null && scrollLevel != null && layoutCriteria != null) {
            int colorActive = ContextCompat.getColor(requireContext(), R.color.green_active);
            int colorInactive = ContextCompat.getColor(requireContext(), R.color.gray_400);
            int colorLineInactive = ContextCompat.getColor(requireContext(), R.color.gray_200);

            tvLevel.setOnClickListener(v -> {
                tvLevel.setTextColor(colorActive);
                viewLevel.setBackgroundColor(colorActive);

                tvCriteria.setTextColor(colorInactive);
                viewCriteria.setBackgroundColor(colorLineInactive);

                scrollLevel.setVisibility(View.VISIBLE);
                layoutCriteria.setVisibility(View.GONE);
            });

            tvCriteria.setOnClickListener(v -> {
                tvLevel.setTextColor(colorInactive);
                viewLevel.setBackgroundColor(colorLineInactive);

                tvCriteria.setTextColor(colorActive);
                viewCriteria.setBackgroundColor(colorActive);

                scrollLevel.setVisibility(View.GONE);
                layoutCriteria.setVisibility(View.VISIBLE);
            });
        }
    }
}
