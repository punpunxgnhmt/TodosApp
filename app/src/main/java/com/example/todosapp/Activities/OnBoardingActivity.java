package com.example.todosapp.Activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todosapp.Adapters.ViewPager2StateAdapter;
import com.example.todosapp.Fragments.OnBoarding.OnBoarding1Fragment;
import com.example.todosapp.Fragments.OnBoarding.OnBoarding2Fragment;
import com.example.todosapp.Fragments.OnBoarding.OnBoarding3Fragment;
import com.example.todosapp.R;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator3;


/**
 * This activity will only show once, this is the first time which user open the application
 * */

public class OnBoardingActivity extends AppCompatActivity implements View.OnClickListener {

    ViewPager2 viewPager;
    CircleIndicator3 circleIndicator;
    ViewPager2StateAdapter adapter;
    private long backTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);
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