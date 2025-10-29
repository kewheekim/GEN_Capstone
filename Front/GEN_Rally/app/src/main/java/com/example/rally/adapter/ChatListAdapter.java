package com.example.rally.adapter;

import android.content.Context;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder> {

    private final Context context;
    private List<ChatRoomListDto> items = new ArrayList<>();
    private static final DateTimeFormatter SERVER_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true) // 소수점 이하 0~9자리까지 허용
            .toFormatter();

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

            String formatTime = room.getLastMessageTime();

            tvName.setText(room.getOpponentName());
            tvGameStyle.setText(room.getGameStyle());
            tvTime.setText(formatChatListTime(formatTime));
            tvContent.setText(room.getLastMessage());
            Glide.with(context)
                    .load(room.getOpponentProfileImageUrl())
                    .placeholder(R.drawable.ic_default_profile1)
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

        public static String formatChatListTime(String dateTimeString) {
            if (dateTimeString == null || dateTimeString.isEmpty()) {
                return "";
            }

            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, SERVER_DATETIME_FORMATTER);
                long timeMillis = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREA);
                return sdf.format(new Date(timeMillis));

            } catch (Exception e) {
                Log.e("ChatList", "Failed to format list time: " + dateTimeString, e);
                return dateTimeString.split(" ")[0];
            }
        }
    }
}
