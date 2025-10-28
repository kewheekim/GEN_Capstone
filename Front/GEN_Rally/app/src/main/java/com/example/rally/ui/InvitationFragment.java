package com.example.rally.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rally.R;
import com.google.android.material.tabs.TabLayout;

// MAT_REC_001, MAT_SENT_001
public class InvitationFragment extends Fragment {
    private static final String ARG_INITIAL_TAB = "initial_tab";
    private TabLayout tabs;
    public static InvitationFragment newInstance(boolean openSent) {
        InvitationFragment f = new InvitationFragment();
        Bundle b = new Bundle();
        b.putString(ARG_INITIAL_TAB, openSent ? "sent" : "received");
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        tabs = v.findViewById(R.id.tab_invitation);

        //  받은요청/ 보낸요청 탭
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                Fragment f = (tab.getPosition() == 0)
                        ? new InvitationReceivedFragment()
                        : new InvitationSentFragment();

                getChildFragmentManager().beginTransaction()
                        .replace(R.id.fragment_layout, f)
                        .commit();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 초기 탭 화면 설정
        if (savedInstanceState == null) {
            String initial = (getArguments() != null)
                    ? getArguments().getString(ARG_INITIAL_TAB)
                    : null;

            int index = "sent".equals(initial) ? 1 : 0; // 디폴트: 받은 요청
            Fragment initialChild = (index == 0)
                    ? new InvitationReceivedFragment()
                    : new InvitationSentFragment();

            getChildFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_layout, initialChild)
                    .commitNow();

            TabLayout.Tab tab = tabs.getTabAt(index);
            if (tab != null) tab.select();
        }
    }
}
