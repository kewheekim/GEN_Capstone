package com.example.rally.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rally.R;

public class GoalCreateFragment extends Fragment {

    public GoalCreateFragment() {  }

    public static GoalCreateFragment newInstance(String param1, String param2) {
        GoalCreateFragment fragment = new GoalCreateFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_goal_create, container, false);
    }
}