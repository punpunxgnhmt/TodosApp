package com.example.todosapp.Fragments.Main;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.todosapp.Activities.AuthActivity;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment {

    View view;
    DrawerLayout drawerLayout;
    TextView tvUsername, tvCountCompletedTasks, tvCountPendingTasks;
    ImageView imgUser;
    ImageButton btnOptions;
    PieChart pieChart;
    NavigationView navigationView;

    Database database;
    ChildRefEventListener tagChangeListener, taskChangeListener;

    FirebaseAuth auth;
    FirebaseUser user;
    Map<String, Integer> map;
    ArrayList<Task> tasks;
    ArrayList<Tag> tags;
    List<PieEntry> entries;
    ArrayList<Integer> colors;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        bindingViews();
        initDatabase();
        initData();
        initUserData();
        intTaskOverview();
        initChart();
        initNavigationView();
        handleEvents();
        return view;
    }


    private void bindingViews() {
        drawerLayout = view.findViewById(R.id.drawerLayout);
        imgUser = view.findViewById(R.id.imgUser);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvCountCompletedTasks = view.findViewById(R.id.tvCountCompletedTasks);
        tvCountPendingTasks = view.findViewById(R.id.tvCountPendingTasks);
        btnOptions = view.findViewById(R.id.btnOptions);
        pieChart = view.findViewById(R.id.pieChart);
        navigationView = view.findViewById(R.id.navigationView);
    }

    private void initDatabase() {
        TodoApplication todoApplication = (TodoApplication) getActivity().getApplication();
        database = todoApplication.getDatabase();

        tagChangeListener = new ChildRefEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot) {
                Tag tag = snapshot.getValue(Tag.class);
                if (tag == null)
                    return;
                if (!map.containsKey(tag.getTitle())) {
                    map.put(tag.getTitle(), 0);
                }
                initChart();
                Log.e("EEE", "tagChangeListener onChildAdded");
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
                Tag tag = snapshot.getValue(Tag.class);
                Tag oldTag = (Tag) oldObject;
                String oldTagName = oldTag.getTitle();
                String newTagName = tag.getTitle();
                if (!oldTagName.equals(newTagName)) {
                    int oldTagCount = map.get(oldTagName);
                    map.remove(oldTagName);
                    map.put(newTagName, oldTagCount + 1);
                    initChart();
                    Log.e("EEE", "tagChangeListener onChildChanged");
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot, int position) {
                Tag tag = snapshot.getValue(Tag.class);
                String tagTitle = tag.getTitle();
                if (map.containsKey(tagTitle)) {
                    int countRemoteTasksOfTagRemove = map.get(tagTitle);
                    int countUnClassified = map.get(getString(R.string.unclassified));
                    map.put(getString(R.string.unclassified), countUnClassified + countRemoteTasksOfTagRemove);
                    map.remove(tag.getTitle());
                    initChart();
                    Log.e("EEE", "tagChangeListener onChildRemoved");
                }
            }
        };

        database.TAGS.addValueRefChangeListener(tagChangeListener);

        taskChangeListener = new ChildRefEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                String tagTitle = getTagTitle(task.getTagId());

                if (!map.containsKey(tagTitle)) {
                    tagTitle = getString(R.string.task_classification);
                }
                int count = map.get(tagTitle);
                map.put(tagTitle, count + 1);
                intTaskOverview();
                initChart();
                Log.e("EEE", "taskChangeListener onChildAdded");
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
                Task task = snapshot.getValue(Task.class);
                Task oldTask = (Task) oldObject;

                String tagTitle = getTagTitle(task.getTagId());
                String oldTagTitle = getTagTitle(oldTask.getTagId());

                if (tagTitle.equals(oldTagTitle))
                    return;

                if (!map.containsKey(tagTitle)) {
                    tagTitle = getString(R.string.unclassified);
                }
                int countNewTag = map.get(tagTitle);
                map.put(tagTitle, countNewTag + 1);


                if (!map.containsKey(oldTagTitle)) {
                    oldTagTitle = getString(R.string.unclassified);
                }
                int countOldTag = map.get(oldTagTitle);
                map.put(oldTagTitle, countOldTag - 1);
                intTaskOverview();
                initChart();
                Log.e("EEE", "taskChangeListener onChildChanged");
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot, int position) {
                Task task = snapshot.getValue(Task.class);
                String tagTitle = getTagTitle(task.getTagId());
                if (!map.containsKey(tagTitle)) {
                    tagTitle = getString(R.string.unclassified);
                }
                int count = map.get(tagTitle);
                map.put(tagTitle, count - 1);
                intTaskOverview();
                initChart();
                Log.e("EEE", "taskChangeListener onChildRemoved");
            }
        };

        database.TASKS.addValueRefChangeListener(taskChangeListener);
    }

    private void initData() {
        tasks = database.TASKS.get();
        tags = database.TAGS.get();

        map = new HashMap<>();

        map.put(getString(R.string.unclassified), 0);
        for (int i = 1; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            map.put(tag.getTitle(), 0);
        }

        for (Task task : tasks) {
            String tagTitle = getTagTitle(task.getTagId());

            if (!map.containsKey(tagTitle)) {
                tagTitle = getString(R.string.unclassified);
            }
            int count = map.get(tagTitle);
            count++;
            map.put(tagTitle, count);
        }

        entries = new ArrayList<>();
    }

    private void initUserData() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        tvUsername.setText(user.getDisplayName());
        Glide.with(getContext()).load(user.getPhotoUrl()).error(R.drawable.ic_user_circle).into(imgUser);
    }

    private void intTaskOverview() {
        int totalTasks = tasks.size();
        int countCompletedTasks = database.TASKS.getTaskByType(Task.TypeTask.COMPLETED).size();
        int countPendingTasks = totalTasks - countCompletedTasks;

        tvCountCompletedTasks.setText(String.valueOf(countCompletedTasks));
        tvCountPendingTasks.setText(String.valueOf(countPendingTasks));
    }

    private void initChart() {
        entries.clear();
        int maxRatio = 100;
        float currentRatio = 0f;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            float value = (float) entry.getValue() / tasks.size() * 100;

            if ((int) value == 0)
                continue;

            if (currentRatio + value >= maxRatio) {
                value = maxRatio - currentRatio;
            }
            currentRatio += value;
            entries.add(new PieEntry(value, entry.getKey()));
        }

        if (entries.size() > 0) {
            PieDataSet pieDataSet = new PieDataSet(entries, "");
            pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
            PieData data = new PieData(pieDataSet);

            pieChart.setData(data);
            pieChart.setCenterText(getString(R.string.unit_percent));
            pieChart.setCenterTextColor(getContext().getColor(R.color.textColor));
            pieChart.invalidate();
        } else {
            pieChart.setNoDataText(getString(R.string.data_not_available));
            pieChart.setNoDataTextColor(getContext().getColor(R.color.primaryColor));
        }
    }

    private void initNavigationView() {
        View view;
        CircleImageView imgUser;
        TextView tvUsername, tvEmail;

        view = navigationView.getHeaderView(0);
        imgUser = view.findViewById(R.id.imgUser);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);

        Glide.with(getContext()).load(user.getPhotoUrl()).error(R.drawable.ic_user_circle).into(imgUser);
        tvUsername.setText(user.getDisplayName());
        tvEmail.setText(user.getEmail());
    }


    private void handleEvents() {
        btnOptions.setOnClickListener(v -> showHideNavigationView());
        imgUser.setOnClickListener(v -> showHideNavigationView());
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.changePassword) {
                changePassword();
            } else {
                if (id == R.id.logout) {
                    showConfirmLogoutDialog();
                }
            }
            drawerLayout.close();
            return true;
        });
    }

    private String getTagTitle(String tagId) {
        for (int i = 1; i < tags.size(); i++) {
            Tag tag = tags.get(i);
            if (tag.getId().equals(tagId))
                return tag.getTitle();
        }

        return getString(R.string.unclassified);
    }

    private void changePassword() {
        auth.sendPasswordResetEmail(user.getEmail())
                .addOnCompleteListener(result -> {
                    if (result.isSuccessful()) {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                        builder.setTitle(R.string.change_password);
                        builder.setMessage(R.string.change_password_message);
                        builder.setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss());
                        builder.show();
                    } else {
                        Toast.makeText(getContext(), R.string.change_password_failed, Toast.LENGTH_SHORT).show();
                        Log.e("EEE", result.getException().getMessage());
                    }
                });
    }

    private void showConfirmLogoutDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(R.string.Logout);
        builder.setMessage(R.string.Are_you_sure_you_want_to_logout);
        builder.setPositiveButton(R.string.Logout, (dialog, which) -> logout());
        builder.setNegativeButton(R.string.Cancel, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void logout() {
        TodoApplication application = (TodoApplication) getContext().getApplicationContext();
        application.logout();
        Intent intent = new Intent(getContext(), AuthActivity.class);
        getContext().startActivity(intent);
        getActivity().finish();
    }

    public void showHideNavigationView() {
        if (isOpenNavView())
            drawerLayout.close();
        else
            drawerLayout.open();
    }

    public boolean isOpenNavView() {
        return drawerLayout.isOpen();
    }

    public void closeNavView() {
        drawerLayout.close();
    }
}