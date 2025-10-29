package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.ChatListAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.ChatRoomListDto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// CHAT_001
public class ChatListActivity extends AppCompatActivity {
    private static final String BASE_URL = "ws://172.19.8.237:8080/stomp";
    private ChatListAdapter adapter;
    private ApiService apiService;
    private RecyclerView chatList;
    private Toolbar toolbar;
    private LinearLayoutManager layoutManager;
    private TokenStore tokenStore; // TokenStore 선언
    private long currentUserId = -1L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        try {
            tokenStore = new TokenStore(getApplicationContext());

            currentUserId = tokenStore.getUserId();

            if (currentUserId == -1L) {
                // 사용자 ID가 없으면 로그인 화면으로 이동 또는 오류 처리
                Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, AuthActivity.class));
                finish();
                return;
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e("ChatListActivity", "TokenStore 초기화 실패", e);
            Toast.makeText(this, "보안 저장소 오류 발생", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        apiService = RetrofitClient
                .getSecureClient(getApplicationContext(),"http://172.19.8.237:8080/")
                .create(ApiService.class);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        chatList = findViewById(R.id.rv_chat_list);
        adapter = new ChatListAdapter(this);

        chatList.setLayoutManager(new LinearLayoutManager(this));
        chatList.setAdapter(adapter);

        adapter.setOnItemClickListener(this::openChatActivity);

        loadChatRoomLists();

    }

    private void openChatActivity(ChatRoomListDto room) {
        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra(ChatActivity.ROOM_ID, room.getRoomId()); // 채팅방 ID
        intent.putExtra(ChatActivity.MY_USER_ID, currentUserId); // 현재 사용자 ID

        startActivity(intent);
    }

    private void loadChatRoomLists(){
        apiService.getAllChatRooms()
                .enqueue(new Callback<List<ChatRoomListDto>>() {
                    @Override
                    public void onResponse(Call<List<ChatRoomListDto>> call, Response<List<ChatRoomListDto>> response) {
                        if(response.isSuccessful() && response.body() != null) {
                            adapter.setItems(response.body());
                        }else{
                            Log.d("failed to load chat rooms", "code=" + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ChatRoomListDto>> call, Throwable t) {
                        Log.e("loadChatRoom","onFailure " ,t);
                        Toast.makeText(ChatListActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
