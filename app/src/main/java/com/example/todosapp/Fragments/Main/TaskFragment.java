package com.example.todosapp.Fragments.Main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.todosapp.Activities.TagManageActivity;
import com.example.todosapp.Activities.Task.CompletedTaskActivity;
import com.example.todosapp.Adapters.TagAdapter;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;
import com.example.todosapp.Views.TaskLayout;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;


public class TaskFragment extends Fragment {

    // UI Element
    View view;
    RecyclerView rvTag;
    Toolbar toolbar;
    LinearLayout layoutGroupContainer, layoutNotifyGroupEmpty;
    TaskLayout pastTasks, todayTasks, futureTasks, todayCompletedTasks, completedTasks;
    MaterialButton btnViewCompletedTask;

    // Adapter
    TagAdapter tagAdapter;

    // Data
    Database database;
    ArrayList<Tag> tags;
    ArrayList<Task> tasks;
    String selectedTagId;
    int countFilterRequestsFinish;

    ChildRefEventListener tasksChangeEvent, tagsChangeEvent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task, container, false);
        getDataUser();
        bindingViews();
        createTagOptions();
        showTags();
        showTasksByType();
        handleValueChange();
        handleEvents();
        return view;
    }

    private void handleEvents() {
        btnViewCompletedTask.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CompletedTaskActivity.class);
            ArrayList<Task> tasks = completedTasks.getTasks();
            intent.putExtra("completedTasks", tasks);
            startActivity(intent);
        });
    }


    private void getDataUser() {
        TodoApplication application = (TodoApplication) getActivity().getApplication();
        database = application.getDatabase();
        tags = database.TAGS.get();
        this.selectedTagId = "";
    }

    private void bindingViews() {
        rvTag = view.findViewById(R.id.rvTag);
        toolbar = view.findViewById(R.id.toolbar);
        layoutGroupContainer = view.findViewById(R.id.layoutGroupContainer);
        layoutNotifyGroupEmpty = view.findViewById(R.id.layoutNotifyGroupEmpty);
        pastTasks = view.findViewById(R.id.pastTasks);
        todayTasks = view.findViewById(R.id.todayTasks);
        futureTasks = view.findViewById(R.id.futureTasks);
        todayCompletedTasks = view.findViewById(R.id.todayCompletedTasks);
        completedTasks = view.findViewById(R.id.completedTasks);
        btnViewCompletedTask = view.findViewById(R.id.btnViewCompletedTask);
    }

    private void handleValueChange() {
        tagsChangeEvent = new ChildRefEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot) {
                tagAdapter.itemAdded();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
                tagAdapter.itemChanged(position);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot, int position) {
                tagAdapter.itemRemoved(position);
            }
        };
        database.TAGS.addValueRefChangeListener(tagsChangeEvent);

        tasksChangeEvent = new ChildRefEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot) {
                checkLayoutListEmpty();
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
                checkLayoutListEmpty();
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot, int position) {
                checkLayoutListEmpty();
            }
        };
        database.TASKS.addValueRefChangeListener(tasksChangeEvent);


    }

    private void createTagOptions() {
        toolbar.inflateMenu(R.menu.menu_task_fragment);
        toolbar.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(getContext(), TagManageActivity.class);
            getContext().startActivity(intent);
            return true;
        });
    }

    private void showTags() {
        tagAdapter = new TagAdapter(getContext(), tags, (tag, position) -> {
            String tagId = position == 0 ? "" : tag.getId();
            if (tagId.equals(selectedTagId))
                return;
            filterByTagId(tagId);
        });
        rvTag.setAdapter(tagAdapter);
        rvTag.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    private void filterByTagId(String tagId) {
        Log.e("EEE", "filterByTagId: " + tagId);
        selectedTagId = tagId;
        countFilterRequestsFinish = 0;
        pastTasks.filterByTagId(selectedTagId, this::finishFilter);
        todayTasks.filterByTagId(selectedTagId, this::finishFilter);
        futureTasks.filterByTagId(selectedTagId, this::finishFilter);
        todayCompletedTasks.filterByTagId(selectedTagId, this::finishFilter);
        completedTasks.filterByTagId(selectedTagId, this::finishFilter);
    }

    private void showTasksByType() {
        pastTasks.setInfo(Task.TypeTask.PAST_UNCOMPLETED, R.string.before);
        todayTasks.setInfo(Task.TypeTask.TODAY_UNCOMPLETED, R.string.today);
        futureTasks.setInfo(Task.TypeTask.FUTURE_UNCOMPLETED, R.string.future);
        todayCompletedTasks.setInfo(Task.TypeTask.TODAY_COMPLETED, R.string.finish_today);
        completedTasks.setInfo(Task.TypeTask.COMPLETED, R.string.completed);
        checkLayoutListEmpty();
    }

    private void finishFilter() {
        int maxRequests = 5;
        countFilterRequestsFinish++;
        if (countFilterRequestsFinish >= maxRequests) {
            checkLayoutListEmpty();
        }
    }

    private void checkLayoutListEmpty() {

        boolean allGroupEmpty = pastTasks.isEmpty()
                && todayTasks.isEmpty()
                && futureTasks.isEmpty()
                && todayCompletedTasks.isEmpty()
                && completedTasks.isEmpty();
        if (allGroupEmpty) {
            layoutGroupContainer.setVisibility(View.GONE);
            layoutNotifyGroupEmpty.setVisibility(View.VISIBLE);
        } else {
            layoutGroupContainer.setVisibility(View.VISIBLE);
            layoutNotifyGroupEmpty.setVisibility(View.GONE);
        }
    }

    public String getSelectedTagId() {
        return selectedTagId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.TASKS.removeValueRefChangeListener(tasksChangeEvent);
        database.TAGS.removeValueRefChangeListener(tagsChangeEvent);
    }
}