package com.example.rally.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.ChatAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.ChatMessageDto;
import com.example.rally.dto.ChatMessageRequest;
import com.example.rally.dto.ChatRoomDto;
import com.example.rally.viewmodel.ChatMessage;
import com.example.rally.viewmodel.ChatViewModel;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

// CHAT_002
public class ChatActivity extends AppCompatActivity {

    private static final String BASE_URL = "ws://10.0.2.2:8080/stomp";
    private static final String SUBSCRIBE_URL = "/sub/dm/";
    private static final String SEND_URL = "/pub/dm/";

    public static final String ROOM_ID = "extra_room_id";
    public static final String MY_USER_ID = "extra_my_user_id";

    private StompClient stompClient;
    private ApiService apiService;

    private Disposable lifecycleDisposable;
    private Disposable topicDisposable;
    private Gson gson = new Gson();
    private RecyclerView rvChat;
    private ChatAdapter adapter;
    private ChatViewModel viewModel;
    private LinearLayoutManager layoutManager;
    private EditText etMessage;
    private ImageButton btnSend;
    private Toolbar toolbar;

    private long roomId = -1L;
    private long myUserId = -1L;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        apiService = RetrofitClient
                .getSecureClient(getApplicationContext(),"http://10.0.2.2:8080/")
                .create(ApiService.class);

        // Intent에서 roomId, myUserId 받아오기 (호출부에서 넣어줘야 함)
        Intent it = getIntent();
        roomId = it.getLongExtra(ROOM_ID, -1L);
        myUserId = it.getLongExtra(MY_USER_ID, -1L);
        if (roomId == -1L || myUserId == -1L) {
            Toast.makeText(this, "채팅 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
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

        // STOMP 연결 및 구독 시작
        connectStompAndSubscribe();
        markChatRoomAsRead(roomId);
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
        loadParticipantProfilesFromServer(roomId);
        loadInitialMessagesFromServer(roomId);
    }

    // 메시지 전송
    private void trySend() {
        String txt = etMessage.getText().toString().trim();
        if (txt.isEmpty()) return;

        Long tempMessageId = System.currentTimeMillis();
        ChatMessage msg = new ChatMessage(
                tempMessageId,
                ChatMessage.VIEW_TYPE_SENT,
                txt,
                System.currentTimeMillis(),
                myUserId,
                null
        );
        adapter.addMessage(msg);
        etMessage.setText("");
        hideKeyboard();
        new Handler().postDelayed(this::scrollToBottom, 50);

        ChatMessageRequest req = new ChatMessageRequest();
        req.setSenderId(myUserId);
        req.setContent(txt);
        String json = gson.toJson(req);

        // 전송
        try {
            stompClient.send(SEND_URL + roomId, json).subscribe(() -> {
                // 전송 성공: 실제 저장/브로드캐스트는 서버에서 처리 -> 서버에서 내려오는 메시지를 받아 sync 함
            }, throwable -> {
                // 전송 실패: 사용자에게 알리고(또는 재시도 큐에 넣기)
                Log.e("ChatActivity", "STOMP send error", throwable);
                runOnUiThread(() -> Toast.makeText(this, "메시지 전송 실패", Toast.LENGTH_SHORT).show());
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "메시지 전송 예외", Toast.LENGTH_SHORT).show();
        }
    }

    // STOMP 연결 및 구독
    private void connectStompAndSubscribe() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BASE_URL);

        lifecycleDisposable = stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("ChatActivity", "STOMP 연결 열림");
                    // 구독 시작
                    subscribeTopic();
                    break;
                case ERROR:
                    Log.e("ChatActivity", "STOMP 에러", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("ChatActivity", "STOMP 연결 닫힘");
                    break;
            }
        });

        stompClient.connect();
    }

    private void subscribeTopic() {
        String topic = SUBSCRIBE_URL + roomId;
        topicDisposable = stompClient.topic(topic).subscribe(topicMessage -> {
            String payload = topicMessage.getPayload();
            try {
                ChatMessageDto dto = gson.fromJson(payload, ChatMessageDto.class);
                runOnUiThread(() -> viewModel.addIncomingMessage(dto, myUserId));
            } catch (Exception e) {
                Log.e("ChatActivity", "payload parsing failed: " + payload, e);
            }
        }, throwable -> {
            Log.e("ChatActivity", "subscription error", throwable);
        });
    }

    // 사용자 정보 캐싱
    private void loadParticipantProfilesFromServer(Long roomId) {
        apiService.getChatRoomInfo(roomId)
                .enqueue(new Callback<List<ChatRoomDto>>() {
                    @Override
                    public void onResponse(Call<List<ChatRoomDto>> call, Response<List<ChatRoomDto>> response) {
                        if(response.isSuccessful() && response.body() != null) {
                            viewModel.cacheParticipantProfiles(response.body());
                        }else{
                            Log.d("failed to load user", "code=" + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ChatRoomDto>> call, Throwable t) {
                        Log.e("loadMessage","onFailure " ,t);
                        Toast.makeText(ChatActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }

    // 이전 메시지 로드
    private void loadInitialMessagesFromServer(Long roomId){
        apiService.loadMessages(roomId)
                .enqueue(new Callback<List<ChatMessageDto>>() {
                    @Override
                    public void onResponse(Call<List<ChatMessageDto>> call, Response<List<ChatMessageDto>> response) {
                        Log.d("loadMessage", "onResponse ✅ code=" + response.code());
                        if(response.isSuccessful() && response.body()!=null){
                            List<ChatMessageDto> dtos = response.body();
                            viewModel.initializeMessages(dtos, myUserId);

                        }else {
                            Log.d("failed to load messages", "code=" + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<ChatMessageDto>> call, Throwable t) {
                        Log.e("loadMessage","onFailure " ,t);
                        Toast.makeText(ChatActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }

    // 읽음 처리
    private void markChatRoomAsRead(Long roomId) {
        apiService.markChatRoomAsRead(roomId)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d("ChatActivity", "읽음 처리 성공!");
                        } else {
                            Log.e("ChatActivity", "읽음 처리 실패: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("ChatActivity", "읽음 처리 네트워크 오류", t);
                    }
                });
    }
    private void scrollToBottom() {
        int count = adapter.getItemCount();
        if (count > 0) {
            rvChat.scrollToPosition(count - 1);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        android.view.View focus = getCurrentFocus();
        if (imm != null) {
            if (focus != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            } else if (etMessage != null) {
                imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lifecycleDisposable != null && !lifecycleDisposable.isDisposed()) {
            lifecycleDisposable.dispose();
        }
        if (topicDisposable != null && !topicDisposable.isDisposed()) {
            topicDisposable.dispose();
        }
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}
