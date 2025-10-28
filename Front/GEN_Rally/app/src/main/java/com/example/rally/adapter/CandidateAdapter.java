package com.example.rally.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.dto.CandidateItem;
import com.example.rally.dto.CandidateResponseDto;
import com.example.rally.ui.PopupCandidateDetailActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Map;

public class CandidateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_TIER_HEADER = 0;
    private static final int TYPE_USER_CARD = 1;

    private final List<CandidateItem> items;

    private final Map<Integer, Integer> tierHeaderMap;
    private final int firstTierHeader;   // 구분선 표시용
    private final long requestId;

    public CandidateAdapter(List<CandidateItem> items, Map<Integer, Integer> tierHeaderMap, long requestId) {
        this.items = items;
        this.tierHeaderMap = tierHeaderMap;
        this.requestId = requestId;

        int first = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof CandidateItem.TierHeader) {
                first = i;
                break;
            }
        }
        this.firstTierHeader = first;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof CandidateItem.TierHeader) return TYPE_TIER_HEADER;
        else return TYPE_USER_CARD;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_TIER_HEADER) {
            View view = inflater.inflate(R.layout.item_tier, parent, false);
            return new TierHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_candidate, parent, false);
            return new UserCardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CandidateItem item = items.get(position);

        if (holder instanceof TierHeaderViewHolder) {
            int tier = ((CandidateItem.TierHeader) item).tier;
            int tierRelation = tierHeaderMap.getOrDefault(tier, -1);
            ((TierHeaderViewHolder) holder).bind(tier, tierRelation, position == firstTierHeader);
        }
        else {
            CandidateResponseDto user = ((CandidateItem.UserCard) item).user;
            ((UserCardViewHolder) holder).bind(user, requestId);
        }
    }


    @Override
    public int getItemCount() {
        return items.size();
    }
    static class TierHeaderViewHolder extends RecyclerView.ViewHolder {
        ImageView tierImage;
        MaterialCardView speechCard;
        TextView tvCardText;
        View divider;

        TierHeaderViewHolder(View itemView) {
            super(itemView);
            tierImage = itemView.findViewById(R.id.tier_image);
            speechCard = itemView.findViewById(R.id.speech_card);
            tvCardText = itemView.findViewById(R.id.tv_card_text);
            divider=itemView.findViewById(R.id.divider);
        }

        void bind(int tier, int relation, boolean isFirstHeader) {
            tierImage.setImageResource(getTierDrawable(tier));
            // 말풍선
            speechCard.setVisibility(View.GONE);
            tvCardText.setText(null);
            if (relation == 1) {
                speechCard.setVisibility(View.VISIBLE);
                tvCardText.setText("나와 같은 티어의 상대예요");
            } else if (relation == 0) {
                speechCard.setVisibility(View.VISIBLE);
                tvCardText.setText("한 단계 위 티어의 상대예요");
            }

            // 구분선
            if (isFirstHeader) {
                divider.setVisibility(View.GONE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }
        }

        private int getTierDrawable(int tier) {
            switch (tier) {
                case 0:
                    //return R.drawble.ic_tier_bronze1_detail;
                case 1:
                    //return R.drawble.ic_tier_bronze2_detail;
                case 2:
                    //return R.drawble.ic_tier_bronze3_detail;
                case 3:
                    return R.drawable.ic_tier_silver1_detail;
                case 4:
                    return R.drawable.ic_tier_silver2_detail;
                case 5:
                    //return R.drawable.ic_tier_silver3_detail;
                case 6:
                    //return R.drawble.ic_tier_gold1_detail;
                case 7:
                    //return R.drawble.ic_tier_gold2_detail;
                case 8:
                    //return R.drawble.ic_tier_gold3_detail;
                case 9:
                    //return R.drawble.ic_tier_platinum1_detail;
                case 10:
                    //return R.drawble.ic_tier_platinum2_detail;
                case 11:
                    //return R.drawble.ic_tier_platinum3_detail;
                default:
                    return R.drawable.ic_singles;
            }
        }
    }

    static class UserCardViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView userName;
        ImageView genderIcon;
        ImageView sameTimeIcon;
        TextView sameTimeText;
        ImageView samePlaceIcon;
        TextView samePlaceText;

        UserCardViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.iv_profile);
            userName = itemView.findViewById(R.id.tv_nickname);
            genderIcon = itemView.findViewById(R.id.iv_gender_icon);
            sameTimeIcon = itemView.findViewById(R.id.iv_time_status);
            sameTimeText = itemView.findViewById(R.id.tv_time_status);
            samePlaceIcon = itemView.findViewById(R.id.iv_location_status);
            samePlaceText = itemView.findViewById(R.id.tv_place_status);
        }

        void bind(CandidateResponseDto user, long requestId) {
            userName.setText(user.getName());

            // 성별 아이콘
            if (user.getGender()==0) {
                // 남성
                genderIcon.setImageResource(R.drawable.ic_gender_male);
            } else {
                // 여성
                genderIcon.setImageResource(R.drawable.ic_gender_female);
            }

            // 프로필 이미지 로딩
            Glide.with(itemView.getContext())
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.ic_default_profile)
                    .circleCrop()
                    .into(profileImage);

            // 프로필 이미지 하드코딩
            if(user.getName().equals("아어려워요"))
                profileImage.setImageResource(R.drawable.profile_image_male);
            else if(user.getName().equals("흠냐링"))
                profileImage.setImageResource(R.drawable.profile_image_female1);
            else if(user.getName().equals("안세영이되"))
                profileImage.setImageResource(R.drawable.profile_image_female2);

            // 시간 상태
            if(user.isSameTime()) {
                sameTimeText.setText("시간이 동일해요");
                sameTimeIcon.setImageResource(R.drawable.ic_circle);
            }
            else {
                sameTimeText.setText("시간이 일부 겹쳐요");
                sameTimeIcon.setImageResource(R.drawable.ic_circlehalf);
            }

            // 위치 상태
            if(user.isSamePlace()) {
                samePlaceText.setText("위치가 동일해요");
                samePlaceIcon.setImageResource(R.drawable.ic_circle);
            }
            else {
                samePlaceText.setText(String.format("%.1fkm 떨어져 있어요", user.getDistance()));
                samePlaceIcon.setImageResource(R.drawable.ic_circlehalf);
            }
            // 클릭 시 상세화면으로 이동
            itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, PopupCandidateDetailActivity.class);
                intent.putExtra("user", user); // Serializable 객체 전달
                intent.putExtra("requestId", requestId);
                context.startActivity(intent);
            });
        }
    }
}