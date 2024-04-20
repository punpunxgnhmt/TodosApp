package com.example.todosapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todosapp.R;

public class SplashActivity extends AppCompatActivity {
    ImageView logoImg;
    TextView tvName;
    Animation scaleAnimation;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        InitComponents();
        InitAnimation();
        scaleAnimation();
        Navigate();

    }
    private void InitComponents() {
        logoImg = findViewById(R.id.logo_img);
        tvName = findViewById(R.id.tv_name);
        sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
    }

    private void InitAnimation() {
        scaleAnimation = new ScaleAnimation(0.5f, 1f, 0.5f, 1f, 50f, 50f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatMode(Animation.INFINITE);
    }

    private void scaleAnimation() {
        logoImg.startAnimation(scaleAnimation);
        tvName.startAnimation(scaleAnimation);
    }

    private void Navigate() {
        // After 1 second after the application start, navigate to auth screen to authentication
        Handler handler = new Handler();
        boolean isFirstRun = sharedPreferences.getBoolean("IsFirstRun", true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
//                Class<?> nextClass = isFirstRun ? OnBoardingActivity.class : AuthActivity.class;
                intent = new Intent(SplashActivity.this, OnBoardingActivity.class);
                startActivity(intent);
                sharedPreferences.edit().putBoolean("IsFirstRun", false).apply();
                finish();
                handler.removeCallbacks(this);
                handler.removeCallbacks(this);
            }
        }, 1000);
    }
}