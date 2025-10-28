package com.example.rally.adapter;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.example.rally.dto.MatchFoundItem;

import java.util.Objects;

public class MatchFoundDiff extends DiffUtil.ItemCallback<MatchFoundItem> {
    @Override
    public boolean areItemsTheSame(@NonNull MatchFoundItem oldItem, @NonNull MatchFoundItem newItem) {
        Long o = oldItem.getGameId(), n = newItem.getGameId();
        return o != null && o.equals(n);
    }
    @Override
    public boolean areContentsTheSame(@NonNull MatchFoundItem oldItem, @NonNull MatchFoundItem newItem) {
        return java.util.Objects.equals(oldItem.getState(), newItem.getState())
                && java.util.Objects.equals(oldItem.getDate(), newItem.getDate())
                && java.util.Objects.equals(oldItem.getGameType(), newItem.getGameType())
                && java.util.Objects.equals(oldItem.getTime(), newItem.getTime())
                && java.util.Objects.equals(oldItem.getPlace(), newItem.getPlace())
                && java.util.Objects.equals(oldItem.getOpponentName(), newItem.getOpponentName())
                && java.util.Objects.equals(oldItem.getOpponentProfile(), newItem.getOpponentProfile());
    }
}
