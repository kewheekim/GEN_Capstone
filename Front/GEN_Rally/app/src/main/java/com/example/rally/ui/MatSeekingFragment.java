package com.example.rally.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.MatchSeekingAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchSeekingItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatSeekingFragment extends Fragment {
    private RecyclerView rv;
    private MatchSeekingAdapter adapter;
    private Call<List<MatchSeekingItem>> call;

    public MatSeekingFragment() {}

    public static MatSeekingFragment newInstance() {
        return new MatSeekingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_match_seeking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rv = v.findViewById(R.id.rv_seeking);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);

        adapter = new MatchSeekingAdapter(new MatchSeekingAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MatchSeekingItem item, int position) {
                android.widget.Toast.makeText(requireContext(),
                        "찾은 매칭:" + item.getRequestId(), android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        rv.setAdapter(adapter);

        fetchSeekingMatches();
    }

    private void fetchSeekingMatches() {
        ApiService api = RetrofitClient
                .getSecureClient(requireContext(), BuildConfig.API_BASE_URL)
                .create(ApiService.class);
        call = api.getSeekingMatches();
        call.enqueue(new Callback<List<MatchSeekingItem>>() {
            @Override public void onResponse(Call<List<MatchSeekingItem>> call, Response<List<MatchSeekingItem>> resp) {
                if (!isAdded()) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.submitList(resp.body());
                } else {
                    android.widget.Toast.makeText(requireContext(),
                            "불러오기 실패: " + resp.code(), android.widget.Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<MatchSeekingItem>> call, Throwable t) {
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