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
                selected = new MatchFragment();
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

        // 다른 액티비티에서 mainActivity로 전환
        String target = getIntent().getStringExtra("navigateTo");
        Log.d("MainActivity", "navigateTo="+target);

        if (target != null && target.equals("matching")) {
            MatchRequestDto userInput = (MatchRequestDto) getIntent().getSerializableExtra("userInput");
            Log.d("MainActivity", "userInput' getPlace="+userInput.getPlace());

            MatchFragment fragment = MatchFragment.newInstance(userInput);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            // 하단 내비게이션 ui
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
            MatchFragment fragment = MatchFragment.newInstance(userInput);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            bottomNav.getMenu().findItem(R.id.navigation_matching).setChecked(true);
        }
    }
}
