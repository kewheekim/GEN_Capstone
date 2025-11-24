package com.example.rally.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.dto.GoalActiveItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class GoalCheckAdapter extends RecyclerView.Adapter<GoalCheckAdapter.GoalViewHolder> {

    public interface OnAchieveCheckedChangeListener {
        void onAchieveCheckedChanged(GoalActiveItem item, int position, boolean isChecked);
    }

    private final List<GoalActiveItem> items = new ArrayList<>();
    private final OnAchieveCheckedChangeListener listener;

    public GoalCheckAdapter(List<GoalActiveItem> initialItems, OnAchieveCheckedChangeListener listener) {
        if (initialItems != null) {
            items.addAll(initialItems);
        }
        this.listener = listener;
    }

    public void setItems(List<GoalActiveItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_check, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalActiveItem item = items.get(position);
        holder.bind(item, position ==0);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<GoalActiveItem> getItems() {
        return items;
    }

    public List<GoalActiveItem> getAchievedItems() {
        List<GoalActiveItem> result = new ArrayList<>();
        for (GoalActiveItem item : items) {
            if (item.isAchieved()) {
                result.add(item);
            }
        }
        return result;
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        View divider;
        MaterialCardView cvTarget;
        MaterialCardView cvTheme;
        TextView tvTarget;
        TextView tvTheme;
        TextView tvGoal;
        AppCompatCheckBox cbAchieve;

        GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            divider = itemView.findViewById(R.id.divider);
            cvTarget = itemView.findViewById(R.id.cv_target);
            tvTarget = itemView.findViewById(R.id.tv_target);
            cvTheme = itemView.findViewById(R.id.cv_theme);
            tvTheme = itemView.findViewById(R.id.tv_theme);
            tvGoal = itemView.findViewById(R.id.tv_goal);
            cbAchieve = itemView.findViewById(R.id.cb_achieve);
        }

        void bind(GoalActiveItem item, boolean isFirst) {
            divider.setVisibility(isFirst ? View.GONE : View.VISIBLE);
            String targetText;
            if ("기간".equals(item.getType())) {
                targetText= item.getTargetWeeksCount() + "주";
            } else {
                targetText= item.getTargetWeeksCount() + "회";
            }
            tvTarget.setText(targetText);
            tvTheme.setText(item.getTheme());

            String theme = item.getTheme();
            int bgColor;
            int textColor;
            if ("건강 관리".equals(theme)) {
                bgColor = ContextCompat.getColor(itemView.getContext(), R.color.blue_light);
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.blue);
            } else if ("실력증진".equals(theme) || "실력 증진".equals(theme) || "대인관계".equals(theme)) {
                bgColor = ContextCompat.getColor(itemView.getContext(), R.color.orange_light);
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.orange);
            } else {
                bgColor = ContextCompat.getColor(itemView.getContext(), R.color.blue_light);
                textColor = ContextCompat.getColor(itemView.getContext(), R.color.blue);
            }

            cvTarget.setCardBackgroundColor(bgColor);
            cvTheme.setCardBackgroundColor(bgColor);
            tvTarget.setTextColor(textColor);
            tvTheme.setTextColor(textColor);
            tvGoal.setText(item.getName());
            cbAchieve.setOnCheckedChangeListener(null);
            cbAchieve.setChecked(item.isAchieved());

            cbAchieve.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setAchieved(isChecked);
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onAchieveCheckedChanged(item, position, isChecked);
                    }
                }
            });
        }
    }
}
