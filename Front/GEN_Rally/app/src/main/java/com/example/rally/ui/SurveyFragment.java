package com.example.rally.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SurveyFragment extends Fragment {
    private static final String ARG_QUESTION = "arg_question";
    private static final String ARG_OPTIONS  = "arg_options";

    public static SurveyFragment newInstance(String question, String[] options){
        SurveyFragment fragment = new SurveyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION, question);
        args.putStringArray(ARG_OPTIONS, options);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_survey, container, false);
        TextView questionText = view.findViewById(R.id.text_question);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        Button nextBtn = view.findViewById(R.id.btn_next);

        // 인자 받아오기
        Bundle args = getArguments();
        if (args != null) {
            questionText.setText(args.getString(ARG_QUESTION));
            String[] options = args.getStringArray(ARG_OPTIONS);

            if (options != null) {
                for (int i = 0; i < options.length; i++) {
                    ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(), R.style.MyCustomRadioButton);
                    RadioButton rb = new RadioButton(wrapper);
                    rb.setTag(i);
                    rb.setId(View.generateViewId());

                    rb.setText(parseGreenTag(requireContext(), options[i])); // <green> 태그 파싱
                    rb.setTextColor(Color.parseColor("#676769"));
                    rb.setTextSize(14);
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
                int idx = (int) selected.getTag();
                ((SurveyActivity) requireActivity()).goToNextQuestion(idx);
            }
        });

        return view;
    }
    private CharSequence parseGreenTag(Context ctx, String raw) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        Pattern p = Pattern.compile("<green>(.+?)</green>");
        Matcher m = p.matcher(raw);
        int last = 0;
        int greenColor = ContextCompat.getColor(ctx, R.color.green_active);
        while (m.find()) {
            // 태그 전 텍스트
            ssb.append(raw, last, m.start());
            // 태그 안 텍스트
            int spanStart = ssb.length();
            ssb.append(m.group(1));
            int spanEnd = ssb.length();
            ssb.setSpan(new ForegroundColorSpan(greenColor),
                    spanStart, spanEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            last = m.end();
        }
        // 마지막 남은 텍스트
        ssb.append(raw, last, raw.length());
        return ssb;
    }
}
