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

import android.util.Log;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.rally.dto.InvitationItem;
import com.example.rally.dto.MatchRequestDetails;
import com.google.gson.Gson;
import java.util.List;

// MAT_REC_001
public class InvitationReceivedFragment extends Fragment {
    private RecyclerView rv;
    private InvitationAdapter adapter;
    private Dialog cancelDialog;
    private Call<List<InvitationItem>> receivedCall;
    private Call<MatchRequestDetails> detailCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitation_received, container, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = view.findViewById(R.id.rv_received);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(true);

        if (adapter == null) {
            adapter = new InvitationAdapter(
                    false,
                    R.layout.item_invitation_received,
                    new InvitationAdapter.OnItemClickListener() {

                        @Override
                        public void onConfirmClick(@NonNull View anchor, @NonNull InvitationItem item) {
                            // 기존 onConfirmClick(InvitationItem item)에서 하던 내용 그대로 사용
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
                        public void onItemClick(@NonNull InvitationItem item) {
                            // 필요 시 클릭 동작 (없으면 비워둬도 OK)
                        }

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
        fetchReceivedInvitations();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (receivedCall != null) { receivedCall.cancel(); receivedCall = null; }
        if (detailCall != null) { detailCall.cancel(); detailCall = null; }
        rv = null;
    }

    private void fetchReceivedInvitations() {
        ApiService api = RetrofitClient.getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);
        receivedCall = api.getReceivedInvitations();
        receivedCall.enqueue(new Callback<List<InvitationItem>>() {
            @Override public void onResponse(Call<List<InvitationItem>> call, Response<List<InvitationItem>> resp) {
                if (!isAdded()) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    List<InvitationItem> list = resp.body();
                    Log.d("받은 요청: ", "" +list.size());
                    adapter.submitList(resp.body());
                } else {
                    Toast.makeText(requireContext(),"불러오기 실패: " + resp.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<List<InvitationItem>> call, Throwable t) {
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(requireContext(),"네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
}
