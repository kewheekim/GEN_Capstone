package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.example.rally.dto.MatchInfoDto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// HOM_001
public class HomeFragment extends Fragment {
    private RecyclerView rvGames;
    private ImageView ivGameNull;
    private GameCardAdapter gameAdapter;
    private TokenStore tokenStore;
    private long currentUserId = -1L;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        /*try {
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
        }*/

        rvGames = view.findViewById(R.id.rv_games);
        ivGameNull = view.findViewById(R.id.iv_gamenull);

        // 매칭 버튼
        ImageButton requestBtn = view.findViewById(R.id.btn_request);
        requestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MatTypeActivity.class);
            startActivity(intent);
        });

        // 채팅 버튼
        ImageButton chatBtn = view.findViewById(R.id.btn_chat);
        chatBtn.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(),ChatListActivity.class);
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
        setupRecyclerViews();
        loadHomeData();

        return view;
    }

    private void setupRecyclerViews() {
        gameAdapter = new GameCardAdapter();
        rvGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvGames.setAdapter(gameAdapter);
    }

    private void loadHomeData() {
        ApiService apiService = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

        apiService.getHome()
                .enqueue(new Callback<List<MatchInfoDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<MatchInfoDto>> call, @NonNull Response<List<MatchInfoDto>> response) {
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
                public void onFailure(@NonNull Call<List<MatchInfoDto>> call, @NonNull Throwable t) {
                    Log.e("HomeFragment", "홈 데이터 로드 실패", t);
                    rvGames.setVisibility(View.GONE);
                    ivGameNull.setVisibility(View.VISIBLE);
                }
        });
    }
}
