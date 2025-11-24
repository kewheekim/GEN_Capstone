package com.example.rally.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatSeekingFragment extends Fragment {
    private RecyclerView rv;
    private MatchSeekingAdapter adapter;
    private Dialog cancelDialog;
    private Call<List<MatchSeekingItem>> call;
    private Call<ResponseBody> cancelCall;

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
            public void onItemClick(MatchSeekingItem item, int position) { }
        });

        adapter.setOnMoreClickListener((anchor, item, position) -> {
            showMoreMenu(anchor, item);
        });

        // 후보 보기 버튼
        adapter.setOnCandidatesClickListener((item, position) -> {
            Long requestId = item.getRequestId();
            if (requestId == null || requestId <= 0) return;

            Intent intent = new Intent(requireContext(), LoadingActivity.class);
            intent.putExtra("requestId", requestId);
            intent.putExtra("gameType", item.getGameType());
            String gameStyle = item.getGameStyle();
            if(gameStyle == "편하게" || gameStyle == "열심히")
                gameStyle +=" 해요";
            else
                gameStyle = "상관없어요";
            intent.putExtra("gameStyle", gameStyle);
            intent.putExtra("date", item.getDate().split("\\(")[0]);
            intent.putExtra("time", item.getTime());
            intent.putExtra("placeName", item.getPlace());
            startActivity(intent);
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

    private void showMoreMenu(@NonNull View anchor, @NonNull MatchSeekingItem matchSeekingItem) {
        if (!isAdded()) return;
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenuInflater().inflate(R.menu.match_option_menu, menu.getMenu());

        menu.setOnMenuItemClickListener( mi -> {
            int id= mi.getItemId();
            if (id == R.id.match_cancel) {
                showCancelDialog(matchSeekingItem.getRequestId());
                return true;
            }
            return false;
        });
        menu.show();
    }

    private void showCancelDialog(@NonNull Long requestId) {
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
            if(!isAdded()) return;
            btnCancel.setEnabled(false);
            ApiService api = RetrofitClient
                    .getSecureClient(requireContext(), BuildConfig.API_BASE_URL).create(ApiService.class);

            cancelCall = api.cancelMatchRequest(requestId);
            cancelCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (!isAdded() || call.isCanceled()) return;

                    btnCancel.setEnabled(true);

                    if (response.isSuccessful()) {
                        android.widget.Toast.makeText(requireContext(),
                                "매칭 신청이 취소되었습니다.", android.widget.Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                        fetchSeekingMatches(); // 리스트 다시 조회
                    } else {
                        android.widget.Toast.makeText(requireContext(),
                                "취소 실패: " + response.code(), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    if (!isAdded() || call.isCanceled()) return;
                    btnCancel.setEnabled(true);
                    android.widget.Toast.makeText(requireContext(),
                            "네트워크 오류: " + t.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        });
        cancelDialog = dialog;
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (call != null) { call.cancel(); call = null; }
        if (cancelCall != null) { cancelCall.cancel(); cancelCall = null; }
        rv = null;
    }
}