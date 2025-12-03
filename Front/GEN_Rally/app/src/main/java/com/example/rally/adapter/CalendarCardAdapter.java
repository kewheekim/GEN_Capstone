package com.example.rally.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.dto.GameReviewDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class CalendarCardAdapter extends RecyclerView.Adapter<CalendarCardAdapter.GameViewHolder>{
    private List<GameReviewDto> items;
    public CalendarCardAdapter(List<GameReviewDto> items) {
        this.items = items;
    }
    public void setItems(List<GameReviewDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
    public interface OnItemClickListener {
        void onItemClick(Long gameId);
    }
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarCardAdapter.GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record_calendar_card, parent, false);
        return new CalendarCardAdapter.GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarCardAdapter.GameViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvOpponentScore, tvMyScore, tvTime, tvSteps, tvKcal, tvDate, tvWin;
        ImageView ivOpponentProfile;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOpponentScore = itemView.findViewById(R.id.tv_opponent_score);
            tvMyScore = itemView.findViewById(R.id.tv_my_score);
            tvTime = itemView.findViewById(R.id.tv_record_time);
            tvSteps = itemView.findViewById(R.id.tv_record_walk);
            tvKcal = itemView.findViewById(R.id.tv_record_kcal);
            tvDate = itemView.findViewById(R.id.tv_calender_date);
            tvWin = itemView.findViewById(R.id.tv_win_lose);

            ivOpponentProfile = itemView.findViewById(R.id.iv_opponent_profile);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(items.get(position).getGameId());
                }
            });
        }

        public void bind(GameReviewDto item) {
            tvOpponentScore.setText(String.valueOf(item.getOpponentScore()));
            tvMyScore.setText(String.valueOf(item.getMyScore()));
            tvTime.setText(item.getPlayTime());
            tvSteps.setText(String.valueOf(item.getSteps()));
            tvKcal.setText(String.valueOf(item.getCalories()));

            if (item.getDate() != null) {
                try {
                    LocalDate date = LocalDate.parse(item.getDate().toString());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d일 EEEE", Locale.KOREAN);
                    tvDate.setText(date.format(formatter));
                } catch (Exception e) {
                    tvDate.setText(item.getDate().toString());
                }
            }

            if(item.getOpponentScore()>item.getMyScore()){
                tvWin.setText("패");
                tvWin.setTextColor(Color.parseColor("#F9B6D6"));
            } else if(item.getOpponentScore()<item.getMyScore()){
                tvWin.setText("승");
                tvWin.setTextColor(Color.parseColor("#AAE4C7"));
            }

            Glide.with(itemView.getContext())
                    .load(item.getOpponentImage())
                    .placeholder(R.drawable.ic_default_profile1)
                    .into(ivOpponentProfile);
        }
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

}
