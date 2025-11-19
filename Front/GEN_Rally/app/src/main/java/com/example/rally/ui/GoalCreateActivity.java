package com.example.rally.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rally.R;

public class GoalCreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_create);

        if (savedInstanceState == null) {
            showThemeFragment();
        }
    }

    public void showThemeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new GoalThemeFragment())
                .commit();
    }

    public void showTargetFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_layout, new GoalTargetFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showCalorieFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new GoalCalorieFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showCreateFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new GoalCreateFragment())
                .addToBackStack(null)
                .commit();
    }
}
