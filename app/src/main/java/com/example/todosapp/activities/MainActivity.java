package com.example.todosapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todosapp.R;
import com.example.todosapp.adapters.ViewPager2StateAdapter;
import com.example.todosapp.fragments.main.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.todosapp.fragments.main.TaskFragment;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    BottomNavigationView bottomNavigationView;
    ViewPager2StateAdapter viewPagerAdapter;
    TaskFragment taskFragment;
    StatsFragment statsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bindingViews();
        initViewPager();
        handleEvents();
    }

    private void bindingViews() {
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void initViewPager() {

        // create a list fragments to show in one activity
        ArrayList<Fragment> fragments = new ArrayList<>();
        taskFragment = new TaskFragment();
        statsFragment = new StatsFragment();
        fragments.add(taskFragment);
        fragments.add(statsFragment);

        viewPagerAdapter = new ViewPager2StateAdapter(this, fragments, null);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setUserInputEnabled(false);
    }

    private void handleEvents() {

        // when swipe to change fragments -> update selected icon in bottom navigation view
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int id = bottomNavigationView.getMenu().getItem(position).getItemId();
                bottomNavigationView.setSelectedItemId(id);
            }
        });

        // when click to a icon, change fragment in viewpage to display
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Menu menu = bottomNavigationView.getMenu();
            int profileFragmentPos = 2;
            for (int i = 0; i < menu.size(); i++) {
                if (menu.getItem(i) == item) {
                    viewPager.setCurrentItem(i);

                    return true;
                }
            }
            return false;
        });
    }

}