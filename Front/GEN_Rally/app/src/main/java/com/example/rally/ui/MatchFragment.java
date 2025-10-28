package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;
import com.example.rally.wear.PhoneDataLayerClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.tabs.TabLayout;

// MAT_001
public class MatchFragment extends Fragment {
    private static final String ARG_INITIAL_TAB_INDEX = "initial_tab_index"; // 0: 찾은 매칭, 1: 찾고있는 매칭 / 2: 받은/보낸 요청
    private static final String ARG_INVITATION_SUBTAB = "invitation_subtab"; // "sent"|"received"

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
        Button startBtn = view.findViewById(R.id.btn_start);
        TabLayout tabs = view.findViewById(R.id.tab_match);

        tabs.addOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { switchTab(tab.getPosition()); }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
        });

        if (savedInstanceState == null) {
            int initial = (getArguments() != null) ? getArguments().getInt(ARG_INITIAL_TAB_INDEX, 0) : 0;
            TabLayout.Tab t = tabs.getTabAt(initial);
            if (t != null) t.select();
            else {
                switchTab(initial);
                TabLayout.Tab first = tabs.getTabAt(0);
                if (first != null) first.select();
            }
        }

        Wearable.getNodeClient(requireContext()).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    StringBuilder sb = new StringBuilder("connected nodes: ");
                    for (Node n : nodes)
                        sb.append(n.getDisplayName()).append("(").append(n.getId()).append(") ");
                    android.util.Log.d("PhoneDL", sb.toString());
                    android.widget.Toast.makeText(requireContext(),
                            nodes.isEmpty() ? "연결된 워치 없음" : sb.toString(),
                            android.widget.Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> android.util.Log.e("PhoneDL", "getConnectedNodes failed", e));

        startBtn.setOnClickListener(v -> {
            PhoneDataLayerClient.sendGameSetup(
                    requireContext(),
                    "match-123",
                    "너무어려워요",
                    "안세영이되",
                    false,
                    new PhoneDataLayerClient.SendCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(requireContext(), "워치로 전송 완료", android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                        @Override
                        public void onNoNode() {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(requireContext(), "연결된 워치 없음", android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                        @Override
                        public void onError(Exception e) {
                            requireActivity().runOnUiThread(() ->
                                    android.widget.Toast.makeText(requireContext(), "전송 실패: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
            );
            startActivity(new Intent(requireContext(), ScoreMonitorActivity.class)
                    .putExtra("gameId", "match-123")
                    .putExtra("opponentName", "너무어려워요")
                    .putExtra("userName", "안세영이되")
                    .putExtra("localIsUser1", false)
            );
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