package com.example.rally.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.google.android.libraries.places.api.model.AutocompletePrediction;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    public interface OnPlaceSelectedListener {
        void onPlaceSelected(AutocompletePrediction place);
    }

    private final List<AutocompletePrediction> items;
    private final OnPlaceSelectedListener listener;

    public SearchAdapter(List<AutocompletePrediction> items, OnPlaceSelectedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_results, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AutocompletePrediction item = items.get(position);
        holder.placeName.setText(item.getPrimaryText(null));
        holder.placeAddress.setText(item.getSecondaryText(null));

        holder.btnSelect.setOnClickListener(v -> listener.onPlaceSelected(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView placeName, placeAddress;
        Button btnSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placeName = itemView.findViewById(R.id.tv_place_name);
            placeAddress = itemView.findViewById(R.id.tv_place_address);
            btnSelect = itemView.findViewById(R.id.btn_select);
        }
    }
}
