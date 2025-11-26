package com.example.rally.ui;

import com.example.rally.R;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

// MAT_END_001
public class GameFinishActivity extends AppCompatActivity {
    private ImageView ivFinishGif;
    private Button btnNext;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_finish);

        ivFinishGif = findViewById(R.id.iv_finish_gif);
        btnNext = findViewById(R.id.btn_next);

        Glide.with(this).asGif().load(R.drawable.game_finish_gif)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(
                            @Nullable GlideException e,
                            Object model,
                            Target<GifDrawable> target,
                            boolean isFirstResource
                    ) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(
                            @NonNull GifDrawable resource,
                            Object model,
                            Target<GifDrawable> target,
                            DataSource dataSource,
                            boolean isFirstResource
                    ) {
                        resource.setLoopCount(1);
                        return false;
                    }
                })
                .into(ivFinishGif);

        btnNext.setOnClickListener( v -> {
            btnNext.setEnabled(false);
        });
    }
}