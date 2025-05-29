package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rally.R;
import com.example.rally.adapter.SearchAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchLocationActivity extends AppCompatActivity {
    private EditText etSearch;
    private RecyclerView rvResults;
    private SearchAdapter searchAdapter;
    private PlacesClient placesClient; // 구글 Places API 사용
    private List<AutocompletePrediction> searchResults = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);

        etSearch = findViewById(R.id.et_search_query);
        rvResults = findViewById(R.id.rv_results);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        // Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyBZt2tKHejWOtLWkIT64JLGHifjUPomV7M");
        }
        placesClient = Places.createClient(this);

        searchAdapter = new SearchAdapter(searchResults, this::fetchPlaceDetails);
        rvResults.setAdapter(searchAdapter);

        // 검색어 입력 후 엔터 → 검색 실행
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String keyword = etSearch.getText().toString();
                searchPlaces(keyword);
                return true;
            }
            return false;
        });
    }

    private void searchPlaces(String keyword) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(keyword)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            searchResults.clear();
            searchResults.addAll(response.getAutocompletePredictions());
            searchAdapter.notifyDataSetChanged();
        });
    }

    private void fetchPlaceDetails(AutocompletePrediction prediction) {
        String placeId = prediction.getPlaceId();
        List<Place.Field> fields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            sendResult(place.getName(), place.getLatLng().latitude, place.getLatLng().longitude);
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
