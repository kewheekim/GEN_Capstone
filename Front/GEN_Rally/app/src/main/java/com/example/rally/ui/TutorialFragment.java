package com.example.rally.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.rally.R;

public class TutorialFragment extends Fragment {
    private static final String ARG_TITLE = "t";
    private static final String ARG_DESC = "d";
    private static final String ARG_IMG = "img";
    private static final String ARG_BIAS = "bias";

    public static TutorialFragment newInstance(String title, String desc, @DrawableRes int img, float bias){
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESC, desc);
        args.putInt(ARG_IMG, img);
        args.putFloat(ARG_BIAS, bias);
        TutorialFragment f = new TutorialFragment();
        f.setArguments(args);
        return f;
    }

    public TutorialFragment() {
        super(R.layout.fragment_tutorial);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        ImageView ivTut = view.findViewById(R.id.iv_tutorial);

        Bundle b = getArguments();
        tvTitle.setText(b.getString(ARG_TITLE));
        tvDesc.setText(b.getString(ARG_DESC));
        ivTut.setImageResource(b.getInt(ARG_IMG));

        // bias 적용 (0=위, 0.5=중앙, 1=아래)
        ConstraintLayout.LayoutParams lp =
                (ConstraintLayout.LayoutParams) ivTut.getLayoutParams();
        lp.verticalBias = b.getFloat(ARG_BIAS, 0.5f);
        ivTut.setLayoutParams(lp);
    }
}
