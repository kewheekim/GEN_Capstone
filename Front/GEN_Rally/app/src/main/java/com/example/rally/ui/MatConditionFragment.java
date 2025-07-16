package com.example.rally.ui;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rally.R;


public class MatConditionFragment extends Fragment {
    private static final String ARG_QUESTION = "question";
    private static final String ARG_OPTIONS = "options";

    public static MatConditionFragment newInstance(String question, String[] options) {
        MatConditionFragment fragment = new MatConditionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION, question);
        args.putStringArray(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_single, container, false);

        TextView questionText = view.findViewById(R.id.text_question);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        Button nextBtn = view.findViewById(R.id.btn_next).findViewById(R.id.btn_next);

        // 인자 받아오기
        Bundle args = getArguments();
        if (args != null) {
            questionText.setText(args.getString(ARG_QUESTION));

            String[] options = args.getStringArray(ARG_OPTIONS);
            if (options != null) {
                for (String option : options) {
                    ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(), R.style.MyCustomRadioButton);
                    RadioButton rb = new RadioButton(wrapper);
                    rb.setText(option);
                    rb.setTextColor(Color.parseColor("#676769"));
                    rb.setTextSize(18);
                    rb.setButtonDrawable(null);
                    float extra = 5 * getResources().getDisplayMetrics().density;  // 5dp -> px
                    rb.setLineSpacing(extra, 1f);

                    Drawable radioDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.custom_radio_button);
                    rb.setCompoundDrawablesWithIntrinsicBounds(null, null, radioDrawable, null); // 오른쪽에만 설정

                    rb.setCompoundDrawablePadding((int) (16 * getResources().getDisplayMetrics().density)); // 16dp

                    rb.setBackgroundResource(R.drawable.bg_selector_radio_button);


                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            (int) (88 * getResources().getDisplayMetrics().density) // 88dp
                    );
                    int marginBottom = (int) (13 * getResources().getDisplayMetrics().density); // 13dp -> px
                    params.setMargins(0, 0, 0, marginBottom);

                    rb.setLayoutParams(params);

                    // 내부 패딩 (글자 위치 조정용)
                    rb.setPadding(
                            (int) (24 * getResources().getDisplayMetrics().density),  // left padding
                            (int) (16 * getResources().getDisplayMetrics().density),  // top
                            (int) (24 * getResources().getDisplayMetrics().density),  // right
                            (int) (16 * getResources().getDisplayMetrics().density)   // bottom
                    );

                    radioGroup.addView(rb);
                }
                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    if (checkedId != -1) {
                        nextBtn.setEnabled(true);
                        nextBtn.setTextColor(Color.parseColor("#FFFFFF"));
                        nextBtn.setBackgroundResource(R.drawable.bg_next_button_active);
                    } else {
                        nextBtn.setEnabled(false);
                        nextBtn.setBackgroundResource(R.drawable.bg_next_button_inactive);
                    }
                });
            }
        }

        nextBtn.setOnClickListener(v -> {
            int checkedId = radioGroup.getCheckedRadioButtonId();
            if (checkedId != -1) {
                RadioButton selected = view.findViewById(checkedId);
                ((MatConditionActivity) requireActivity()).goToNextQuestion(selected.getText().toString());
            }
        });

        return view;
    }
}
