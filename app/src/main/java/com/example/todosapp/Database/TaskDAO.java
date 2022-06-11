package com.example.todosapp.Database;

import static com.example.todosapp.Utils.FirebaseConstants.timeOut;

import android.annotation.SuppressLint;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todosapp.Interfaces.Callback;
import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Interfaces.FindTask;
import com.example.todosapp.Models.Task;
import com.example.todosapp.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * this class is used for interaction with database about tasks.
 * */



public class TaskDAO {
    private DatabaseReference reference;
    private ArrayList<Task> tasks;

    /**
     * #tasksByType: this is a map to save list of list task base on completed state of task
     *  if the deadline of task is the past, and this task not completed -> this task is in group: Past_UNCOMPLETED
     *  if the deadline of task is to day, and this task not completed -> this task is in group: TODAY_UNCOMPLETED
     *  if the deadline of task is in future, and this task not completed -> this task is in group: FUTURE_UNCOMPLETED
     *  if the task is completed and the completed day is today -> this task in group: TODAY_COMPLETED
     *  if the task is completed, this task in group COMPLETED,so Group COMPLETED have TODAY_COMPLETED.
     * */
    Map<String, ArrayList<Task>> tasksByType;


    private ChildEventListener eventListener;
    private ArrayList<ChildRefEventListener> callbacks;

    public TaskDAO(DatabaseReference taskReference, User user) {
        this.reference = taskReference;
        this.tasks = user.getTasks();
        this.callbacks = new ArrayList<>();
        groupTasksByType();
        handleTaskChangeListener();
    }

    // other object register value change
    public void addValueRefChangeListener(ChildRefEventListener callback) {
        if (callback != null) {
            if (!callbacks.contains(callback)) {
                callbacks.add(callback);
            }
        }
    }

    // remove listen value change
    public void removeValueRefChangeListener(ChildRefEventListener callback) {
        if (callback != null && callbacks != null) {
            callbacks.remove(callback);
        }
    }

