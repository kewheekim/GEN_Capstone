package com.example.rally.adapter;

import com.example.rally.dto.MatchSeekingItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

public class MatchSeekingDiff extends DiffUtil.ItemCallback<MatchSeekingItem> {
    @Override
    public boolean areItemsTheSame(@NonNull MatchSeekingItem oldItem, @NonNull MatchSeekingItem newItem) {
        if (oldItem.getRequestId() == null || newItem.getRequestId() == null) return false;
        return oldItem.getRequestId().equals(newItem.getRequestId());
    }

    @Override
    public boolean areContentsTheSame(@NonNull MatchSeekingItem oldItem, @NonNull MatchSeekingItem newItem) {
        return safeEq(oldItem.getDate(), newItem.getDate())
                && safeEq(oldItem.getGameType(), newItem.getGameType())
                && safeEq(oldItem.getTime(), newItem.getTime())
                && safeEq(oldItem.getPlace(), newItem.getPlace())
                && safeEq(oldItem.getState(), newItem.getState())
                && (oldItem.getCreatedAt() == null ? newItem.getCreatedAt() == null
                : oldItem.getCreatedAt().equals(newItem.getCreatedAt()));
    }

    private boolean safeEq(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
}

