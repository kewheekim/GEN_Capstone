package com.example.rally.adapter;

import com.example.rally.R;
import com.example.rally.dto.MatchSeekingItem;
import com.google.android.material.card.MaterialCardView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class MatchSeekingAdapter extends ListAdapter<MatchSeekingItem, MatchSeekingAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(MatchSeekingItem item, int position);
    }
    public interface OnCandidatesClickListener {
        void onCandidatesClick(MatchSeekingItem item, int position);
    }
    public interface OnMoreClickListener {
        void onMoreClick(View anchor, MatchSeekingItem item, int position);
    }

    private OnItemClickListener itemClickListener;
    private OnCandidatesClickListener candidatesClickListener;
    private OnMoreClickListener moreClickListener;

    public MatchSeekingAdapter(OnItemClickListener onItemClickListener) {
        super(new MatchSeekingDiff());
        setHasStableIds(true);
        this.itemClickListener = onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener l) { this.itemClickListener = l; }
    public void setOnCandidatesClickListener(OnCandidatesClickListener l) { this.candidatesClickListener = l; }
    public void setOnMoreClickListener(OnMoreClickListener l) { this.moreClickListener = l; }

    @Override
    public long getItemId(int position) {
        MatchSeekingItem item = getItem(position);
        return item.getRequestId() == null ? RecyclerView.NO_ID : item.getRequestId();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match_seeking, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MatchSeekingItem item = getItem(position);

        String stateRaw = item.getState();
        String stateText = stateRaw;
        switch (stateText) {
            case "대기" : stateText = "요청보류 중"; break;
            case "요청중" : stateText = "상대수락 대기 중"; break;
            default: stateText = stateText; break;
        }
        h.tvState.setText(stateText);

        if (item.getDate() != null && item.getGameType() != null)
            h.tvDateType.setText( item.getDate() + " " + item.getGameType()+" 경기");

        h.tvTime.setText(item.getTime() != null ? item.getTime() : "-");
        h.tvPlace.setText(item.getPlace() != null ? item.getPlace() : "-");

        h.btnCandidates.setEnabled(true);

        // 클릭 리스너
        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(item, h.getBindingAdapterPosition());
        });
        h.btnCandidates.setOnClickListener(v -> {
            if (candidatesClickListener != null) candidatesClickListener.onCandidatesClick(item, h.getBindingAdapterPosition());
        });
        h.btnMore.setOnClickListener(v -> {
            if (moreClickListener != null) moreClickListener.onMoreClick(h.btnMore, item, h.getBindingAdapterPosition());
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView speechCard;
        TextView tvState, tvDateType, tvTime, tvPlace, tvRefusal;
        View btnCandidates;
        ImageButton btnMore;

        VH(@NonNull View itemView) {
            super(itemView);
            speechCard = itemView.findViewById(R.id.speech_card);
            tvState     = itemView.findViewById(R.id.tv_state);
            tvDateType  = itemView.findViewById(R.id.tv_date_type);
            tvTime      = itemView.findViewById(R.id.tv_time);
            tvPlace     = itemView.findViewById(R.id.tv_place);
            tvRefusal   = itemView.findViewById(R.id.tv_refusal);
            btnCandidates = itemView.findViewById(R.id.btn_candidates);
            btnMore     = itemView.findViewById(R.id.btn_more);
        }
    }
}

