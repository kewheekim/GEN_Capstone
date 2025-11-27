package com.example.rally.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.CandidateResponseDto;
import com.example.rally.dto.InvitationAcceptRequest;
import com.example.rally.dto.InvitationAcceptResponse;
import com.example.rally.dto.MatchRequestDetails;
import com.example.rally.dto.MatchRequestInfoDto;
import com.example.rally.dto.InvitationRefuseRequest;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// MAT_REC_002
public class InvitationDetailsActivity extends AppCompatActivity {
    private Call<MatchRequestDetails> detailCall;
    ImageButton btnBack;
    TextView tvDateType, tvMyTime, tvMyPlace, tvName;
    ImageView profileImg;
    ImageView ivGender;
    ImageView ivProfile;
    TextView tvWin;
    TextView tvTime;
    TextView tvTimeState;
    ImageView ivTime;
    TextView tvPlace;
    TextView tvPlaceState;
    ImageView ivPlace;
    TextView tvStyle;
    ImageView ivTier;
    Button btnRequest;
    RatingBar ratingBar;
    Button btnRefuse;
    Button btnAccept;
    private Long invitationId;
    private String invitationState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_received_detail);
        btnBack = findViewById(R.id.btn_back);
        tvDateType = findViewById(R.id.tv_date_type);
        tvMyTime = findViewById(R.id.tv_my_time);
        tvMyPlace = findViewById(R.id.tv_my_place);
        profileImg = findViewById(R.id.iv_profile);
        tvName = findViewById(R.id.tv_opponent_name);
        tvWin = findViewById(R.id.tv_win_rate);
        ivProfile = findViewById(R.id.iv_profile);
        ivGender = findViewById(R.id.iv_gender);
        tvTime = findViewById(R.id.tv_time);
        tvTimeState = findViewById(R.id.tv_time_status);
        ivTime = findViewById(R.id.ic_time);
        tvPlace = findViewById(R.id.tv_opponent_place);
        tvPlaceState = findViewById(R.id.tv_place_status);
        ivPlace = findViewById(R.id.ic_place);
        tvStyle = findViewById(R.id.tv_style);
        ivTier = findViewById(R.id.iv_tier);
        btnRequest = findViewById(R.id.btn_request);
        ratingBar = findViewById(R.id.rating_bar);
        btnRefuse = findViewById(R.id.btn_refuse);
        btnAccept = findViewById(R.id.btn_accept);

        btnBack.setOnClickListener(v -> finish());

        String detailJson = getIntent().getStringExtra("detail_json");
        if (detailJson == null || detailJson.isEmpty()) {
            Toast.makeText(this, "상세 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MatchRequestDetails detail = new Gson().fromJson(detailJson, MatchRequestDetails.class);
        if (detail == null) {
            Toast.makeText(this, "정보 로드 실패", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MatchRequestInfoDto my = detail.getMy();
        CandidateResponseDto opponent = detail.getOpponent();
        if (opponent == null) {
            Toast.makeText(this, "상대 정보 없음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        invitationId = getIntent().getLongExtra("invitation_id", -1L);
        invitationState = getIntent().getStringExtra("invitation_state");
        boolean isSent = getIntent().getBooleanExtra("is_sent", false);
        if (isSent) {
            btnAccept.setVisibility(View.GONE);
            btnRefuse.setVisibility(View.GONE);
        } else if (invitationState != null) {
            switch(invitationState) {
                case "수락":
                case "취소":
                case "거절":
                case "경기확정":
                    btnAccept.setVisibility(View.GONE);
                    btnRefuse.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }

        btnAccept.setEnabled(true);
        btnAccept.setOnClickListener(v -> {
            if (invitationId == null || invitationId <= 0) {
                Toast.makeText(this, "invitationId가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            btnAccept.setEnabled(false);
            ApiService api = RetrofitClient.getSecureClient( this, BuildConfig.API_BASE_URL).create(ApiService.class);
            InvitationAcceptRequest body = new InvitationAcceptRequest(invitationId);

            api.acceptInvitation(body).enqueue(new Callback<InvitationAcceptResponse>() {
                @Override
                public void onResponse(Call<InvitationAcceptResponse> call, Response<InvitationAcceptResponse> resp) {
                    btnAccept.setEnabled(true);
                    if (resp.isSuccessful() && resp.body() != null) {
                        InvitationAcceptResponse data = resp.body();

                        Intent intent = new Intent(InvitationDetailsActivity.this, MatSuccessActivity.class);
                        intent.putExtra("game_id",          data.getGameId());
                        intent.putExtra("room_id",          data.getRoomId());
                        intent.putExtra("user_id", data.getUserId());
                        intent.putExtra("user_profile",     data.getUserProfile());
                        intent.putExtra("opponent_profile", data.getOpponentProfile());
                        intent.putExtra("opponent_name",    data.getOpponentName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(InvitationDetailsActivity.this, "수락 실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<InvitationAcceptResponse> call, Throwable t) {
                    btnAccept.setEnabled(true);
                    Toast.makeText(InvitationDetailsActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        bindMy(my);
        bindOpponent(opponent);
    }
    private void bindMy(MatchRequestInfoDto my) {
        if(my.getDate() != null && my.getGameType() != null) {
            LocalDate dateRaw = LocalDate.parse(my.getDate());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M월 d일(E)");
            String date = dateRaw.format(fmt);
            String gameType = my.getGameType();
            tvDateType.setText(date + " " +gameType + " 경기");
        }
        if (my.getTimeRange() != null) tvMyTime.setText(my.getTimeRange());
        if (my.getPlace() != null) {
            tvMyPlace.setText(my.getPlace());
        }
    }
    private void bindOpponent(CandidateResponseDto opponent) {
        // 닉네임
        if (opponent.getName() != null) tvName.setText(opponent.getName());
        // 프로필 이미지
        if (opponent.getProfileImage() != null && !opponent.getProfileImage().isEmpty()) {
            int sizePx = (int) (68 * getResources().getDisplayMetrics().density);
            Glide.with(this)
                    .load(opponent.getProfileImage())
                    .error(R.drawable.ic_default_profile)
                    .centerCrop()
                    .placeholder(R.drawable.ic_default_profile1)
                    .override(sizePx, sizePx)
                    .into(ivProfile);
        } else {
                profileImg.setImageResource(R.drawable.ic_default_profile);
            }

        // 성별
        if (opponent.getGender() == 0) {
            ivGender.setImageResource(R.drawable.ic_gender_male);
        } else {
            ivGender.setImageResource(R.drawable.ic_gender_female);
        }
        // 승률
        double wr = opponent.getWinningRate();
        if( wr != 0)
            tvWin.setText(String.format("최근 5경기 승률 %.0f%%", wr));
        else
            tvWin.setText("최근 5경기 승률 60%");
        // 시간
        if (opponent.getTime() != null) tvTime.setText(opponent.getTime());
        if (opponent.isSameTime()) {
            tvTimeState.setText("시간이 동일해요");
            tvTimeState.setTextColor(getColor(R.color.green_active));
            ivTime.setImageResource(R.drawable.ic_time_same);
        } else {
            tvTimeState.setText("시간이 일부 겹쳐요");
            tvTimeState.setTextColor(getColor(R.color.gray_500));
            ivTime.setImageResource(R.drawable.ic_time_diff);
        }
        // 위치
        if (opponent.getPlace() != null) tvPlace.setText(opponent.getPlace());
        if (opponent.isSamePlace()) {
            tvPlaceState.setText("위치가 동일해요");
            tvPlaceState.setTextColor(getColor(R.color.green_active));
            ivPlace.setImageResource(R.drawable.ic_location_same);
        } else {
            tvPlaceState.setText(String.format("%.1fkm 떨어져 있어요", opponent.getDistance()));
            tvPlaceState.setTextColor(getColor(R.color.gray_500));
            ivPlace.setImageResource(R.drawable.ic_location_diff);
        }
        // 경기 스타일
        String[] gameStyleMap = {"상관없어요", "편하게 즐겨요", "열심히 경기해요"};
        int gs = opponent.getGameStyle();
        if (gs >= 0 && gs < gameStyleMap.length) {
            tvStyle.setText(gameStyleMap[gs]);
        } else {
            tvStyle.setText("상관없어요");
        }
        // 매너 지수
        float manner = (float) opponent.getMannerScore();
        ratingBar.setRating(manner);

        // 티어
        switch (opponent.getTier()) {
            case 3:  ivTier.setImageResource(R.drawable.ic_tier_silver1); break;
            case 4:  ivTier.setImageResource(R.drawable.ic_tier_silver2); break;
            case 5: ivTier.setImageResource(R.drawable.ic_tier_silver3); break;
            default: ivTier.setImageResource(R.drawable.ic_tier_silver1);
        }

        btnRefuse.setOnClickListener(v ->
                showRefusalDialog()
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (detailCall != null) detailCall.cancel();
    }
    private void showRefusalDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_popup_invitation_refusal);

        MaterialButton btnPlace     = dialog.findViewById(R.id.btn_place);
        MaterialButton btnTime      = dialog.findViewById(R.id.btn_time);
        MaterialButton btnDuplicate = dialog.findViewById(R.id.btn_duplicate);
        MaterialButton btnOther     = dialog.findViewById(R.id.btn_other);
        MaterialButton btnRefusal   = dialog.findViewById(R.id.btn_refusal);
        ImageButton btnX     = dialog.findViewById(R.id.btn_x);

        List<MaterialButton> all = Arrays.asList(btnPlace, btnTime, btnDuplicate, btnOther);

        // 하나만 체크되도록
        View.OnClickListener singleSelect = v -> {
            for (MaterialButton b : all) {
                b.setChecked(b == v);
                b.setSelected(b == v);
            }
        };
        for (MaterialButton b : all) {
            b.setCheckable(true);
            b.setOnClickListener(singleSelect);
        }

        // 닫기
        btnX.setOnClickListener(v -> dialog.dismiss());

        // 거절 버튼 동작
        btnRefusal.setOnClickListener(v -> {
            String reasonText = null;
            for (int i = 0; i < all.size(); i++) {
                MaterialButton b = all.get(i);
                if (b.isChecked()) {
                    // \n 삭제
                    CharSequence label = b.getText();
                    reasonText = label != null ? label.toString().replace("\n", " ").trim() : null;
                    break;
                }
            }

            if (reasonText == null || reasonText.isEmpty()) {
                Toast.makeText(this, "사유를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRefusal.setEnabled(false);

            ApiService api = RetrofitClient
                    .getSecureClient(this, BuildConfig.API_BASE_URL)
                    .create(ApiService.class);

            InvitationRefuseRequest body = new InvitationRefuseRequest(
                    invitationId,
                    reasonText
            );

            api.refuseInvitation(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> resp) {
                    btnRefusal.setEnabled(true);
                    if (resp.isSuccessful()) {
                        dialog.dismiss();
                        Intent intent = new Intent(InvitationDetailsActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("navigateTo", "invitation_received");
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(InvitationDetailsActivity.this, "거절 실패(" + resp.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    btnRefusal.setEnabled(true);
                    Toast.makeText(InvitationDetailsActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
