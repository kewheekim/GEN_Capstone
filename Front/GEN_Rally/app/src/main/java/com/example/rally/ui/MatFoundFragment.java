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
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchFoundItem;
import com.example.rally.adapter.MatchFoundAdapter;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatFoundFragment extends Fragment {
    private RecyclerView rv;
    private MatchFoundAdapter adapter;
    private Dialog cancelDialog;
    private Call<List<MatchFoundItem>> call;
    private Call<ResponseBody> cancelCall;

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
            @Override public void onItemClick(@NonNull MatchFoundItem item) { }
            @Override public void onChatClick(@NonNull MatchFoundItem item) {
                final Long roomId = item.getRoomId();
                final Long userId = item.getUserId();
                if(roomId ==null || userId == null ) {
                    Toast.makeText(requireContext(), "채팅 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(requireContext(), ChatActivity.class);
                intent.putExtra(ChatActivity.ROOM_ID, roomId.longValue());
                intent.putExtra(ChatActivity.MY_USER_ID, userId.longValue());
                startActivity(intent);
            }
            @Override public void onMoreClick(View anchor, @NonNull MatchFoundItem item) {
                showMoreMenu(anchor, item);
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

    private void showMoreMenu(@NonNull View anchor, @NonNull MatchFoundItem matchFoundItem) {
        if (!isAdded()) return;
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenuInflater().inflate(R.menu.match_option_menu, menu.getMenu());

        menu.setOnMenuItemClickListener( mi -> {
            int id= mi.getItemId();
            if (id == R.id.match_cancel) {
                showCancelDialog(matchFoundItem.getGameId());
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void showCancelDialog(@NonNull Long gameId) {
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
        btnBack.setOnClickListener(dismiss);

        btnCancel.setOnClickListener( v -> {
            if (!isAdded()) return;

            btnCancel.setEnabled(false);

            ApiService api = RetrofitClient
                    .getSecureClient(requireContext(), BuildConfig.API_BASE_URL)
                    .create(ApiService.class);

            cancelCall = api.cancelGame(gameId);
            cancelCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!isAdded() || call.isCanceled()) return;
                    btnCancel.setEnabled(true);

                    if (response.isSuccessful()) {
                        Toast.makeText(requireContext(), "매칭이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        fetchFoundMatches();
                    } else {
                        Toast.makeText(requireContext(), "취소 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (!isAdded() || call.isCanceled()) return;
                    btnCancel.setEnabled(true);
                    Toast.makeText(requireContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        cancelDialog = dialog;
        dialog.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(cancelDialog != null && cancelDialog.isShowing())
            cancelDialog.dismiss();
        cancelDialog = null;
        if (call != null) { call.cancel(); call = null; }
        rv = null;
    }
}