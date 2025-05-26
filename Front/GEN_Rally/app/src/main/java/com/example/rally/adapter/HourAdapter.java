package com.example.rally.adapter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.google.android.material.card.MaterialCardView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class HourAdapter extends RecyclerView.Adapter<HourAdapter.VH> {
    private final List<Integer> hours;
    private final Set<Integer> selected = new HashSet<>();

    public HourAdapter(List<Integer> hours) {
        this.hours = hours;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hour_slot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        int hour = hours.get(position);
        holder.tvHour.setText(hour + "시");

        boolean isSel = selected.contains(hour);
        int fillColor = ContextCompat.getColor(
                holder.card.getContext(),
                isSel ? R.color.green_active : R.color.gray_inactive
        );
        holder.card.setBackgroundTintList(ColorStateList.valueOf(fillColor));

    }

    @Override
    public int getItemCount() {
        return hours.size();
    }

    public Set<Integer> getSelected() {
        return selected;
    }

    public void toggle(int hour) {
        if (selected.contains(hour)) selected.remove(hour);
        else selected.add(hour);
        notifyItemChanged(hours.indexOf(hour));
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvHour;
        MaterialCardView card;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tv_hour);
            card   = itemView.findViewById(R.id.card_slot);
        }
    }
}

