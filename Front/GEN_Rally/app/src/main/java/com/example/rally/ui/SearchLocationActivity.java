package com.example.rally.ui;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Locale;

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
            Places.initialize(getApplicationContext(), "AIzaSyBHB-noMRpwjdh2-CyN7b4_MU9J0GmeZqs", Locale.KOREAN);
        }
        placesClient = Places.createClient(this);

        searchAdapter = new SearchAdapter(searchResults, this::fetchPlaceDetails);
        rvResults.setAdapter(searchAdapter);

        // 검색어 입력 후 엔터 → 검색 실행
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                if(!keyword.isEmpty()) {
                    searchPlaces(keyword);
                }
                return true;
            }
            return false;
        });
    }

    private void searchPlaces(String keyword) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(keyword)
                .setCountries(Arrays.asList("KR"))  // 대한민국 대상으로 검색
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            searchResults.clear();
            searchResults.addAll(response.getAutocompletePredictions());
            searchAdapter.notifyDataSetChanged();
        });
    }

    private void fetchPlaceDetails(AutocompletePrediction p) {
        List<Place.Field> fields =
                Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        FetchPlaceRequest req = FetchPlaceRequest.builder(p.getPlaceId(), fields).build();
        placesClient.fetchPlace(req).addOnSuccessListener(res -> {
            Place place = res.getPlace();
            sendResult(place.getName(),
                    place.getAddress(),               // 주소
                    place.getLatLng().latitude,
                    place.getLatLng().longitude);
        });
    }

    // 선택한 장소 → SetLocationActivity로 전달
    private void sendResult(String name, String address, double lat, double lng) {
        Intent intent = new Intent();
        intent.putExtra("location_name", name);
        intent.putExtra("address_name", address);
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        setResult(RESULT_OK, intent);
        finish();
    }
}
