package com.example.rally.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.dto.MatchInfoDto;
import com.example.rally.dto.MatchRequestInfoDto;
import com.example.rally.ui.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class GameCardAdapter extends RecyclerView.Adapter<GameCardAdapter.GameViewHolder>{

    private List<MatchInfoDto> gameList = new ArrayList<>();
    private Context context;

    // 데이터 리스트 업데이트
    public void setGameList(List<MatchInfoDto> gameList) {
        this.gameList = gameList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_main_game_card, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        MatchInfoDto game = gameList.get(position);
        holder.bind(game, context);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageButton btnStartGame, btnChat;
        TextView tvMatchDate, tvMatchTime, tvMatchPlace, tvMatchStyle, tvOpponentName;
        ImageView ivOpponentProfile;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);

            btnStartGame = itemView.findViewById(R.id.btn_start_game);
            btnChat = itemView.findViewById(R.id.btn_chat);
            tvMatchDate = itemView.findViewById(R.id.tv_match_date);
            tvMatchTime = itemView.findViewById(R.id.tv_match_time);
            tvMatchPlace = itemView.findViewById(R.id.tv_match_place);
            tvMatchStyle = itemView.findViewById(R.id.tv_match_style);
            tvOpponentName = itemView.findViewById(R.id.tv_opponent_name);
            ivOpponentProfile = itemView.findViewById(R.id.iv_opponent_profile);
        }

        public void bind(MatchInfoDto game, Context context) {
            tvMatchDate.setText(game.getDate());
            tvMatchTime.setText(game.getTimeRange());
            tvMatchPlace.setText(game.getPlace());
            tvMatchStyle.setText(game.getGameStyle());
            tvOpponentName.setText(game.getOpponentName());

            if (game.getOpponentProfileUrl() != null) {
                 Glide.with(context)
                      .load(game.getOpponentProfileUrl())
                      .into(ivOpponentProfile);
             } else {
            ivOpponentProfile.setImageResource(R.drawable.profile_image_male);
             }

            // 카드 내 버튼 클릭 리스너
            btnStartGame.setOnClickListener(v -> {
                // TODO: 경기 시작 카드로 바꾸기
            });

            btnChat.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(ChatActivity.ROOM_ID, game.getRoomId()); // 채팅방 ID
                // intent.putExtra(ChatActivity.MY_USER_ID, currentUserId);
                context.startActivity(intent);
            });
        }
    }
}
