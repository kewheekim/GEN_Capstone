package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.rally.R;
import com.google.android.material.imageview.ShapeableImageView;

public class MatSuccessActivity extends AppCompatActivity {

    private ShapeableImageView ivMyProfile, ivOpponentProfile;
    private TextView tvInform;
    private Button btnGoChat;

    private Long gameId;
    private Long roomId;
    private String userId;
    private String myProfileUrl;
    private String opponentProfileUrl;
    private String opponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_match);

        ivMyProfile = findViewById(R.id.iv_my_profile);
        ivOpponentProfile = findViewById(R.id.iv_opponent_profile);
        tvInform   = findViewById(R.id.tv_match_inform);
        btnGoChat = findViewById(R.id.btn_go_chat);

        Intent it = getIntent();
        gameId  =  it.getLongExtra("game_id", -1L);
        roomId= it.getLongExtra("room_id", -1L);
        userId = it.getStringExtra("user_id");
        myProfileUrl = it.getStringExtra("user_profile");
        opponentProfileUrl = it.getStringExtra("opponent_profile");
        opponentName = it.getStringExtra("opponent_name");

        if (!TextUtils.isEmpty(opponentName)) {
            tvInform.setText(opponentName + "님과 매칭되었어요");
        } else {
            tvInform.setText("매칭이 성사되었어요");
        }

        Glide.with(this)
                .load(myProfileUrl)
                .error(R.drawable.ic_default_profile)
                .apply(new RequestOptions()
                        .transform(new RoundedCorners((int) (10 * getResources().getDisplayMetrics().density)))
                .placeholder(R.drawable.ic_default_profile))
                .into(ivMyProfile);

        Glide.with(this)
                .load(opponentProfileUrl)
                .error(R.drawable.ic_default_profile)
                .apply(new RequestOptions()
                        .transform(new RoundedCorners((int) (10 * getResources().getDisplayMetrics().density)))
                        .placeholder(R.drawable.ic_default_profile))
                .into(ivOpponentProfile);

        btnGoChat.setOnClickListener(v -> {
            if (roomId == null || roomId <= 0) {
                Toast.makeText(this, "채팅방 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // todo: ChatActivity 연결
             Intent chat = new Intent(this, ChatActivity.class);
             chat.putExtra(ChatActivity.ROOM_ID, roomId);
             chat.putExtra(ChatActivity.MY_USER_ID, userId);
             startActivity(chat);

            Toast.makeText(this,
                    "roomId=" + roomId + "채팅 화면 이동",
                    Toast.LENGTH_SHORT).show();
        });
    }
}
