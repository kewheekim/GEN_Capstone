package com.example.rally.ui;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// CHAT_003
public class ChatPromiseActivity extends AppCompatActivity implements OnMapReadyCallback {
    TextView tvPromiseTitle, tvPromiseStyle, tvPromiseDate, tvPromisePlace, tvStartTime, tvEndTime;
    TextView tvTimeMe, tvTimeYou, tvPlaceMe, tvPlaceYou;
    ImageButton btnDate, btnStartTime, btnEndTime, btnPlace;
    Button btnFinish;
    String promisePlace;
    private GoogleMap mMap;
    private ActivityResultLauncher<Intent> searchLauncher;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_promise);

        tvPromiseTitle = findViewById(R.id.tv_promise_title);
        tvPromiseStyle = findViewById(R.id.tv_promise_style);
        tvPromiseDate = findViewById(R.id.tv_promise_date);
        tvStartTime = findViewById(R.id.tv_promise_start_time);
        tvEndTime = findViewById(R.id.tv_promise_end_time);
        tvPromisePlace = findViewById(R.id.tv_promise_place);

        tvTimeMe = findViewById(R.id.tv_time_me);
        tvTimeYou = findViewById(R.id.tv_time_you);
        tvPlaceMe = findViewById(R.id.tv_place_me);
        tvPlaceYou = findViewById(R.id.tv_place_you);

        btnDate = findViewById(R.id.btn_select_date);
        btnStartTime = findViewById(R.id.btn_select_start_time);
        btnEndTime = findViewById(R.id.btn_select_end_time);
        btnPlace = findViewById(R.id.btn_select_place);
        btnFinish = findViewById(R.id.btn_finish);

        Intent prev = getIntent();
        String opponentName = prev.getStringExtra("opponent_name") + "님과 경기 약속";
        tvPromiseTitle.setText(opponentName);
        // 경기 카드
        tvTimeMe.setText(prev.getStringExtra("my_time"));
        tvPlaceMe.setText(prev.getStringExtra("my_place"));
        tvTimeYou.setText(prev.getStringExtra("opponent_time"));
        tvPlaceYou.setText(prev.getStringExtra("opponent_place"));

        // 하단
        String style = prev.getStringExtra("match_type") + " | " +prev.getStringExtra("match_style");
        tvPromiseStyle.setText(style);
        tvPromiseDate.setText(prev.getStringExtra("match_date"));
        promisePlace = prev.getStringExtra("my_place");
        tvPromisePlace.setText(promisePlace);

        String timeRange = prev.getStringExtra("my_time");
        if (timeRange != null && !timeRange.isEmpty()) {
            String[] parts = timeRange.split("~");

            if (parts.length == 2) {
                String startTimeStr = parts[0].split(":")[0];
                String endTimeStr = parts[1].split(":")[0];

                tvStartTime.setText(startTimeStr + ":00");
                tvEndTime.setText(endTimeStr + ":00");
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // onMapReady 콜백 등록
        }

        btnStartTime.setOnClickListener(v->{
            String currentTimeStr = tvStartTime.getText().toString();
            showTimePickerDialog(currentTimeStr, tvStartTime);
        });

        btnEndTime.setOnClickListener(v->{
            String currentTimeStr = tvEndTime.getText().toString();
            showTimePickerDialog(currentTimeStr, tvEndTime);
        });

        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        // SearchLocationActivity에서 돌아온 결과 처리
                        String name = result.getData().getStringExtra("location_name");
                        String address = result.getData().getStringExtra("address_name");
                        double lat = result.getData().getDoubleExtra("lat", 0);
                        double lng = result.getData().getDoubleExtra("lng", 0);

                        promisePlace = name;
                        selectedLatLng = new LatLng(lat, lng); // 위경도 저장
                        tvPromisePlace.setText(name);

                        if (mMap != null) {
                            mMap.clear();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
                            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title(name));
                        }
                    }
                });

        btnPlace.setOnClickListener(v->{
            Intent intent = new Intent(this, SearchLocationActivity.class);
            searchLauncher.launch(intent);
        });

        // 완료 버튼 -> 경기 약속 생성 카드 전송
        btnFinish.setOnClickListener(v->{
            String finalDate = tvPromiseDate.getText().toString();
            String finalStartTime = tvStartTime.getText().toString();
            String finalEndTime = tvEndTime.getText().toString();
            String finalPlaceName = tvPromisePlace.getText().toString();

            double lat = selectedLatLng != null ? selectedLatLng.latitude : 0;
            double lng = selectedLatLng != null ? selectedLatLng.longitude : 0;
            Intent resultIntent = new Intent();

            // 약속 정보 담기
            resultIntent.putExtra("final_date", finalDate);
            resultIntent.putExtra("final_start_time", finalStartTime);
            resultIntent.putExtra("final_end_time", finalEndTime);
            resultIntent.putExtra("final_location_name", finalPlaceName);
            resultIntent.putExtra("final_lat", lat);
            resultIntent.putExtra("final_lng", lng);

            setResult(RESULT_OK, resultIntent);
            finish();
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (promisePlace != null && !promisePlace.isEmpty()) {
            // 장소 이름을 위/경도로 변환하고 지도에 표시하는 함수 호출
            displayLocationOnMap(promisePlace);
        } else {
            Log.e("ChatPromiseActivity", "매칭 장소 정보(promisePlaceName)가 없습니다.");
        }
    }

    private void showTimePickerDialog(String timeStr, TextView targetTextView) {
        int hour = 0;
        int minute = 0;

        try {
            String[] parts = timeStr.split(":");
            if (parts.length >= 1) {
                hour = Integer.parseInt(parts[0]);
            }
            if (parts.length >= 2) {
                minute = Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            Log.e("ChatPromiseActivity", "시간 파싱 오류: " + timeStr, e);
            // 파싱 오류 시 현재 시각을 기본값으로 사용
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHourOfDay, selectedMinute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHourOfDay, selectedMinute);
                    targetTextView.setText(selectedTime);
                },
                hour,
                minute,
                true
        );

        timePickerDialog.show();
    }

    private void displayLocationOnMap(String locationName) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
            // 주소 이름을 기반으로 위/경도 리스트 가져오기
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng locationLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(locationLatLng).title(locationName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationLatLng, 15));
            } else {
                Log.e("ChatPromiseActivity", "Geocoder가 장소: " + locationName + "에 대한 위/경도를 찾지 못했습니다.");
            }
        } catch (IOException e) {
            Log.e("ChatPromiseActivity", "Geocoder 서비스 오류", e);
        }
    }
}
