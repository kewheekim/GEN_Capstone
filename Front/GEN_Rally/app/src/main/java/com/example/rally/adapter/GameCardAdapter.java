package com.example.rally.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
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

    public interface OnChatButtonClickListener {
        void onChatButtonClick(long roomId);
    }

    private OnChatButtonClickListener chatButtonClickListener;
    private static final SparseBooleanArray flippedStates = new SparseBooleanArray();
    private List<MatchInfoDto> gameList = new ArrayList<>();
    private Context context;

    public void setOnChatButtonClickListener(OnChatButtonClickListener listener) {
        this.chatButtonClickListener = listener;
    }
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
        return new GameViewHolder(view, chatButtonClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        MatchInfoDto game = gameList.get(position);
        boolean isFlipped = flippedStates.get(position, false); // 뒤집혔는지: 기본값 false
        holder.bind(game, context, position, isFlipped);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        View layoutInfoCard;   // 앞면
        View layoutStartCard;  // 뒷면
        ImageButton btnStartGame, btnChat, btnBackToInfo;
        TextView tvMatchDate, tvMatchTime, tvMatchPlace, tvMatchStyle, tvOpponentName;
        ImageView ivOpponentProfile;
        OnChatButtonClickListener chatButtonClickListener;

        public GameViewHolder(@NonNull View itemView, OnChatButtonClickListener listener) {
            super(itemView);
            this.chatButtonClickListener = listener;

            layoutInfoCard = itemView.findViewById(R.id.layout_info_card);
            layoutStartCard = itemView.findViewById(R.id.layout_start_card);

            btnBackToInfo = itemView.findViewById(R.id.btn_back_info);
            btnStartGame = itemView.findViewById(R.id.btn_start_game);
            btnChat = itemView.findViewById(R.id.btn_chat);
            tvMatchDate = itemView.findViewById(R.id.tv_match_date);
            tvMatchTime = itemView.findViewById(R.id.tv_match_time);
            tvMatchPlace = itemView.findViewById(R.id.tv_match_place);
            tvMatchStyle = itemView.findViewById(R.id.tv_match_style);
            tvOpponentName = itemView.findViewById(R.id.tv_opponent_name);
            ivOpponentProfile = itemView.findViewById(R.id.iv_opponent_profile);
        }

        public void bind(MatchInfoDto game, Context context, int position, boolean isFlipped) {
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

            if (isFlipped) {
                layoutInfoCard.setVisibility(View.GONE);
                layoutStartCard.setVisibility(View.VISIBLE);
            } else {
                layoutInfoCard.setVisibility(View.VISIBLE);
                layoutStartCard.setVisibility(View.GONE);
            }

            // 앞면 -> 뒷면
            btnStartGame.setOnClickListener(v -> {
                flippedStates.put(position, true);
                layoutInfoCard.setVisibility(View.GONE);
                layoutStartCard.setVisibility(View.VISIBLE);
            });

            // 뒷면 -> 앞면
            btnBackToInfo.setOnClickListener(v -> {
                flippedStates.delete(position);
                layoutStartCard.setVisibility(View.GONE);
                layoutInfoCard.setVisibility(View.VISIBLE);
            });

            btnChat.setOnClickListener(v -> {
                if (chatButtonClickListener != null) {
                    chatButtonClickListener.onChatButtonClick(game.getRoomId());
                }
            });
        }
    }
}
