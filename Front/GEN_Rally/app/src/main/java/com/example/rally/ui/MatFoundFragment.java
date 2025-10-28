package com.example.rally.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchFoundItem;
import com.example.rally.adapter.MatchFoundAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatFoundFragment extends Fragment {

    private RecyclerView rv;
    private MatchFoundAdapter adapter;
    private Call<List<MatchFoundItem>> call;

    public MatFoundFragment() {}

    public static MatFoundFragment newInstance() {
        return new MatFoundFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_match_found, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rv = v.findViewById(R.id.rv_found);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);

        adapter = new MatchFoundAdapter(new MatchFoundAdapter.OnItemClickListener() {
            @Override public void onItemClick(@NonNull MatchFoundItem item) {
                android.widget.Toast.makeText(requireContext(),
                        "찾은 매칭:" + item.getGameId(), android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override public void onChatClick(@NonNull MatchFoundItem item) {
                android.widget.Toast.makeText(requireContext(),
                        "채팅 버튼: " + item.getOpponentName(), android.widget.Toast.LENGTH_SHORT).show();
            }
            @Override public void onMoreClick(@NonNull MatchFoundItem item) {
                android.widget.Toast.makeText(requireContext(),
                        "더보기 버튼: " + item.getGameId(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        fetchFoundMatches();
    }

    private void fetchFoundMatches() {
        ApiService api = RetrofitClient
                .getSecureClient(requireContext(), BuildConfig.API_BASE_URL)
                .create(ApiService.class);
        call = api.getFoundMatches();
        call.enqueue(new Callback<List<MatchFoundItem>>() {
            @Override public void onResponse(Call<List<MatchFoundItem>> call, Response<List<MatchFoundItem>> resp) {
                if (!isAdded()) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.submitList(resp.body());
                } else {
                    android.widget.Toast.makeText(requireContext(),
                            "불러오기 실패: " + resp.code(), android.widget.Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<MatchFoundItem>> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
                android.widget.Toast.makeText(requireContext(),
                        "네트워크 오류: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) { call.cancel(); call = null; }
        rv = null;
    }
}