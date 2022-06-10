package com.example.todosapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todosapp.Activities.Task.AddTaskActivity;
import com.example.todosapp.Adapters.ViewPager2StateAdapter;
import com.example.todosapp.Fragments.Main.CalendarFragment;
import com.example.todosapp.Fragments.Main.ProfileFragment;
import com.example.todosapp.Fragments.Main.TaskFragment;
import com.example.todosapp.R;
import com.example.todosapp.Views.MessageLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    MessageLayout messageLayout;
    ViewPager2 viewPager;
    BottomNavigationView bottomNavigationView;
    ViewPager2StateAdapter viewPagerAdapter;
    FloatingActionButton btnAddTask;
    CircleImageView imgAnim;

    TaskFragment taskFragment;
    CalendarFragment calendarFragment;
    ProfileFragment profileFragment;
    long backTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindingViews();
        initViewPager();
        createAnim();
        handleEvents();
    }

    private void bindingViews() {
        messageLayout = findViewById(R.id.messageLayout);
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        btnAddTask = findViewById(R.id.btnAddTask);
        imgAnim = findViewById(R.id.imgAnim);
    }

    private void initViewPager() {

        // create a list fragments to show in one activity
        ArrayList<Fragment> fragments = new ArrayList<>();
        taskFragment = new TaskFragment();
        calendarFragment = new CalendarFragment();
        profileFragment = new ProfileFragment();
        fragments.add(taskFragment);
        fragments.add(calendarFragment);
        fragments.add(profileFragment);

        viewPagerAdapter = new ViewPager2StateAdapter(this, fragments, null);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(fragments.size());
        viewPager.setUserInputEnabled(false);
    }

    private void handleEvents() {

        // when swipe to change fragments -> update selected icon in bottom navigtaion view
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
                    if (i == profileFragmentPos) {
                        profileFragment.closeNavView();
                    }
                    return true;
                }
            }
            return false;
        });

        // when user click to the button which have plus symbol -> navigate to add task activity
        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
            int currentFragPos = viewPager.getCurrentItem();
            if (currentFragPos == 0) {
                String selectedTagId = taskFragment.getSelectedTagId();
                if (!selectedTagId.equals(""))
                    intent.putExtra("selectedTagId", selectedTagId);
            } else {
                if (currentFragPos == 1) {
                    Date selectedDate = calendarFragment.getSelectedDate();
                    intent.putExtra("selectedDate", selectedDate);
                }
            }
            startActivity(intent);
        });
    }

    private void createAnim() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.floating_action_button_anim);
        imgAnim.startAnimation(animation);
    }

    @Override
    public void onBackPressed() {

        // handle when user click back button on device
        int currentFragmentPos = viewPager.getCurrentItem();
        int profileFragmentPos = 2;

        // if current fragment is user fragment
        // close the navigation view first
        if (currentFragmentPos == profileFragmentPos) {
            if (profileFragment.isOpenNavView()) {
                profileFragment.closeNavView();
                return;
            }
        }

        // user have to click back button twice in 2 seconds to close application
        if (backTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finishAffinity();
            return;
        } else {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
        }

        backTime = System.currentTimeMillis();
    }
}