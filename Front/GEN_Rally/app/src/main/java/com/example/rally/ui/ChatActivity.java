package com.example.rally.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.ChatAdapter;
import com.example.rally.dto.ChatRoomDto;
import com.example.rally.viewmodel.ChatMessage;
import com.example.rally.viewmodel.ChatViewModel;

import java.util.ArrayList;
import java.util.List;

// CHAT_002
public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private ChatViewModel viewModel;
    private LinearLayoutManager layoutManager;
    private EditText etMessage;
    private ImageButton btnSend;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        findViewByIdViews();

        adapter = new ChatAdapter(this, viewModel );
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);

        setupKeyboardListener();

        btnSend.setOnClickListener(v -> trySend());
        observeViewModel();
        loadChatData();

    }

    private void findViewByIdViews() {
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
    }

    private void setupKeyboardListener() {
        final View root = findViewById(android.R.id.content);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int lastHeight = 0;
            @Override
            public void onGlobalLayout() {
                int height = root.getRootView().getHeight() - root.getHeight();
                // 키보드가 열렸다고 판단 (임계값 조정 가능)
                if (height > 200 && lastHeight != height) {
                    scrollToBottom();
                }
                lastHeight = height;
            }
        });
    }

    //  LiveData 관찰 로직
    private void observeViewModel() {
        viewModel.getMessages().observe(this, messages -> {
            // ViewModel에서 날짜 라벨까지 처리된 최종 리스트를 받아서 Adapter에 제출
            adapter.submitList(messages);
            // 새 메시지가 추가되면 맨 아래로 스크롤
            scrollToBottom();
        });
    }

    // 초기 데이터 로드 및 프로필 캐싱
    private void loadChatData() {
        // TODO: 채팅방 입장 API 호출 (프로필 캐싱 데이터 확보)

        List<ChatRoomDto> participants = new ArrayList<>();

        viewModel.cacheParticipantProfiles(participants);

        // TODO: 이전 메시지 목록 로드 API 호출
        // loadInitialMessagesFromServer(ROOM_ID);

    }

    // 메시지 전송
    private void trySend() {
        String txt = etMessage.getText().toString().trim();
        if (txt.isEmpty()) return;

        ChatMessage msg = new ChatMessage(ChatMessage.VIEW_TYPE_SENT, txt, System.currentTimeMillis(), null);
        adapter.addMessage(msg);
        etMessage.setText("");
        hideKeyboard();
        // 바로 맨 아래로 스크롤
        new Handler().postDelayed(this::scrollToBottom, 50);
        // TODO: 실제 서버 전송 로직 추가
    }

    private void scrollToBottom() {
        int count = adapter.getItemCount();
        if (count > 0) {
            rvChat.scrollToPosition(count - 1);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
