package com.example.rally.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.rally.R;
import com.example.rally.dto.InvitationItem;
import com.google.android.material.card.MaterialCardView;

public class InvitationAdapter extends ListAdapter<InvitationItem, InvitationAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull InvitationItem item);
        void onMoreClick(@NonNull InvitationItem item);
        void onConfirmClick(@NonNull InvitationItem item); // “요청내용 확인하기” 등
    }

    private final boolean isSent;          // true: 보낸 요청 / false: 받은 요청
    private final int itemLayoutRes;
    @NonNull private final OnItemClickListener listener;

    public InvitationAdapter(boolean isSent,
                             @LayoutRes int itemLayoutRes,
                             @NonNull OnItemClickListener listener) {
        super(new InvitationDiff());
        this.isSent = isSent;
        this.itemLayoutRes = itemLayoutRes;
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        Long id = getItem(position).getInvitationId();
        return (id == null) ? RecyclerView.NO_ID : id;
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView speechCard;
        final TextView tvState,  tvDateType, tvRequest, tvRefusal;
        final View btnConfirm, divider;
        final ImageView ivProfile;
        final ImageButton btnMore;

        VH(@NonNull View itemView) {
            super(itemView);
            speechCard = itemView.findViewById(R.id.speech_card);
            tvState    = itemView.findViewById(R.id.tv_state);
            tvDateType = itemView.findViewById(R.id.tv_date_type);
            tvRequest  = itemView.findViewById(R.id.tv_request);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            tvRefusal = itemView.findViewById(R.id.tv_refusal);
            btnMore    = itemView.findViewById(R.id.btn_more);
            divider = itemView.findViewById(R.id.divider);
        }

        void bind(@NonNull InvitationItem item, boolean isSent, @NonNull OnItemClickListener listener) {
            Context ctx = speechCard.getContext();
            String stateRaw = item.getState();
            String stateText = stateRaw;
            if (stateText == null) {
                stateText = "대기";
            } else {
                if(isSent) {
                    switch (stateText) {
                        case "요청중":
                            stateText = "요청완료";
                            speechCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.gray100));
                            speechCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.gray_nav_bar));
                            tvState.setTextColor(ContextCompat.getColor(ctx, R.color.gray_text));
                            break;
                        case "수락":
                            stateText = "상대수락";
                            speechCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.green_select));
                            speechCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.green_active));
                            tvState.setTextColor(ContextCompat.getColor(ctx, R.color.green_active));
                            break;
                        case "거절":
                            stateText = "상대거절"; break;
                        default:
                            stateText = stateText; break;
                    }
                }
                else {
                    switch (stateText) {
                        case "요청중":
                            stateText = "수락 대기중";
                            speechCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.gray100));
                            speechCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.gray_nav_bar));
                            tvState.setTextColor(ContextCompat.getColor(ctx, R.color.gray_text));
                            break;
                        case "수락":
                            stateText = "수락 완료";
                            speechCard.setCardBackgroundColor(ContextCompat.getColor(ctx, R.color.green_select));
                            speechCard.setStrokeColor(ContextCompat.getColor(ctx, R.color.green_active));
                            tvState.setTextColor(ContextCompat.getColor(ctx, R.color.green_active));
                            break;
                        case "거절":      stateText = "거절 완료"; break;
                        default:                    stateText = stateText; break;
                    }
                    // 프로필 이미지 로딩
                    Glide.with(itemView.getContext())
                            .load(item.getOpponentProfileImage())
                            .placeholder(R.drawable.ic_default_profile)
                            .apply(new RequestOptions()
                                    .transform(new RoundedCorners((int) ( 24 * itemView.getResources().getDisplayMetrics().density)))
                                    .placeholder(R.drawable.ic_default_profile))
                            .into(ivProfile);
                }
                if (tvState != null)
                    tvState.setText(stateText);
            }

            if (tvDateType != null) {
                String date = item.getDate();
                String gameType = item.getGameType();
                tvDateType.setText(date + " " + gameType + " 경기");
            }

            if (tvRequest != null) {
                String opponentName = item.getOpponentName();
                tvRequest.setText(opponentName + "님");
                if (isSent) {
                    tvRequest.setText( opponentName + "님에게 매칭을 요청했어요!");
                } else {
                    tvRequest.setText(opponentName + "님이 매칭을 요청했어요!");
                }
            }

            // 거절 상태인 경우
            boolean isRejected = "거절".equals(stateRaw);
            String reason = item.getRefusal();
            boolean showReason = isRejected && reason != null && !reason.trim().isEmpty();

            if (tvRefusal != null) {
                if (showReason) {
                    tvRefusal.setText("거절사유: " + reason);
                    tvRefusal.setVisibility(View.VISIBLE);
                } else {
                    tvRefusal.setText(null);
                    tvRefusal.setVisibility(View.GONE);
                }
            }
            if (divider != null) {
                divider.setVisibility(showReason ? View.VISIBLE : View.GONE);
            }

            // 클릭 이벤트
            itemView.setOnClickListener(v -> listener.onItemClick(item));
            if (btnMore != null) btnMore.setOnClickListener(v -> listener.onMoreClick(item));
            if (btnConfirm != null) btnConfirm.setOnClickListener(v -> listener.onConfirmClick(item));
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(itemLayoutRes, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getItem(position), isSent, listener);
    }
}
