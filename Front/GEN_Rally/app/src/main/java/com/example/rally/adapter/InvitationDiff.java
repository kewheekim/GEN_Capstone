package com.example.rally.adapter;

import androidx.recyclerview.widget.DiffUtil;

import com.example.rally.dto.InvitationItem;

import lombok.NonNull;

public class InvitationDiff extends DiffUtil.ItemCallback<InvitationItem> {
    @Override
    public boolean areItemsTheSame(@NonNull InvitationItem oldItem,
                                   @NonNull InvitationItem newItem) {
        Long a = oldItem.getInvitationId();
        Long b = newItem.getInvitationId();
        return (a == b) || (a != null && a.equals(b));
    }

    @Override
    public boolean areContentsTheSame(@NonNull InvitationItem oldItem,
                                      @NonNull InvitationItem newItem) {
        return eq(oldItem.getState(), newItem.getState())
                && eq(oldItem.getRefusal(), newItem.getRefusal())
                && eq(oldItem.getOpponentId(), newItem.getOpponentId())
                && eq(oldItem.getOpponentName(), newItem.getOpponentName())
                && eq(oldItem.getOpponentProfileImage(), newItem.getOpponentProfileImage())
                && eq(oldItem.getMyRequestId(), newItem.getMyRequestId())
                && eq(oldItem.getOpponentRequestId(), newItem.getOpponentRequestId());
    }

    private static boolean eq(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
