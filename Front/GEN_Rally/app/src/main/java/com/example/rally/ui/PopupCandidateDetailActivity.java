package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.rally.R;
import com.example.rally.dto.CandidateResponseDto;

// MAT_AP_S_004
public class PopupCandidateDetailActivity extends AppCompatActivity {

    private CandidateResponseDto user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_candidate_detail);

        user = (CandidateResponseDto) getIntent().getSerializableExtra("user");
        long requestId = getIntent().getLongExtra("requestId", -1);
        // ui
        ImageButton btnX = findViewById(R.id.btn_x);
        ImageView profileImg = findViewById(R.id.iv_profile);
        TextView tvName = findViewById(R.id.tv_nickname);
        ImageView ivGender = findViewById(R.id.iv_gender);
        TextView tvWin = findViewById(R.id.tv_win_rate);
        TextView tvTime = findViewById(R.id.tv_time);
        TextView tvTimeState = findViewById(R.id.tv_time_status);
        ImageView ivTime = findViewById(R.id.ic_time);
        TextView tvPlace = findViewById(R.id.tv_opponent_place);
        TextView tvPlaceState = findViewById(R.id.tv_place_status);
        ImageView ivPlace = findViewById(R.id.ic_place);
        TextView tvStyle = findViewById(R.id.tv_opponent_style);
        ImageView ivTier = findViewById(R.id.iv_tier);
        Button btnRequest = findViewById(R.id.btn_request);
        RatingBar ratingBar = findViewById(R.id.rating_bar);

        // 3. 데이터 바인딩
        tvName.setText(user.getName());

        if (user.getProfileImage() != null) {
            Glide.with(this)
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.ic_default_profile)
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners((int) (10 * getResources().getDisplayMetrics().density)))
                            .placeholder(R.drawable.ic_default_profile))
                    .into(profileImg);
        }
        else {
            // 프로필 이미지 하드코딩
            if(user.getName().equals("아어려워요"))
                profileImg.setImageResource(R.drawable.profile_image_male);
            else if(user.getName().equals("흠냐링"))
                profileImg.setImageResource(R.drawable.profile_image_female1);
            else if(user.getName().equals("안세영이되"))
                profileImg.setImageResource(R.drawable.profile_image_female2);
        }

        if (user.getGender() == 0) {
            ivGender.setImageResource(R.drawable.ic_gender_male);
        } else {
            ivGender.setImageResource(R.drawable.ic_gender_female);
        }

        tvWin.setText(String.format("최근 5경기 승률 %.0f%%", user.getWinningRate()));
        tvTime.setText(user.getTime());

        if (user.isSameTime()) {
            tvTimeState.setText("시간이 동일해요");
            tvTimeState.setTextColor(getColor(R.color.green_active));
            ivTime.setImageResource(R.drawable.ic_circle);
        } else {
            tvTimeState.setText("시간이 일부 겹쳐요");
            tvTimeState.setTextColor(getColor(R.color.pink));
            ivTime.setImageResource(R.drawable.ic_circlehalf);
        }

        tvPlace.setText(user.getPlace());

        if (user.isSamePlace()) {
            tvPlaceState.setText("위치가 동일해요");
            tvPlaceState.setTextColor(getColor(R.color.green_active));
            ivPlace.setImageResource(R.drawable.ic_circle);
        } else {
            tvPlaceState.setText(String.format("%.1fkm 떨어져 있어요", user.getDistance()));
            tvPlaceState.setTextColor(getColor(R.color.pink));
            ivPlace.setImageResource(R.drawable.ic_circlehalf);
        }

        // 경기 스타일 (예시 매핑)
        String[] gameStyleMap = {"상관없어요", "편하게 즐겨요", "열심히 경기해요"};
        tvStyle.setText(gameStyleMap[user.getGameStyle()]);

        // 매너 지수
        if(user.getMannerScore()!=0) {

        }
        ratingBar.setRating((float) user.getMannerScore());

        switch (user.getTier()) {
            case 0:
                //tier.setImageResource(R.drawble.ic_tier_bronze1);
                break;
            case 1:
                //tier.setImageResource(R.drawble.ic_tier_bronze2);
                break;
            case 2:
                //tier.setImageResource(R.drawble.ic_tier_bronze3);
                break;
            case 3:
                 ivTier.setImageResource(R.drawable.ic_tier_silver1);
                 break;
            case 4:
                 ivTier.setImageResource(R.drawable.ic_tier_silver2);
                 break;
            case 5:
                //tier.setImageResource(R.drawable.ic_tier_silver3);
                break;
            case 6:
                //tier.setImageResource(R.drawble.ic_tier_gold1);
                break;
            case 7:
                //tier.setImageResource(R.drawble.ic_tier_gold2);
                break;
            case 8:
                //tier.setImageResource(R.drawble.ic_tier_gold3);
                break;
            case 9:
                //tier.setImageResource(R.drawble.ic_tier_platinum1);
                break;
            case 10:
                //tier.setImageResource(R.drawble.ic_tier_platinum2);
                break;
            case 11:
                //tier.setImageResource(R.drawble.ic_tier_platinum3);
                break;
            default:
                //tier.setImageResource(R.drawble.ic_tier_platinum1);
        }

        // X 버튼: 닫기
        btnX.setOnClickListener(v -> finish());

        // 매칭 요청 버튼
        btnRequest.setOnClickListener(v -> {
            Intent intent = new Intent(PopupCandidateDetailActivity.this, PopupInviteActivity.class);
            intent.putExtra("opponentId", user.getUserId());
            intent.putExtra("opponentName", user.getName());
            intent.putExtra("opponentRequestId", user.getRequestId());
            intent.putExtra("myRequestId", requestId);
            startActivity(intent);
        });
    }
}