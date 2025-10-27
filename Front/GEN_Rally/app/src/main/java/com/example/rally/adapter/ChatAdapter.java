package com.example.rally.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.dto.ChatRoomDto;
import com.example.rally.viewmodel.ChatMessage;
import com.example.rally.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final Context context;
    private final Long currentUserId;
    private List<ChatMessage> items = new ArrayList<>();
    private final int maxBubbleWidthPx;
    private final ChatViewModel viewModel;
    public interface OnCardClickListener {
        void onCardClick(ChatMessage message);
    }

    // 필드 추가
    private final OnCardClickListener cardClickListener;

    public ChatAdapter(Context context, Long currentUserId, ChatViewModel viewModel, OnCardClickListener listener) {
        this.context = context;
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.maxBubbleWidthPx = (int) (screenWidth * 0.63f);
        this.viewModel = viewModel;
        this.cardClickListener = listener;
        this.currentUserId = currentUserId;
    }
    @Override
    public int getItemViewType(int position) {
        return items.get(position).getFinalViewType(currentUserId);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == ChatMessage.VIEW_TYPE_SENT) {
            View v = inf.inflate(R.layout.item_chat_me, parent, false);
            return new SentVH(v);
        } else if (viewType == ChatMessage.VIEW_TYPE_RECEIVED) {
            View v = inf.inflate(R.layout.item_chat_you, parent, false);
            return new ReceivedVH(v);
        } else if(viewType== ChatMessage.VIEW_TYPE_DATE) { // 날짜 구분
            View v = inf.inflate(R.layout.item_chat_date, parent, false);
            return new DateVH(v);

        } else if (viewType == ChatMessage.VIEW_TYPE_MATCH_SENT) { // 내가 보낸 카드
            View v = inf.inflate(R.layout.item_chat_card_me, parent, false);
            return new CardSentVH(v);
        }else if (viewType == ChatMessage.VIEW_TYPE_MATCH_RECEIVED_CREATED) {
            View v = inf.inflate(R.layout.item_chat_card_you_make, parent, false);
            return new CardReceivedCreatedVH(v);
        } else if (viewType == ChatMessage.VIEW_TYPE_MATCH_RECEIVED_CONFIRMED) {
            View v = inf.inflate(R.layout.item_chat_card_you_confirm, parent, false);
            return new CardReceivedConfirmedVH(v);
        }  else {
            View v = inf.inflate(R.layout.item_chat_card_you_confirm, parent, false);
            return new CardReceivedConfirmedVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage m = items.get(position);
        if (holder instanceof SentVH) {
            SentVH vh = (SentVH) holder;
            vh.tvSentBubble.setText(m.getContent());
            vh.tvSentBubble.setMaxWidth(maxBubbleWidthPx);
            vh.tvSentTime.setText(m.getFormattedTime());

        } else if (holder instanceof ReceivedVH) {
            ReceivedVH vh = (ReceivedVH) holder;
            vh.tvReceivedBubble.setText(m.getContent());
            vh.tvReceivedBubble.setMaxWidth(maxBubbleWidthPx);
            vh.tvReceivedTime.setText(m.getFormattedTime());

            ChatRoomDto profile = viewModel.getProfile(m.getSenderId());
            if (profile != null) {
                Glide.with(context)
                        .load(profile.getOpponentProfileUrl())
                        .into(vh.ivProfile);
                vh.tvNickname.setText(profile.getOpponentName());
            }

        } else if (holder instanceof DateVH) {
            DateVH vh = (DateVH) holder;
            vh.tvDate.setText(m.getContent());
        } else if (holder instanceof CardSentVH) {
            ((CardSentVH) holder).bind(m, cardClickListener);
        } else if (holder instanceof CardReceivedCreatedVH) {
            ((CardReceivedCreatedVH) holder).bind(context, m, viewModel, cardClickListener);
        } else if (holder instanceof CardReceivedConfirmedVH) {
            ((CardReceivedConfirmedVH) holder).bind(context, m, viewModel, cardClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addMessage(ChatMessage newMsg) {
        List<ChatMessage> newList = new ArrayList<>(items);
        newList.add(newMsg);
        submitList(newList);
    }

    // 완전한 새 리스트 제출 (DiffUtil 사용)
    public void submitList(List<ChatMessage> newList) {
        ChatDiffCallback diffCallback = new ChatDiffCallback(this.items, newList);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
        this.items = new ArrayList<>(newList);
        diffResult.dispatchUpdatesTo(this);
        TextView tvBubble;
        TextView tvTime;
        ImageView ivProfile;
    }

    // 아래로 유형별 뷰홀더
    static class SentVH extends RecyclerView.ViewHolder {
        TextView tvSentBubble;
        TextView tvSentTime;
        SentVH(@NonNull View itemView) {
            super(itemView);
            tvSentBubble = itemView.findViewById(R.id.tv_bubble_me);
            tvSentTime = itemView.findViewById(R.id.tv_sent_time);
        }
    }

    static class ReceivedVH extends RecyclerView.ViewHolder {
        TextView tvReceivedBubble;
        TextView tvReceivedTime;
        ImageView ivProfile;
        TextView tvNickname;
        ReceivedVH(@NonNull View itemView) {
            super(itemView);
            tvReceivedBubble = itemView.findViewById(R.id.tv_bubble_you);
            tvReceivedTime = itemView.findViewById(R.id.tv_received_time);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
        }
    }

    // 채팅방 내 날짜 구분 라벨
    static class DateVH extends RecyclerView.ViewHolder {
        TextView tvDate;
        DateVH(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }

    static class CardSentVH extends RecyclerView.ViewHolder {
        TextView tvCardTitle;
        TextView tvDate;
        TextView tvTime;
        TextView tvPlace;
        TextView tvSentTime;

        CardSentVH(@NonNull View itemView) {
            super(itemView);
            tvCardTitle = itemView.findViewById(R.id.tv_card_title);
            tvDate = itemView.findViewById(R.id.tv_card_date);
            tvTime = itemView.findViewById(R.id.tv_card_time);
            tvPlace = itemView.findViewById(R.id.tv_card_place);
            tvSentTime = itemView.findViewById(R.id.tv_sent_time);
        }
        public void bind(ChatMessage m, ChatAdapter.OnCardClickListener cardClickListener) {
            ChatMessage.MatchInfo info = m.getMatchInfo();
            if (info != null) {
                // 상태에 따라 타이틀 텍스트만 변경 (버튼은 XML에서 제거)
                if ("CONFIRMED".equals(info.status)) {
                    tvCardTitle.setText("경기 약속을 확정했습니다.");
                } else {
                    tvCardTitle.setText("경기 약속을 만들었습니다."); // CREATED 상태
                }

                tvDate.setText(info.dateText);
                tvTime.setText(info.timeText);
                tvPlace.setText(info.place);
                tvSentTime.setText(m.getFormattedTime());

                // 상세 페이지 이동 등의 로직만 bind (버튼 클릭 X)
                itemView.setOnClickListener(v -> {
                    if (cardClickListener != null) {
                        cardClickListener.onCardClick(m);
                    }
                });
            }
        }
    }

    // 약속 잡기 카드: 남이 보냄 (버튼 O)
    static class CardReceivedCreatedVH extends RecyclerView.ViewHolder {
        TextView tvCardTitle;
        TextView tvDate;
        TextView tvTime;
        TextView tvPlace;
        TextView tvReceivedTime;
        ImageView ivProfile;
        TextView tvNickname;
        Button tvConfirmButton; // 버튼이 레이아웃에 있다면 추가

        CardReceivedCreatedVH(@NonNull View itemView) {
            super(itemView);
            tvCardTitle = itemView.findViewById(R.id.tv_card_title);
            tvDate = itemView.findViewById(R.id.tv_card_date);
            tvTime = itemView.findViewById(R.id.tv_card_time);
            tvPlace = itemView.findViewById(R.id.tv_card_place);
            tvReceivedTime = itemView.findViewById(R.id.tv_received_time);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvConfirmButton = itemView.findViewById(R.id.btn_match_confirm);
        }

        public void bind(Context context, ChatMessage m, ChatViewModel viewModel, ChatAdapter.OnCardClickListener cardClickListener) {
            ChatMessage.MatchInfo info = m.getMatchInfo();

            if (info != null) {
                tvCardTitle.setText("경기 약속을 만들었습니다.");
                tvDate.setText(info.dateText);
                tvTime.setText(info.timeText);
                tvPlace.setText(info.place);
                tvReceivedTime.setText(m.getFormattedTime());

                // 프로필 정보 바인딩
                ChatRoomDto profile = viewModel.getProfile(m.getSenderId());
                if (profile != null) {
                    Glide.with(context)
                            .load(profile.getOpponentProfileUrl())
                            .into(ivProfile);
                    tvNickname.setText(profile.getOpponentName());
                }

                // 확정 가능 카드는 클릭 시 확정 로직이나 상세 페이지 이동 로직이 수행되어야 합니다.
                itemView.setOnClickListener(v -> {
                    if (cardClickListener != null) {
                        // ChatActivity의 onCardClick 메서드가 호출되어 확정 로직을 실행합니다.
                        cardClickListener.onCardClick(m);
                    }
                });
            }
        }
    }

    // 경기 확정 카드: 남이 보냄 (버튼 X)
    static class CardReceivedConfirmedVH extends RecyclerView.ViewHolder {
        TextView tvCardTitle;
        TextView tvDate;
        TextView tvTime;
        TextView tvPlace;
        TextView tvReceivedTime;
        ImageView ivProfile;
        TextView tvNickname;

        CardReceivedConfirmedVH(@NonNull View itemView) {
            super(itemView);
            tvCardTitle = itemView.findViewById(R.id.tv_card_title);
            tvDate = itemView.findViewById(R.id.tv_card_date);
            tvTime = itemView.findViewById(R.id.tv_card_time);
            tvPlace = itemView.findViewById(R.id.tv_card_place);
            tvReceivedTime = itemView.findViewById(R.id.tv_received_time);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
        }

        public void bind(Context context, ChatMessage m, ChatViewModel viewModel, ChatAdapter.OnCardClickListener cardClickListener) {
            ChatMessage.MatchInfo info = m.getMatchInfo();

            if (info != null) {
                tvCardTitle.setText("경기 약속을 확정했습니다.");
                tvDate.setText(info.dateText);
                tvTime.setText(info.timeText);
                tvPlace.setText(info.place);
                tvReceivedTime.setText(m.getFormattedTime());

                ChatRoomDto profile = viewModel.getProfile(m.getSenderId());
                if (profile != null) {
                    Glide.with(context)
                            .load(profile.getOpponentProfileUrl())
                            .into(ivProfile);
                    tvNickname.setText(profile.getOpponentName());
                }
            }
        }
    }


    // DiffUtil
    static class ChatDiffCallback extends DiffUtil.Callback {
        private final List<ChatMessage> oldList;
        private final List<ChatMessage> newList;

        ChatDiffCallback(List<ChatMessage> oldList, List<ChatMessage> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ChatMessage o = oldList.get(oldItemPosition);
            ChatMessage n = newList.get(newItemPosition);
            // 단순 비교: 같은 타입 + 같은 timestamp + 같은 text 라면 같은 항목으로 간주
            return o.getMessageId() != null && n.getMessageId() != null
                    ? o.getMessageId().equals(n.getMessageId())
                    : (o.getViewType() == n.getViewType() && o.getTimestamp() == n.getTimestamp());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            // 간단하게 같은지 비교
            ChatMessage o = oldList.get(oldItemPosition);
            ChatMessage n = newList.get(newItemPosition);
            return areItemsTheSame(oldItemPosition, newItemPosition);
        }
    }
}
