package com.example.rally.adapter;

import com.example.rally.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.dto.MatchFoundItem;
import com.google.android.material.card.MaterialCardView;


public class MatchFoundAdapter extends ListAdapter<MatchFoundItem, MatchFoundAdapter.VH> {
    public interface OnItemClickListener {
        void onItemClick(@NonNull MatchFoundItem item);
        void onChatClick(@NonNull MatchFoundItem item);
        void onMoreClick(@NonNull MatchFoundItem item);
    }

    @NonNull private final OnItemClickListener listener;

    public MatchFoundAdapter(@NonNull OnItemClickListener listener) {
        super(new MatchFoundDiff());
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Long id = getItem(position).getGameId();
        return id == null ? RecyclerView.NO_ID : (id.hashCode() & 0xffffffffL);
    }

    static class VH extends RecyclerView.ViewHolder {
        final MaterialCardView speechCard;
        final TextView tvState, tvDateType, tvTimeTitle, tvTime, tvPlaceTitle, tvPlace, tvOpponent;
        final ImageView ivProfile;
        final ImageButton btnChat, btnMore;

        VH(@NonNull View itemView) {
            super(itemView);
            speechCard = itemView.findViewById(R.id.speech_card);
            tvState = itemView.findViewById(R.id.tv_state);
            tvDateType = itemView.findViewById(R.id.tv_date_type);
            tvTimeTitle =itemView.findViewById(R.id.tv_time_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvPlaceTitle =itemView.findViewById(R.id.tv_place_title);
            tvPlace = itemView.findViewById(R.id.tv_place);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvOpponent  = itemView.findViewById(R.id.tv_opponent);
            btnChat = itemView.findViewById(R.id.btn_chat);
            btnMore = itemView.findViewById(R.id.btn_more);
        }

        void bind(@NonNull MatchFoundItem item, @NonNull OnItemClickListener listener) {
            Context ctx = speechCard.getContext();
            String stateRaw = item.getState();
            String stateText = stateRaw;
            switch (stateText) {
                case "수락":
                    stateText = "경기 약속 확정 전";
                    speechCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.gray100));
                    speechCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.gray_nav_bar));
                    tvState.setTextColor(ContextCompat.getColor(ctx, R.color.gray_text));
                    break;
                case "경기확정":
                    stateText = "경기 약속 확정 완료";
                    speechCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.green_select));
                    speechCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.green_active));
                    tvState.setTextColor(ContextCompat.getColor(ctx, R.color.green_active));
                    tvTimeTitle.setVisibility(View.VISIBLE);
                    tvTime.setVisibility(View.VISIBLE);
                    tvPlaceTitle.setVisibility(View.VISIBLE);
                    tvPlace.setVisibility(View.VISIBLE);
                    tvTime.setText(item.getTime());
                    tvPlace.setText(item.getPlace());
                    break;
                default:
                    stateText = stateText; break;
            }
            tvState.setText(stateText);
            tvDateType.setText(item.getDate() + " " + item.getGameType() + "경기");
            tvOpponent.setText(item.getOpponentName());

            // 프로필 사진 로드
            try {
                if (item.getOpponentProfile() != null && !item.getOpponentProfile().isEmpty()) {
                    Glide.with(ivProfile.getContext())
                            .load(item.getOpponentProfile())
                            .placeholder(R.drawable.ic_default_profile)
                            .error(R.drawable.ic_default_profile)
                            .into(ivProfile);
                } else {
                    ivProfile.setImageResource(R.drawable.ic_default_profile);
                }
            } catch (Throwable t) {
                ivProfile.setImageResource(R.drawable.ic_default_profile);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            btnChat.setOnClickListener(v -> listener.onChatClick(item));
            btnMore.setOnClickListener(v -> listener.onMoreClick(item));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_found, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), listener);
    }
}