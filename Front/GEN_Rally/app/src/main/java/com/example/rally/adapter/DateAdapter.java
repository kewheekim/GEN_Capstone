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

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.VH> {
    public interface Listener { void onDateSelected(LocalDate date); }
    private List<LocalDate> items;
    private int selectedPos = 0;
    private Listener listener;

    public DateAdapter(List<LocalDate> items, Listener l) {
        this.items = items;
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View vew = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_date_card, p,false);
        return new VH(vew);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        LocalDate d = items.get(pos);
        String dayLabel = pos==0? "오늘" : pos==1? "내일"
                : d.getDayOfWeek().getDisplayName(
                TextStyle.SHORT, Locale.KOREAN);
        h.tvDay.setText(dayLabel);
        h.tvDate.setText(d.getMonthValue()+"월 "+d.getDayOfMonth()+"일");

        // 선택 강조
        MaterialCardView card = (MaterialCardView) h.itemView;
        boolean sel = pos == selectedPos;
        card.setStrokeWidth(sel? 2:1);
        card.setStrokeColor(ColorStateList.valueOf(
                sel? Color.parseColor("#2ABA72") : Color.parseColor("#D9D9D9")));

        int fillColor = ContextCompat.getColor(
                card.getContext(),
                sel ? R.color.green_select : R.color.white
        );
        card.setBackgroundTintList(ColorStateList.valueOf(fillColor));

        h.itemView.setOnClickListener(v->{
            int old = selectedPos;
            selectedPos = h.getAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selectedPos);
            listener.onDateSelected(items.get(selectedPos));
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDay, tvDate;
        VH(View v) {
            super(v);
            tvDay  = v.findViewById(R.id.tv_day_label);
            tvDate = v.findViewById(R.id.tv_date_label);
        }
    }
}

