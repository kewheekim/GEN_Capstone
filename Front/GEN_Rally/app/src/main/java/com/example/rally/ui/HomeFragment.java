package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.GameCardAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.auth.TokenStore;
import com.example.rally.dto.GameCardInfoDto;
import com.example.rally.wear.PhoneDataLayerClient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// HOM_001
public class HomeFragment extends Fragment implements GameCardAdapter.OnChatButtonClickListener {
    private RecyclerView rvGames;
    private ImageView ivGameNull;
    private  ImageButton btnRequest, btnChat, btnNoti;
    private GameCardAdapter gameAdapter;
    private TokenStore tokenStore;
    private long currentUserId = -1L;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvGames = view.findViewById(R.id.rv_games);
        ivGameNull = view.findViewById(R.id.iv_gamenull);

        // 매칭 버튼
        btnRequest = view.findViewById(R.id.btn_request);
        btnRequest.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MatTypeActivity.class);
            startActivity(intent);
        });

        // 채팅 버튼
        btnChat = view.findViewById(R.id.btn_chat);
        btnChat.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(),ChatListActivity.class);
            startActivity(intent);
        });

        // 알림 버튼
        btnNoti = view.findViewById(R.id.btn_noti);
        btnNoti.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NotificationActivity.class);
            startActivity(intent);
        });

        // 뒤로가기 막기
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // 동작x
                    }
                }
        );

        try {
            tokenStore = new TokenStore(requireContext());

            currentUserId = tokenStore.getUserId();

            if (currentUserId == -1L) {
                startActivity(new Intent(requireActivity(), AuthActivity.class));
                requireActivity().finish();
                return view;
            }
        } catch (GeneralSecurityException | IOException e) {
            Log.e("HomeFragment", "TokenStore 초기화 실패", e);
            requireActivity().finish();
            return view;
        }

        setupRecyclerViews();
        loadHomeData();

        return view;
    }

    private void setupRecyclerViews() {
        gameAdapter = new GameCardAdapter();
        gameAdapter.setOnChatButtonClickListener(this);
        gameAdapter.setOnRecordStartClickListener(game -> {
            if (getContext() == null) return;

            long gameId = game.getGameId();
            String myName = game.getMyName();
            String opponentName = game.getOpponentName();
            String opponentProfile = game.getOpponentProfileUrl();
            boolean localIsUser1 = game.isUser1();

            // 워치 연결 상태 확인
            com.google.android.gms.wearable.Wearable.getNodeClient(requireContext())
                    .getConnectedNodes()
                    .addOnSuccessListener(nodes -> {
                        StringBuilder sb = new StringBuilder("connected nodes: ");
                        for (com.google.android.gms.wearable.Node n : nodes) {
                            sb.append(n.getDisplayName()).append("(").append(n.getId()).append(") ");
                        }
                        android.util.Log.d("PhoneDL", sb.toString());
                        android.widget.Toast.makeText(requireContext(), nodes.isEmpty() ? "연결된 워치 없음" : sb.toString(), android.widget.
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            android.util.Log.e("PhoneDL", "getConnectedNodes failed", e)
                    );

            // 워치로 게임 세팅 전송
            PhoneDataLayerClient.sendGameSetup(
                    requireContext(),
                    gameId,
                    opponentName,
                    myName,
                    localIsUser1,
                    new PhoneDataLayerClient.SendCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            requireContext(),
                                            "워치로 전송 완료",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }

                        @Override
                        public void onNoNode() {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            requireContext(),
                                            "연결된 워치 없음",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }

                        @Override
                        public void onError(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            requireContext(),
                                            "전송 실패: " + e.getMessage(),
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }
                    }
            );

            // 경기 모니터링 화면으로 전환
            Intent intent = new Intent(requireContext(), ScoreMonitorActivity.class)
                    .putExtra("gameId", gameId)
                    .putExtra("opponentName", opponentName)
                    .putExtra("opponentProfile", opponentProfile)
                    .putExtra("userName", myName)
                    .putExtra("localIsUser1", localIsUser1);

            startActivity(intent);
        });
        rvGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvGames.setAdapter(gameAdapter);
    }

    private void loadHomeData() {
        ApiService apiService = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

        apiService.getHome()
                .enqueue(new Callback<List<GameCardInfoDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<GameCardInfoDto>> call, @NonNull Response<List<GameCardInfoDto>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Log.d("HomeFragment", "홈 데이터 로드 성공: " + response.body().size() + "개");
                        rvGames.setVisibility(View.VISIBLE);
                        ivGameNull.setVisibility(View.GONE);
                        gameAdapter.setGameList(response.body());
                    } else {
                        Log.d("HomeFragment", "홈 데이터 없음");
                        rvGames.setVisibility(View.GONE);
                        ivGameNull.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<GameCardInfoDto>> call, @NonNull Throwable t) {
                    Log.e("HomeFragment", "홈 데이터 로드 실패", t);
                    rvGames.setVisibility(View.GONE);
                    ivGameNull.setVisibility(View.VISIBLE);
                }
        });
    }

    @Override
    public void onChatButtonClick(long roomId) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.ROOM_ID, roomId);
        intent.putExtra(ChatActivity.MY_USER_ID, currentUserId); // Fragment에서 안전하게 가져온 ID 사용

        startActivity(intent);
    }

}
