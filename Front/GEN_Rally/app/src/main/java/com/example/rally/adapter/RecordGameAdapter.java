package com.example.rally.adapter;

import android.content.Context;
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

import java.util.List;

public class RecordGameAdapter extends RecyclerView.Adapter<RecordGameAdapter.GameViewHolder>{
    private List<GameReviewDto> items;
    public RecordGameAdapter(List<GameReviewDto> items) {
        this.items = items;
    }
    public void setItems(List<GameReviewDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordGameAdapter.GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record_game_card, parent, false);
        return new RecordGameAdapter.GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordGameAdapter.GameViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    class GameViewHolder extends RecyclerView.ViewHolder {
        TextView tvOpponentScore, tvMyScore, tvTime, tvSteps, tvKcal;
        ImageView ivOpponentProfile;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOpponentScore = itemView.findViewById(R.id.tv_opponent_score);
            tvMyScore = itemView.findViewById(R.id.tv_my_score);
            tvTime = itemView.findViewById(R.id.tv_record_time);
            tvSteps = itemView.findViewById(R.id.tv_record_walk);
            tvKcal = itemView.findViewById(R.id.tv_record_kcal);

            ivOpponentProfile = itemView.findViewById(R.id.iv_opponent_profile);
        }

        public void bind(GameReviewDto item) {
            tvOpponentScore.setText(String.valueOf(item.getOpponentScore()));
            tvMyScore.setText(String.valueOf(item.getMyScore()));
            tvTime.setText(item.getPlayTime());
            tvSteps.setText(String.valueOf(item.getSteps()));
            tvKcal.setText(String.valueOf(item.getCalories()));

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
