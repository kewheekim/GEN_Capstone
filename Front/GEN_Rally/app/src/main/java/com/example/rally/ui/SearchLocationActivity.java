package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

public class SearchLocationActivity extends AppCompatActivity {
    private EditText etSearch;
    private RecyclerView rvResults;
    private PlacesClient placesClient; // 구글 Places API 사용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        etSearch = findViewById(R.id.et_search);
        rvResults = findViewById(R.id.rv_results);

        // 구글 Places API 초기화
        Places.initialize(getApplicationContext(), "YOUR_API_KEY");
        placesClient = Places.createClient(this);

        // 검색어 입력 후 엔터 → 검색 실행
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etSearch.getText().toString();
            searchPlaces(keyword);
            return true;
        });
    }

    private void searchPlaces(String keyword) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(keyword)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            // RecyclerView에 예쁘게 뿌리기
        });
    }

    // 선택한 장소 → SetLocationActivity로 전달
    private void sendResult(String name, double lat, double lng) {
        Intent intent = new Intent();
        intent.putExtra("place_name", name);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        setResult(RESULT_OK, intent);
        finish();
    }
}
