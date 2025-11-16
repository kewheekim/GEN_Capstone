package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.rally.R;
import com.example.rally.dto.MatchRequestDto;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// HOM_001, MAT_001
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment selected = null;

            if (id == R.id.navigation_home) {
                selected = new HomeFragment();
            } else if (id == R.id.navigation_matching) {
                selected = MatchFragment.newInstance(0);
            } else if (id == R.id.navigation_record) {
                //selected = new RecordFragment();
            } else if (id == R.id.navigation_my) {
                //selected = new MyPageFragment();
            }

            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selected)
                        .commit();
                return true;
            }
            return false;
        });

        // 다른 액티비티에서 MainActivity로 전환
        String target = getIntent().getStringExtra("navigateTo");
        Log.d("MainActivity", "navigateTo="+target);

        if (target != null && target.equals("matching")) {
            MatchFragment fragment = MatchFragment.newInstance(1);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment).commit();
            // 하단 내비게이션
            bottomNav.getMenu().findItem(R.id.navigation_matching).setChecked(true);
            return;
        }
        bottomNav.setSelectedItemId(R.id.navigation_home);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        String target = intent.getStringExtra("navigateTo");
        MatchRequestDto userInput = (MatchRequestDto) intent.getSerializableExtra("userInput");

        if ("matching".equals(target) && userInput != null) {
            MatchFragment fragment = MatchFragment.newInstance(1);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            bottomNav.getMenu().findItem(R.id.navigation_matching).setChecked(true);
        }
        // 매칭 > 받은/보낸 요청 > 보낸 요청 탭
        if ("invitation_sent".equals(target)) {
            // MatchFragment  매칭 > 받은/보낸 요청 탭 열기
            Fragment fragment = MatchFragment.newInstance(2, "sent");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            bottomNav.getMenu().findItem(R.id.navigation_matching).setChecked(true);
            return;
        }
        // 매칭 > 받은/보낸 요청 > 받은 요청 탭
        if ("invitation_received".equals(target)) {
            Fragment fragment = MatchFragment.newInstance(2, "received");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            bottomNav.getMenu().findItem(R.id.navigation_matching).setChecked(true);
            return;
        }
    }
}