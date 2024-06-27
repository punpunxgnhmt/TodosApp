package com.example.todosapp.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todosapp.R;
import com.example.todosapp.Utils.Constants;

/**
 * This activity will be show first when user open the application
 * */
public class SplashActivity extends AppCompatActivity {

    ImageView logoImg;
    TextView tvName;
    Animation scaleAnimation;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        InitComponents();
        InitAnimation();
        scaleAnimation();
        Navigate();
    }



    private void InitComponents() {
        logoImg = findViewById(R.id.logo_img);
//        tvName = findViewById(R.id.tv_name);
        sharedPreferences = getSharedPreferences(Constants.LOGIN, Context.MODE_PRIVATE);
    }

    private void InitAnimation() {
        scaleAnimation = new ScaleAnimation(0.5f, 1f, 0.5f, 1f, 50f, 50f);
        scaleAnimation.setDuration(1000);
        scaleAnimation.setRepeatMode(Animation.REVERSE);
        scaleAnimation.setRepeatMode(Animation.INFINITE);
    }

    private void scaleAnimation() {
        logoImg.startAnimation(scaleAnimation);
//        tvName.startAnimation(scaleAnimation);
    }

    private void Navigate() {
        // After 1 second after the application start, navigate to auth screen to authentication
        Handler handler = new Handler();
        boolean isFirstRun = sharedPreferences.getBoolean(Constants.IS_FIRST_RUN, true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                Class<?> nextClass = isFirstRun ? OnBoardingActivity.class : AuthActivity.class;
                intent = new Intent(SplashActivity.this, nextClass);
                startActivity(intent);
                sharedPreferences.edit().putBoolean(Constants.IS_FIRST_RUN, false).apply();
                finish();
                handler.removeCallbacks(this);
            }
        }, 1000);
    }
}