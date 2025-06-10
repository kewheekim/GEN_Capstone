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
import com.example.rally.R;
import com.example.rally.dto.CandidateResponseDto;

public class PopupPartner1Activity extends AppCompatActivity {

    private CandidateResponseDto user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_partner1);

        user = (CandidateResponseDto) getIntent().getSerializableExtra("user");

        // ui
        ImageButton xBtn = findViewById(R.id.x_btn);
        ImageView profileImg = findViewById(R.id.profile_img);
        TextView nickname = findViewById(R.id.tv_nickname);
        ImageView genderIcon = findViewById(R.id.iv_gender); // 성별 이미지
        TextView winRate = findViewById(R.id.tv_win_rate);
        TextView time = findViewById(R.id.tv_time);
        TextView timeStatus = findViewById(R.id.tv_time_status);
        ImageView timeIcon = findViewById(R.id.ic_time);
        TextView location = findViewById(R.id.tv_location);
        TextView locationStatus = findViewById(R.id.tv_location_status);
        ImageView locationIcon = findViewById(R.id.ic_location);
        TextView style = findViewById(R.id.tv_style);
        ImageView tier = findViewById(R.id.iv_tier);
        Button requestBtn = findViewById(R.id.request_button);
        RatingBar ratingBar = findViewById(R.id.rating_bar);

        // 3. 데이터 바인딩
        nickname.setText(user.getName());

        if (user.getProfileImage() != null) {
            Glide.with(this)
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.ic_default_profile)
                    .circleCrop()
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
            genderIcon.setImageResource(R.drawable.ic_gender_male);
        } else {
            genderIcon.setImageResource(R.drawable.ic_gender_female);
        }

        winRate.setText(String.format("최근 5경기 승률 %.0f%%", user.getWinningRate()));
        time.setText(user.getTime());

        if (user.isSameTime()) {
            timeStatus.setText("시간이 동일해요");
            timeStatus.setTextColor(getColor(R.color.green_active));
            timeIcon.setImageResource(R.drawable.ic_circle);
        } else {
            timeStatus.setText("시간이 일부 겹쳐요");
            timeStatus.setTextColor(getColor(R.color.pink));
            timeIcon.setImageResource(R.drawable.ic_circlehalf);
        }

        location.setText(user.getPlace());

        if (user.isSamePlace()) {
            locationStatus.setText("위치가 동일해요");
            locationStatus.setTextColor(getColor(R.color.green_active));
            locationIcon.setImageResource(R.drawable.ic_circle);
        } else {
            locationStatus.setText(String.format("%.1fkm 떨어져 있어요", user.getDistance()));
            locationStatus.setTextColor(getColor(R.color.pink));
            locationIcon.setImageResource(R.drawable.ic_circlehalf);
        }

        // 경기 스타일 (예시 매핑)
        String[] gameStyleMap = {"상관없어요", "편하게 즐겨요", "열심히 경기해요"};
        style.setText(gameStyleMap[user.getGameStyle()]);

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
                 tier.setImageResource(R.drawable.ic_tier_silver1);
                 break;
            case 4:
                 tier.setImageResource(R.drawable.ic_tier_silver2);
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
        xBtn.setOnClickListener(v -> finish());

        // 매칭 요청 버튼
        requestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PopupPartner1Activity.this, MatchInviteActivity.class);
            intent.putExtra("partnerName", user.getName());
            startActivity(intent);
        });
    }
}