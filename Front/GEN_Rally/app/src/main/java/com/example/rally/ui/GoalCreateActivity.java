package com.example.rally.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rally.R;

public class GoalCreateActivity extends AppCompatActivity {
    private String goalTheme;
    private String goalName;
    private String goalType;
    private Integer targetWeeks;
    private Integer targetCount;
    private Integer targetCalorie;

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
                .replace(R.id.fragment_container, new GoalTargetFragment())
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

    public void onThemeSelected(String goalTheme, String goalName) {
        this.goalName = goalName;
        this.goalTheme = goalTheme;
    }
    public void onTargetSelected(String goalType, Integer targetWeeks, Integer targetCount) {
        this.goalType = goalType;
        this.targetWeeks = targetWeeks;
        this.targetCount = targetCount;
    }
    public void onCalorieInputed(Integer targetCalorie) {
        this.targetCalorie = targetCalorie;
    }

    public String getGoalName() { return goalName; }
    public String getGoalTheme() { return goalTheme; }
    public String getGoalType() { return goalType; }
    public Integer getTargetWeeks() { return targetWeeks; }
    public Integer getTargetCount() { return targetCount; }
    public Integer getTargetCalorie() { return targetCalorie; }
}
