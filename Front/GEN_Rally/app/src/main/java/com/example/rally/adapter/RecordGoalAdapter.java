package com.example.rally.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.dto.GoalActiveItem;

import java.util.List;

public class RecordGoalAdapter extends RecyclerView.Adapter<RecordGoalAdapter.GoalViewHolder>{
    private List<GoalActiveItem> items;
    public RecordGoalAdapter(List<GoalActiveItem> items) {
        this.items = items;
    }
    public void setItems(List<GoalActiveItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal_card, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTitle, tvProgress;
        ProgressBar progressBar;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_goal_type);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(GoalActiveItem item) {
            tvType.setText(item.getTheme());
            tvTitle.setText(item.getName());

            int current = item.getProgressCount();
            int target = item.getTargetWeeksCount();

            // 텍스트 세팅 (7/8)
            tvProgress.setText(current + "/" + target);

            progressBar.setMax(target);
            progressBar.setProgress(current);
        }
    }


    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }
}
