package com.example.rally.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rally.R;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.Node;

import java.util.List;

public class RecordActivity extends AppCompatActivity {

    private static final String TAG = "WearApp";
    private static final String MESSAGE_PATH = "/trigger_notification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Button btnAdd = findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(view -> sendMessageToPhone());
    }

    private void sendMessageToPhone() {
        Log.d(TAG, "메시지 전송");

        MessageClient messageClient = Wearable.getMessageClient(this);

        Wearable.getNodeClient(this).getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (!nodes.isEmpty()) {
                        Node node = nodes.get(0); // 첫 번째 노드 사용
                        messageClient.sendMessage(node.getId(), MESSAGE_PATH, new byte[0])
                                .addOnSuccessListener(unused -> Log.d(TAG, "메시지 전송 성공"))
                                .addOnFailureListener(e -> Log.e(TAG, "메시지 전송 실패", e));
                    } else {
                        Log.e(TAG, "연결된 노드 없음");
                    }
                });
    }
}
