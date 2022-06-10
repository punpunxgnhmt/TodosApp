package com.example.todosapp.Fragments.Main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Adapters.TaskAdapter;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Interfaces.Callback;
import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;
import com.google.firebase.database.DataSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CalendarFragment extends Fragment {


    View view;
    CalendarView calendarView;
    LinearLayout layoutNotifyNoTask;
    RecyclerView rvTasks;

    ArrayList<Task> tasks;
    TaskAdapter adapter;

    Date selectedDate;

    Database database;
    ChildRefEventListener childRefEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        bindingViews();
        initData();
        handleEvents();
        showListTask();
        return view;
    }


    private void bindingViews() {
        calendarView = view.findViewById(R.id.calendarView);
        layoutNotifyNoTask = view.findViewById(R.id.layoutNotifyNoTask);
        rvTasks = view.findViewById(R.id.rvTasks);
    }

    private void initData() {
        TodoApplication application = (TodoApplication) getActivity().getApplication();
        database = application.getDatabase();
        childRefEventListener = new ChildRefEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                if (task == null)
                    return;
                adapter.addTempTasks(task, () -> {
                    getActivity().runOnUiThread(() -> filterDate());
                });
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, int position, Object oldTask) {
                Task task = snapshot.getValue(Task.class);
                if (task == null)
                    return;

                adapter.updateTempTasks(task, () -> {
                    getActivity().runOnUiThread(() -> filterDate());
                });
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot, int position) {
                Task task = snapshot.getValue(Task.class);
                if (task == null)
                    return;
                adapter.removeItemTempTasks(task, () -> {
                    getActivity().runOnUiThread(() -> filterDate());
                });
            }
        };
        database.TASKS.addValueRefChangeListener(childRefEventListener);
        selectedDate = new Date();
        calendarView.setDate(selectedDate.getTime());
    }

    private void handleEvents() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0 ,0, 0);
            Date newSelectedDate = calendar.getTime();
            if (newSelectedDate.compareTo(selectedDate) != 0) {
                selectedDate = newSelectedDate;
                filterDate();
            }
        });
    }


    private void showListTask() {
        tasks = new ArrayList<>(database.TASKS.get());
        adapter = new TaskAdapter(getContext(), tasks);
        adapter.setFinishFilter(this::checkTasksEmpty);
        rvTasks.setAdapter(adapter);
        rvTasks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        filterDate();
    }

    private void checkTasksEmpty() {
        if (adapter.getItemCount() == 0) {
            layoutNotifyNoTask.setVisibility(View.VISIBLE);
            rvTasks.setVisibility(View.GONE);
        } else {
            layoutNotifyNoTask.setVisibility(View.GONE);
            rvTasks.setVisibility(View.VISIBLE);
        }
    }

    private void filterDate(){
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        adapter.getFilterByDeadlineDate().filter(sdf.format(selectedDate));
    }

    public Date getSelectedDate() {
        return selectedDate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.TASKS.removeValueRefChangeListener(childRefEventListener);
    }
}