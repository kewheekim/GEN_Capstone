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
import android.widget.Toast;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.InvitationAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.InvitationItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// MAT_SENT_001
public class InvitationSentFragment extends Fragment {
    private RecyclerView rv;
    private InvitationAdapter adapter;
    private Call<List<InvitationItem>> sentCall;

    public InvitationSentFragment() { }

    public static InvitationSentFragment newInstance() {
        return new InvitationSentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitation_sent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_sent);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);

        if (adapter == null) {
            adapter = new InvitationAdapter(
                    true,
                    R.layout.item_invitation_sent,
                    new InvitationAdapter.OnItemClickListener() {
                        @Override public void onConfirmClick(@NonNull InvitationItem item) {
                            // 보낸 초대 상세 필요 시 구현
                            Toast.makeText(requireContext(),
                                    "상세보기: " + item.getInvitationId(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override public void onItemClick(@NonNull InvitationItem item) { }

                        @Override public void onMoreClick(@NonNull InvitationItem item) { }
                    }
            );
        }
        rv.setAdapter(adapter);

        fetchSentInvitations();
    }

    private void fetchSentInvitations() {
        ApiService api = RetrofitClient
                .getSecureClient(requireContext(), BuildConfig.API_BASE_URL)
                .create(ApiService.class);

        sentCall = api.getSentInvitations();
        sentCall.enqueue(new Callback<List<InvitationItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<InvitationItem>> call,
                                   @NonNull Response<List<InvitationItem>> resp) {
                if (!isAdded()) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    adapter.submitList(resp.body());
                } else {
                    Toast.makeText(requireContext(),
                            "보낸 요청 불러오기 실패: " + resp.code(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<InvitationItem>> call,
                                  @NonNull Throwable t) {
                if (!isAdded()) return;
                if (call.isCanceled()) return;
                Toast.makeText(requireContext(),
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sentCall != null) {
            sentCall.cancel();
            sentCall = null;
        }
        rv = null;
    }
}