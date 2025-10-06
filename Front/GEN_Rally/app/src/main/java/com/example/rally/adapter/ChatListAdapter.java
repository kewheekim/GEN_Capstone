package com.example.rally.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rally.R;
import com.example.rally.dto.ChatRoomListDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder> {

    private final Context context;
    private List<ChatRoomListDto> items = new ArrayList<>();

    public ChatListAdapter(Context context){
        this.context = context;
    }

    public void setItems(List<ChatRoomListDto> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(ChatRoomListDto room);
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ChatRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View v = inf.inflate(R.layout.item_chatroom, parent, false);
        return new ChatRoomViewHolder(v, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomViewHolder holder, int position) {
        ChatRoomListDto room = items.get(position);
        holder.bind(room, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ChatRoomViewHolder extends RecyclerView.ViewHolder{
        private final Context context;
        TextView tvName, tvGameStyle, tvTime, tvUnread, tvContent;
        ImageView ivProfile;
        View viewUnread;

        public ChatRoomViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;

            tvName = itemView.findViewById(R.id.tv_name);
            tvGameStyle = itemView.findViewById(R.id.tv_game_style);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnread = itemView.findViewById(R.id.tv_unread);
            tvContent = itemView.findViewById(R.id.tv_content);
            ivProfile = itemView.findViewById(R.id.iv_user_profile);
            viewUnread = itemView.findViewById(R.id.view_unread);
        }

        public void bind(ChatRoomListDto room, OnItemClickListener listener) {

            tvName.setText(room.getOpponentName());
            tvGameStyle.setText(room.getGameStyle());
            tvTime.setText(formatTime(room.getLastMessageTime()));
            tvContent.setText(room.getLastMessage());
            Glide.with(context)
                    .load(room.getOpponentProfileImageUrl())
                    .placeholder(R.drawable.ic_user_profile)
                    .into(ivProfile);

            if(room.getUnreadCount()>0){
                tvUnread.setText(String.valueOf(room.getUnreadCount()));
                viewUnread.setVisibility(View.VISIBLE);
            }else{
                tvUnread.setText("");
                viewUnread.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(room);
                }
            });
        }
    }

    private static String formatTime(LocalDateTime timeMillis) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
        String time = timeMillis.format(formatter);
        return time;
    }
}
