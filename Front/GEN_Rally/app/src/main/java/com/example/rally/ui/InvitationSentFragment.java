package com.example.rally.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.adapter.InvitationAdapter;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.InvitationItem;
import com.example.rally.dto.MatchRequestDetails;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// MAT_SENT_001
public class InvitationSentFragment extends Fragment {
    private RecyclerView rv;
    private InvitationAdapter adapter;
    private Dialog cancelDialog;
    private Call<List<InvitationItem>> sentCall;
    private Call<MatchRequestDetails> detailCall;

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

                        @Override
                        public void onConfirmClick(@NonNull View anchor, @NonNull InvitationItem item) {
                            Long myRequestId = item.getMyRequestId();
                            Long opponentRequestId = item.getOpponentRequestId();

                            ApiService api = RetrofitClient
                                    .getSecureClient(requireContext(), BuildConfig.API_BASE_URL)
                                    .create(ApiService.class);

                            detailCall = api.getMatchRequestDetails(myRequestId, opponentRequestId);
                            detailCall.enqueue(new Callback<MatchRequestDetails>() {
                                @Override
                                public void onResponse(Call<MatchRequestDetails> call, Response<MatchRequestDetails> resp) {
                                    if (!isAdded()) return;
                                    if (!resp.isSuccessful() || resp.body() == null) {
                                        Toast.makeText(requireContext(), "상세 정보 불러오기 실패: " + resp.code(), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    MatchRequestDetails detail = resp.body();
                                    String detailJson = new Gson().toJson(detail);
                                    Intent intent = new Intent(requireContext(), InvitationDetailsActivity.class);
                                    intent.putExtra("detail_json", detailJson);
                                    intent.putExtra("invitation_state", item.getState());
                                    intent.putExtra("invitation_id", item.getInvitationId());
                                    intent.putExtra("is_sent", true);
                                    startActivity(intent);
                                }

                                @Override
                                public void onFailure(Call<MatchRequestDetails> call, Throwable t) {
                                    if (!isAdded() || call.isCanceled()) return;
                                    Toast.makeText(requireContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onItemClick(@NonNull InvitationItem item) { }

                        @Override
                        public void onMoreClick(@NonNull View anchor, @NonNull InvitationItem item) {
                            PopupMenu menu = new PopupMenu(requireContext(), anchor);
                            menu.getMenuInflater().inflate(R.menu.match_option_menu, menu.getMenu());
                            menu.setOnMenuItemClickListener(mi -> {
                                if (mi.getItemId() == R.id.match_cancel) {
                                    showCancelDialog(item.getInvitationId());
                                    return true;
                                }
                                return false;
                            });
                            menu.show();
                        }
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

    private void showCancelDialog(@NonNull Long invitationId) {
        if(!isAdded()) return;
        if(cancelDialog != null && cancelDialog.isShowing()) return;

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_popup_match_cancel2);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        dialog.setCanceledOnTouchOutside(true);

        ImageButton btnX = dialog.findViewById(R.id.btn_x);
        Button btnBack = dialog.findViewById(R.id.btn_back);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        View.OnClickListener dismiss = v -> dialog.dismiss();
        btnX.setOnClickListener(dismiss);
        btnCancel.setOnClickListener(dismiss);

        btnCancel.setOnClickListener( v -> {
            // todo: 취소 api 호출 (invitationId로 취소)
        });
        cancelDialog = dialog;
        dialog.show();
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