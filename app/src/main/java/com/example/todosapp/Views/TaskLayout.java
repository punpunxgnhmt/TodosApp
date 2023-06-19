package com.example.todosapp.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Adapters.TaskAdapter;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Helper.TaskAdapterItemTouchHelper;
import com.example.todosapp.Interfaces.Callback;
import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskLayout extends FrameLayout implements ChildRefEventListener{
    CheckBox cbShowList;
    RecyclerView rvList;


    Database database;

    ArrayList<Task> tasks;
    TaskAdapter adapter;
    String typeTask;
    Snackbar snackbar;

    public TaskLayout(Context context) {
        super(context);
        initView();
    }

    public TaskLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
        initView();
    }

    private void initData() {
        TodoApplication application = (TodoApplication) getContext().getApplicationContext();
        database = application.getDatabase();

//        childRefEventListener = new ChildRefEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot snapshot) {
//                Task task = snapshot.getValue(Task.class);
//                if (task == null)
//                    return;
//                addTask(task);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
//                Task task = snapshot.getValue(Task.class);
//                if (task == null)
//                    return;
//                updateTask(task);
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot snapshot, int position) {
//                Task task = snapshot.getValue(Task.class);
//                if (task == null)
//                    return;
//                removeTask(task);
//            }
//        };

        database.TASKS.addValueRefChangeListener(this);
    }

    private void initView() {
        inflate(getContext(), R.layout.layout_task_group, this);
        cbShowList = this.findViewById(R.id.cbShowList);
        rvList = this.findViewById(R.id.rvListTask);
        handleEvents();
    }

    private void handleEvents() {
        cbShowList.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int visible = isChecked ? VISIBLE : GONE;
            rvList.setVisibility(visible);
        });
    }

    public void setInfo(String typeTask, String title) {
        setTypeTask(typeTask);
        setTitle(title);
    }

    public void setInfo(String typeTask, @StringRes int resId) {
        setTypeTask(typeTask);
        setTitle(resId);
    }

    public void setTypeTask(String typeTask) {
        this.typeTask = typeTask;
        if (database != null) {
            ArrayList<Task> tasks = database.TASKS.getTaskByType(typeTask);
            setTasks(tasks);
        }
    }

    public void setTitle(String title) {
        cbShowList.setText(title);
    }

    public void setTitle(@StringRes int resId) {
        cbShowList.setText(resId);
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        adapter = new TaskAdapter(getContext(), tasks, typeTask);
        rvList.setAdapter(adapter);
        rvList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        ItemTouchHelper.SimpleCallback swipeRightItemCallback = new TaskAdapterItemTouchHelper(0, ItemTouchHelper.RIGHT, viewHolder -> {
            int position = viewHolder.getAdapterPosition();
            adapter.removeTask(position);
            Task task = adapter.getTaskUndoRemove();
            showUndoRemoveTaskSnackBar(task);
        });
        ItemTouchHelper deleteSwipe = new ItemTouchHelper(swipeRightItemCallback);
        deleteSwipe.attachToRecyclerView(rvList);
        checkListEmpty();
    }

    private void checkListEmpty() {
        if (isEmpty()) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
    }


    public void addTask(Task task) {
        if (task.getTypeTask().equals(typeTask)) {
            adapter.addTask(task, true);
            checkListEmpty();
        }
    }

    public void updateTask(Task task) {
        adapter.updateTask(task);
        checkListEmpty();
    }

    public void removeTask(Task task) {
        if (task.getTypeTask().equals(typeTask)) {
            adapter.removeTask(task);
            checkListEmpty();
        }
    }

    private void showUndoRemoveTaskSnackBar(Task task) {
        AtomicBoolean dismissItSelf = new AtomicBoolean(true);
        snackbar = Snackbar.make(this, R.string.you_just_deleted_a_todo, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.undo, v -> {
            adapter.undoRemoveTask();
            dismissItSelf.set(false);
        });
        snackbar.setActionTextColor(getContext().getColor(R.color.primaryColor));
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (dismissItSelf.get()) {
                    adapter.clearUndoTask();
                    database.TASKS.delete(task, res -> {
                        if(!res.isSuccessful()){
                            HandleError.checkNetWorkError(getContext(), res.getException());
                        }
                    }, null);
                }
            }
        });
        snackbar.show();

    }

    public void filterByTagId(String tagId, Callback finishFilter) {
        adapter.setFinishFilter(() -> {
            checkListEmpty();
            if (finishFilter != null) {
                finishFilter.callback();
            }
        });
        adapter.getFilterByTagId().filter(tagId);
        if (snackbar != null)
            snackbar.dismiss();

    }

    public boolean isEmpty() {
        if (adapter == null)
            return true;
        return adapter.getItemCount() == 0;
    }

    public ArrayList<Task> getTasks() {
        return adapter.getTasks();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        database.TASKS.removeValueRefChangeListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot snapshot) {
        Task task = snapshot.getValue(Task.class);
        if (task == null)
            return;
        addTask(task);
    }

    @Override
    public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
        Task task = snapshot.getValue(Task.class);
        if (task == null)
            return;
        updateTask(task);
    }

    @Override
    public void onChildRemoved(DataSnapshot snapshot, int position) {
        Task task = snapshot.getValue(Task.class);
        if (task == null)
            return;
        removeTask(task);
    }
}
