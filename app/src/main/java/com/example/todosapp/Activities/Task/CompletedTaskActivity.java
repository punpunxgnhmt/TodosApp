package com.example.todosapp.Activities.Task;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Adapters.CompletedTaskAdapter;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;
import com.example.todosapp.Utils.Tools;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity is used for display all completed tasks of user.
 *
 * */

public class CompletedTaskActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView tvNotifyNoTask, tvInfo;
    RecyclerView rvCompletedTasks;

    ArrayList<Task> tasks;

    // divide task base on completed date
    Map<Long, ArrayList<Task>> tasksGroupByDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_task);
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
        bindingViews();
        setToolbar();
        getCompletedTasks();
    }

    private void bindingViews() {
        toolbar = findViewById(R.id.toolbar);
        tvNotifyNoTask = findViewById(R.id.tvNotifyNoTask);
        tvInfo = findViewById(R.id.tvInfo);
        rvCompletedTasks = findViewById(R.id.rvCompletedTasksGroup);
    }

    private void setToolbar() {
        toolbar.setTitle(R.string.completed_task);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void getCompletedTasks() {

        // get completed tasks
        Intent intent = getIntent();
        if (intent.hasExtra("completedTasks")) {
            tasks = (ArrayList<Task>) intent.getSerializableExtra("completedTasks");
        }

        // if completed tasks empty or null display message
        if (tasks == null) {
            notifyNoCompletedTask();
            return;
        }
        if (tasks.size() == 0) {
            notifyNoCompletedTask();
            return;
        }

        // if not, group tasks and show it.
        groupTasksByDate();
        showCompletedTasks();
    }

    private void notifyNoCompletedTask() {
        // hide the completed tasks list and show the message to notify no task completed.
        tvNotifyNoTask.setVisibility(View.VISIBLE);
        rvCompletedTasks.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
    }

    // loop the completed Tasks and group each task by completed date.
    private void groupTasksByDate() {
        tasksGroupByDate = new HashMap<>();
        for (Task task : tasks) {
            chooseGroup(task);
        }
    }

    private void chooseGroup(Task task){
        // the completed date will be covert to long, the it will becomes a key of map.

        // remove the time of date, because we need only date to sort task.
        Date date = Tools.removeTime(task.getCompletedDate());

        //convert date to long number and get task list from map
        ArrayList<Task> groupTask = tasksGroupByDate.get(date.getTime());

        // if list is null -> the date not exist in map
        // so create a new list and put it to map with the key above
        if (groupTask == null) {
            groupTask = new ArrayList<>();
            tasksGroupByDate.put(date.getTime(), groupTask);
        }
        //add the task to list
        groupTask.add(task);
    }


    // this method to rendering completed Tasks.
    private void showCompletedTasks() {
        CompletedTaskAdapter adapter = new CompletedTaskAdapter(this, tasksGroupByDate);
        rvCompletedTasks.setAdapter(adapter);
        rvCompletedTasks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.from_right, R.anim.to_left);
    }
}