package com.example.rally.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rally.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// MAT_AP_003
public class SetLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLatLng;
    private TextView tvLocationName;
    private TextView tvAddressName;
    private EditText etSearch;
    private Button nextBtn;

    private ActivityResultLauncher<Intent> searchLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        tvLocationName = findViewById(R.id.tv_location_name);
        tvAddressName = findViewById(R.id.tv_address_name);
        etSearch = findViewById(R.id.et_search);
        nextBtn = findViewById(R.id.btn_next);

        // SetTimeActivity에서 넘어온 값 꺼내기
        Intent prev = getIntent();
        String date = prev.getStringExtra("date");
        ArrayList<Integer> hours = prev.getIntegerArrayListExtra("hours");

        nextBtn.setEnabled(false);

        // 검색창 클릭 → SearchLocationActivity 이동
        searchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String name = result.getData().getStringExtra("location_name");
                        String address = result.getData().getStringExtra("address_name");
                        double lat = result.getData().getDoubleExtra("lat", 0);
                        double lng = result.getData().getDoubleExtra("lng", 0);

                        currentLatLng = new LatLng(lat, lng);
                        tvLocationName.setText(name);
                        tvAddressName.setText(address);
                        etSearch.setText(name);

                        if (mMap != null) {
                            mMap.clear();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            mMap.addMarker(new MarkerOptions().position(currentLatLng).title(name));
                        }

                        // "다음" 버튼 활성화
                        nextBtn.setEnabled(true);
                        nextBtn.setTextColor(Color.parseColor("white"));
                    }
                });

        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchLocationActivity.class);
            searchLauncher.launch(intent);
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        checkLocationPermission();

        nextBtn.setOnClickListener(view -> {
            // 장소 확인 팝업으로 전달
            String locationName = tvLocationName.getText().toString();
            String addressName = tvAddressName.getText().toString();

            Intent intent = new Intent(SetLocationActivity.this, PopupLocationActivity.class);
            intent.putExtra("gameType", prev.getIntExtra("gameType", -1));
            intent.putExtra("gameStyle", prev.getIntExtra("gameStyle", -1));
            intent.putExtra("sameGender", prev.getBooleanExtra("sameGender", false));

            intent.putExtra("date", date);
            intent.putIntegerArrayListExtra("hours", hours);

            intent.putExtra("location_name", locationName);
            intent.putExtra("address_name", addressName);
            intent.putExtra("lat", currentLatLng.latitude);
            intent.putExtra("lng", currentLatLng.longitude);

            startActivity(intent);
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        // 1) 지도를 갱신
                        if (mMap != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("현재 위치"));
                        }

                        // 2) 위·경도를 주소(문자열)로 변환
                        String addressString = "주소를 가져올 수 없습니다.";
                        try {
                            Geocoder geocoder = new Geocoder(this, Locale.KOREA);
                            List<Address> addresses = geocoder.getFromLocation(
                                    location.getLatitude(),
                                    location.getLongitude(),
                                    1
                            );
                            if (addresses != null && addresses.size() > 0) {
                                Address addr = addresses.get(0);
                                // 원하는 형식으로 가져오기 (예: 도로명 + 상세)
                                addressString = addr.getAddressLine(0);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // 3) TextView에 보여주기
                        tvLocationName.setText("현재 위치");
                        tvAddressName.setText(addressString);
                        etSearch.setText("현재 위치");

                        nextBtn.setEnabled(true);
                        nextBtn.setTextColor(Color.WHITE);
                    } else {
                        tvLocationName.setText("위치 정보를 가져올 수 없습니다.");
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getLastLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
