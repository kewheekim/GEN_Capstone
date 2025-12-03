package com.example.rally.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.dto.CommentDto;
import com.example.rally.dto.GameReviewDto;

import java.util.List;

public class RecordCommentAdapter extends RecyclerView.Adapter<RecordCommentAdapter.CommentViewHolder> {
    private List<CommentDto> items;
    public RecordCommentAdapter(List<CommentDto> items) {
        this.items = items;
    }
    public void setItems(List<CommentDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordCommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_record_comment, parent, false);
        return new RecordCommentAdapter.CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordCommentAdapter.CommentViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTier, tvStyle, tvDate, tvComment;
        ImageView ivOpponentProfile;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_opponent_name);
            tvTier = itemView.findViewById(R.id.tv_tier);
            tvStyle = itemView.findViewById(R.id.tv_game_style);
            tvDate = itemView.findViewById(R.id.tv_game_date);
            tvComment = itemView.findViewById(R.id.tv_comment);

            ivOpponentProfile = itemView.findViewById(R.id.iv_opponent_profile);
        }

        public void bind(CommentDto item) {
            tvName.setText(String.valueOf(item.getNickname()));
            tvTier.setText(String.valueOf(item.getTier()));
            tvStyle.setText(item.getGameStyle());
            tvDate.setText(String.valueOf(item.getDate()));
            tvComment.setText(String.valueOf(item.getComment()));

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
