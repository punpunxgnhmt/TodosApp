package com.example.todosapp.activities;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todosapp.R;
import com.example.todosapp.adapters.ViewPager2StateAdapter;
import com.example.todosapp.fragments.onboarding.OnBoarding1Fragment;
import com.example.todosapp.fragments.onboarding.OnBoarding2Fragment;
import com.example.todosapp.fragments.onboarding.OnBoarding3Fragment;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;

public class OnBoardingActivity extends AppCompatActivity implements View.OnClickListener{

    ViewPager2 viewPager;
    CircleIndicator3 circleIndicator;
    ViewPager2StateAdapter adapter;
    private long backTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_on_boarding);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboarding_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        InitComponent();
        InitAdapter();
    }

    private void InitComponent() {
        viewPager = findViewById(R.id.viewpager2_on_boarding);
        circleIndicator = findViewById(R.id.circle_indicator3_on_boarding);
    }

    private void InitAdapter() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(new OnBoarding1Fragment());
        fragments.add(new OnBoarding2Fragment());
        fragments.add(new OnBoarding3Fragment());
        adapter = new ViewPager2StateAdapter(this, fragments, null);
        viewPager.setAdapter(adapter);
        circleIndicator.setViewPager(viewPager);
        circleIndicator.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int currentItem = viewPager.getCurrentItem();
        if(currentItem != viewPager.getItemDecorationCount()- 1){
            viewPager.setCurrentItem(currentItem + 1);
        }
    }
}