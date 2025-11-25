package com.example.rally.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        Button btnRecordStart;
        TextView tvMatchDate, tvMatchTime, tvMatchPlace, tvMatchStyle, tvOpponentName;
        TextView tvNotYet, tvMainText;
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
            btnRecordStart = itemView.findViewById(R.id.btn_real_start);

            tvMatchDate = itemView.findViewById(R.id.tv_match_date);
            tvMatchTime = itemView.findViewById(R.id.tv_match_time);
            tvMatchPlace = itemView.findViewById(R.id.tv_match_place);
            tvMatchStyle = itemView.findViewById(R.id.tv_match_style);
            tvOpponentName = itemView.findViewById(R.id.tv_opponent_name);
            tvNotYet = itemView.findViewById(R.id.tv_not_yet);
            tvMainText = itemView.findViewById(R.id.tv_main_text);

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

            // 날짜 비교해서 게임 카드 활성화
            if (isDateToday(game.getDate())) {
                tvNotYet.setText("경기 전 준비 운동은 필수!");
                tvMainText.setText("경기 준비가 됐다면\n기록을 시작하세요");
                btnRecordStart.setEnabled(true);
            } else {
                tvNotYet.setText("아직 경기 당일이 아니에요");
                tvMainText.setText("경기 당일이 되면\n기록을 시작할 수 있어요");
                btnRecordStart.setEnabled(false);
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

        private boolean isDateToday(String matchDateStr) {
            if (matchDateStr == null) return false;
            LocalDate now = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREA);
            String todayStr = now.format(formatter);

            // 문자열이 포함되어 있는지 확인 (혹시 공백 등이 다를 수 있으므로)
            // 예: "11월 26일 (수)" == "11월 26일 (수)"
            return matchDateStr.equals(todayStr);

            // 만약 서버 데이터 형식이 "yyyy-MM-dd"라면 아래처럼 파싱해서 비교ㅅ해야 함
            // return LocalDate.parse(matchDateStr).isEqual(now);
        }
    }
}
