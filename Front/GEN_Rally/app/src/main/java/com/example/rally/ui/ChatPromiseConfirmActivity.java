package com.example.rally.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.rally.BuildConfig;
import com.example.rally.R;
import com.example.rally.api.ApiService;
import com.example.rally.api.RetrofitClient;
import com.example.rally.dto.MatchConfirmDto;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// CHAT_004
public class ChatPromiseConfirmActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String ROOM_ID = "extra_room_id";
    Toolbar toolbar;
    ImageView ivProfile, ivGender;
    TextView tvStyle, tvNickname, tvDate, tvStartTime, tvEndTime, tvPlace;
    String confirmedTime;
    String confirmedPlace;
    Button btnConfirm;


    private long roomId = -1L;
    private ApiService apiService;
    private GoogleMap mMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_promise_confirm);

        toolbar = findViewById(R.id.toolbar);
        ivProfile = findViewById(R.id.iv_profile);
        ivGender = findViewById(R.id.iv_gender);
        tvStyle = findViewById(R.id.tv_promise_style);
        tvNickname = findViewById(R.id.tv_nickname);
        tvDate = findViewById(R.id.tv_promise_date);
        tvStartTime = findViewById(R.id.tv_promise_start_time);
        tvEndTime = findViewById(R.id.tv_promise_end_time);
        tvPlace = findViewById(R.id.tv_promise_place);
        btnConfirm = findViewById(R.id.btn_confirm);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());

        Intent prev = getIntent();

        roomId = prev.getLongExtra(ROOM_ID, -1L);
        if (roomId == -1L) {
            Log.e("ChatPromiseConfirmActivity", "RoomID is missing");
            btnConfirm.setEnabled(true);
        }

        tvNickname.setText(prev.getStringExtra("opponent_name"));
        tvStyle.setText(prev.getStringExtra("match_type") + " | " + prev.getStringExtra("match_style"));
        tvDate.setText(prev.getStringExtra("match_info_date"));
        tvPlace.setText(prev.getStringExtra("match_info_place"));

        confirmedTime = prev.getStringExtra("match_info_time");
        confirmedPlace = prev.getStringExtra("match_info_place");

        String timeRange = prev.getStringExtra("match_info_time");
        String[] times = timeRange.split("~\\s*");

        String startTime = "";
        String endTime = "";

        if (times.length == 2) {
            startTime = times[0].trim();
            endTime = times[1].trim();
        } else {
            Log.e("ChatPromiseConfirmActivity", "시간 형식 오류: " + timeRange);
            startTime = "시간 정보 없음";
            endTime = "시간 정보 없음";
        }

        tvStartTime.setText(startTime);
        tvEndTime.setText(endTime);

        apiService = RetrofitClient
                .getSecureClient(getApplicationContext(), BuildConfig.API_BASE_URL)
                .create(ApiService.class);

        btnConfirm.setOnClickListener(v -> {
            MatchConfirmDto dto = new MatchConfirmDto();
            dto.setRoomId(roomId);
            dto.setTime(confirmedTime);
            dto.setPlace(confirmedPlace);

            setGameInfoConfirmMatch(dto);
        });
    }

    private void setGameInfoConfirmMatch(MatchConfirmDto dto){
        apiService.confirmMatch(dto)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ChatPromiseConfirmActivity.this, "경기가 성공적으로 확정되었습니다.", Toast.LENGTH_LONG).show();

                            Intent resultIntent = new Intent();
                            String date = tvDate.getText().toString();
                            resultIntent.putExtra("match_info_date", date);
                            resultIntent.putExtra("match_info_time", confirmedTime);
                            resultIntent.putExtra("match_info_place", confirmedPlace);

                            setResult(RESULT_OK, resultIntent);
                            finish(); 

                        } else {
                            Log.e("ConfirmMatch", "확정 실패: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("ConfirmMatch", "확정 실패: " + t.getMessage());
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (confirmedPlace != null && !confirmedPlace.isEmpty()) {
            // 장소 이름을 위/경도로 변환하고 지도에 표시하는 함수 호출
            displayLocationOnMap(confirmedPlace);
        } else {
            Log.e("ChatPromiseConfirmActivity", "매칭 장소 정보(promisePlaceName)가 없습니다.");
        }
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
                Log.e("ChatPromiseConfirmActivity", "Geocoder가 장소: " + locationName + "에 대한 위/경도를 찾지 못했습니다.");
            }
        } catch (IOException e) {
            Log.e("ChatPromiseConfirmActivity", "Geocoder 서비스 오류", e);
        }
    }
}
