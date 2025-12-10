package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchRequestDto;
import com.example.rally.wear.PhoneDataLayerClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.tabs.TabLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// MAT_001
public class MatchFragment extends Fragment {
    private static final String ARG_INITIAL_TAB_INDEX = "initial_tab_index"; // 0: 찾은 매칭, 1: 찾고있는 매칭, 2: 받은/보낸 요청
    private static final String ARG_INVITATION_SUBTAB = "invitation_subtab"; // "sent"|"received"
    private  TabLayout tabs;
    private View viewUnread;
    private TextView tvUnread;
    private ApiService api;

    public static MatchFragment newInstance(int initialTabIndex, @Nullable String invitationSubTab) {
        MatchFragment f = new MatchFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_INITIAL_TAB_INDEX, initialTabIndex); // 0:Found, 1:Seeking, 2:Invitation
        if (invitationSubTab != null) b.putString(ARG_INVITATION_SUBTAB, invitationSubTab);
        f.setArguments(b);
        return f;
    }

    public static MatchFragment newInstance(int initialTabIndex) {
        return newInstance(initialTabIndex, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_match, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Button btnStart = view.findViewById(R.id.btn_start);
        api = RetrofitClient.getSecureClient(getContext(), BuildConfig.API_BASE_URL).create(ApiService.class);
        tabs = view.findViewById(R.id.tab_match);
        viewUnread = view.findViewById(R.id.view_unread);
        tvUnread = view.findViewById(R.id.tv_unread);

        tabs.addOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                switchTab(pos);
                if(pos ==2) {
                    api.markAsReadInvitation().enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                updateUnreadBadge(0);
                            }
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            if (!isAdded()) return;
                        }
                    });
                }
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
        });

        if (savedInstanceState == null) {
            int initial = (getArguments() != null) ? getArguments().getInt(ARG_INITIAL_TAB_INDEX, 0) : 0;
            switchTab(initial);
            TabLayout.Tab t = tabs.getTabAt(initial);
            if (t != null) t.select();
        }
        loadUnreadNotification();
    }
    private void updateUnreadBadge(int count) {
        if (viewUnread == null || tvUnread == null) return;

        if (count > 0) {
            viewUnread.setVisibility(View.VISIBLE);
            tvUnread.setVisibility(View.VISIBLE);
            tvUnread.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            viewUnread.setVisibility(View.GONE);
            tvUnread.setVisibility(View.GONE);
        }
    }

    private void loadUnreadNotification() {
        api.getUnreadInvitationCounts().enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (!isAdded()) return;
                if(response.isSuccessful() && response.body() != null) {
                    int count= response.body();
                    updateUnreadBadge(count);
                } else {
                    updateUnreadBadge(0);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                if (!isAdded()) return;
                updateUnreadBadge(0);
            }
        });
    }

    private void switchTab(int position) {
        Fragment target;
        String tag;

        switch (position) {
            case 0:
                tag = "MatFoundFragment";
                target = getChildFragmentManager().findFragmentByTag(tag);
                if (target == null) target = new MatFoundFragment();
                break;
            case 1:
                tag = "MatSeekingFragment";
                target = getChildFragmentManager().findFragmentByTag(tag);
                if (target == null) target = new MatSeekingFragment();
                break;
            default: // 2 = Invitation
                tag = "InvitationFragment";
                String sub = (getArguments() != null)
                        ? getArguments().getString(ARG_INVITATION_SUBTAB, "received")
                        : "received";
                boolean openSent = "sent".equals(sub);
                target = InvitationFragment.newInstance(openSent);
                break;
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, target, tag)
                .commit();
    }
}