package com.example.proform;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class LogoAnimation extends AppCompatActivity {
    private ImageView image;
    private static final int ANIMATION_DURATION = 1000;
    private static final int DISPLAY_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo_animation);

        image = findViewById(R.id.image);

        animateImage();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LogoAnimation.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, DISPLAY_DURATION);
    }

    private void animateImage() {
        image.animate()
                .setDuration(ANIMATION_DURATION)
                .alpha(0.5f)
                .scaleX(0.5f)
                .scaleY(0.5f)
                .rotationY(360f)
                .translationY(200f)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        image.animate()
                                .setDuration(ANIMATION_DURATION)
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .rotationX(360f)
                                .translationY(0f)
                                .start();
                    }
                })
                .start();
    }
}
