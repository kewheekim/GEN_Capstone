package com.example.rally.adapter;

import com.example.rally.R;
import com.example.rally.dto.NotificationItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotiViewHolder> {

    private final List<NotificationItem> items = new ArrayList<>();

    public void setItems(List<NotificationItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotiViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotiViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotiViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNoti;
        TextView tvTitle;
        TextView tvBody;
        TextView tvTime;

        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNoti = itemView.findViewById(R.id.iv_noti);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvBody = itemView.findViewById(R.id.tv_body);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(NotificationItem item) {
            tvTitle.setText(item.getTitle());
            tvBody.setText(item.getBody());
            tvTime.setText(formatTime(item.getCreatedAt()));
        }

        private String formatTime(String createdAt) {
            if (createdAt == null || createdAt.isEmpty()) return "";

            try {
                String normalized = createdAt;
                int dotIndex = createdAt.indexOf('.');
                if (dotIndex != -1) {
                    String prefix = createdAt.substring(0, dotIndex);
                    String fraction = createdAt.substring(dotIndex + 1);
                    if (fraction.length() > 3) {
                        fraction = fraction.substring(0, 3);
                    }
                    normalized = prefix + "." + fraction;
                }

                java.time.LocalDateTime created =
                        java.time.LocalDateTime.parse(normalized);
                java.time.LocalDateTime now =
                        java.time.LocalDateTime.now(java.time.ZoneId.systemDefault());

                java.time.Duration duration = java.time.Duration.between(created, now);
                long seconds = duration.getSeconds();

                if (seconds < 60) {
                    return "방금 전";
                }

                long minutes = seconds / 60;
                if (minutes < 60) {
                    return minutes + "분 전";
                }

                long hours = minutes / 60;
                if (hours < 24) {
                    return hours + "시간 전";
                }

                long days = hours / 24;
                return days + "일 전";

            } catch (Exception e) {
                return "";
            }
        }
    }
}