    private void handleTaskChangeListener() {
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Task task = snapshot.getValue(Task.class);
                // when new task added in db
                // check this task exist in device
                for (int i = 0; i < tasks.size(); i++) {
                    Task iTask = tasks.get(i);
                    if (task.getId().equals(iTask.getId())) {
                        return;
                    }
                }
                // if not exist add it to list task
                tasks.add(task);

                // send notification
                for (ChildRefEventListener callback : callbacks) {
                    callback.onChildAdded(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Task task = snapshot.getValue(Task.class);
                for (int i = 0; i < tasks.size(); i++) {
                    Task iTask = tasks.get(i);
                    if (task.getId().equals(iTask.getId())) {
                        Task oldTask = tasks.get(i);
                        tasks.remove(i);
                        tasks.add(i, task);
                        for (ChildRefEventListener callback : callbacks) {
                            callback.onChildChanged(snapshot, i, oldTask);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Task task = snapshot.getValue(Task.class);
                for (int i = 0; i < tasks.size(); i++) {
                    Task iTask = tasks.get(i);
                    if (task.getId().equals(iTask.getId())) {
                        tasks.remove(i);
                        for (ChildRefEventListener callback : callbacks) {
                            callback.onChildRemoved(snapshot, i);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        reference.addChildEventListener(eventListener);
    }

    public void groupTasksByType() {
        if (tasksByType != null) {
            tasksByType.clear();
        } else
            tasksByType = new HashMap<>();

        tasksByType.put(Task.TypeTask.COMPLETED, new ArrayList<>());
        tasksByType.put(Task.TypeTask.TODAY_COMPLETED, new ArrayList<>());
        tasksByType.put(Task.TypeTask.TODAY_UNCOMPLETED, new ArrayList<>());
        tasksByType.put(Task.TypeTask.PAST_UNCOMPLETED, new ArrayList<>());
        tasksByType.put(Task.TypeTask.FUTURE_UNCOMPLETED, new ArrayList<>());

        for (Task task : tasks) {
            String typeTask = task.getTypeTask();
            ArrayList<Task> arrayList = tasksByType.get(typeTask);
            int position = task.isComplete() ?
                    findTheAppropriatePositionTaskToAddByCompletedDate(arrayList, task, 0, arrayList.size()) :
                    findTheAppropriatePositionTaskToAddByDeadline(arrayList, task, 0, arrayList.size());
            arrayList.add(position, task);

            if (typeTask.equals(Task.TypeTask.TODAY_COMPLETED)) {
                ArrayList<Task> completedTasks = tasksByType.get(Task.TypeTask.COMPLETED);
                int pos = findTheAppropriatePositionTaskToAddByCompletedDate(completedTasks, task, 0, completedTasks.size());
                completedTasks.add(pos, task);
            }
        }
        tasksByType.get(Task.TypeTask.COMPLETED);
    }


    public ArrayList<Task> getTaskByType(String type) {
        return tasksByType.get(type);
    }


    public void add(Task task, OnCompleteListener<Void> callback, Callback timeoutCallback) {
        AtomicBoolean returnedResult = new AtomicBoolean(false);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(!returnedResult.get()){
                if(timeoutCallback != null){
                    timeoutCallback.callback();
                }
            }
        }, timeOut);

        String id = reference.push().getKey();
        task.setId(id);
        reference.child(id).setValue(task)
            .addOnCompleteListener(callback)
            .addOnCompleteListener(result -> returnedResult.set(true));
    }

    public void updateTaskCompleteState(Task task, boolean isCompleted, OnCompleteListener<Void> callback, Callback timeoutCallback) {
        Date completedDate = isCompleted ? new Date() : task.getCompletedDate();
        task.setComplete(isCompleted);
        task.setCompletedDate(completedDate);
        update(task, callback, timeoutCallback);
    }

    public void update(Task task, OnCompleteListener<Void> callback, Callback timeoutCallback) {
        AtomicBoolean returnedResult = new AtomicBoolean(false);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(!returnedResult.get()){
                if(timeoutCallback != null){
                    timeoutCallback.callback();
                }
            }
        }, timeOut);
        reference.child(task.getId()).setValue(task)
            .addOnCompleteListener(callback)
            .addOnCompleteListener(result -> returnedResult.set(true));
    }

    public void delete(Task task, OnCompleteListener<Void> callback, Callback timeoutCallback) {
        AtomicBoolean returnedResult = new AtomicBoolean(false);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(!returnedResult.get()){
                if(timeoutCallback != null){
                    timeoutCallback.callback();
                }
            }
        }, timeOut);
        reference.child(task.getId()).removeValue()
            .addOnCompleteListener(callback)
            .addOnCompleteListener(result -> returnedResult.set(true));
    }

    public ArrayList<Task> find(FindTask condition) {
        ArrayList<Task> resultTasks = new ArrayList<>();
        if (condition == null) {
            return tasks;
        }
        for (Task task : tasks) {
            if (condition.getConditions(task)) {
                resultTasks.add(task);
            }
        }
        return resultTasks;
    }

    public ArrayList<Task> findByDeadlineDate(Date date) {
        ArrayList<Task> result = new ArrayList<>();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String strTarget = sdf.format(date);
        for (Task task : tasks) {
            Date deadlineDate = task.getDeadline();
            String strDeadline = sdf.format(deadlineDate);
            if (strDeadline.equals(strTarget))
                result.add(task);
        }
        return result;
    }


    // get position base on deadline of task to add to a list,
    public static int findTheAppropriatePositionTaskToAddByDeadline(ArrayList<Task> tasks, Task task, int start, int end) {
        try {
            if (start == end)
                return end;

            int mid = (int) (start + end) / 2;

            Task midTask = tasks.get(mid);

            if (midTask.getDeadline().compareTo(task.getDeadline()) <= 0) {
                if (mid == end - 1)
                    return end;
                Task afterMidTask = tasks.get(mid + 1);

                if (afterMidTask.getDeadline().compareTo(task.getDeadline()) > 0)
                    return mid + 1;

                return findTheAppropriatePositionTaskToAddByDeadline(tasks, task, mid, end);
            } else {
                if (mid == start)
                    return start;
                Task beforeMidTask = tasks.get(mid - 1);

                if (beforeMidTask.getDeadline().compareTo(task.getDeadline()) <= 0)
                    return mid;

                return findTheAppropriatePositionTaskToAddByDeadline(tasks, task, start, mid);
            }
        } catch (Exception e) {
            return end;
        }
    }

    // get position base on completed task to add to a list,
    // this method usually call to add a task to list of completed task
    public static int findTheAppropriatePositionTaskToAddByCompletedDate(ArrayList<Task> tasks, Task task, int start, int end) {
        try {
            if (start == end)
                return end;

            int mid = (start + end) / 2;

            Task midTask = tasks.get(mid);

            if (midTask.getCompletedDate().compareTo(task.getCompletedDate()) <= 0) {
                if (mid == end - 1)
                    return end;
                Task afterMidTask = tasks.get(mid + 1);

                if (afterMidTask.getCompletedDate().compareTo(task.getCompletedDate()) > 0)
                    return mid + 1;

                return findTheAppropriatePositionTaskToAddByDeadline(tasks, task, mid, end);
            } else {
                if (mid == start)
                    return start;
                Task beforeMidTask = tasks.get(mid - 1);

                if (beforeMidTask.getCompletedDate().compareTo(task.getCompletedDate()) <= 0)
                    return mid;

                return findTheAppropriatePositionTaskToAddByDeadline(tasks, task, start, mid);
            }

        } catch (Exception e) {
            return end;
        }
    }

    public void clear() {
        reference.removeEventListener(eventListener);
        reference = null;
        tasks.clear();
        tasks = null;
        callbacks.clear();
        callbacks = null;
    }

    public ArrayList<Task> get() {
        return tasks;
    }
}