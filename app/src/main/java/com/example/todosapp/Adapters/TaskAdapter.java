package com.example.todosapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Activities.Task.UpdateTaskActivity;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Database.TaskDAO;
import com.example.todosapp.Interfaces.FilterByDeadlineDate;
import com.example.todosapp.Interfaces.FilterByTagId;
import com.example.todosapp.Interfaces.Callback;
import com.example.todosapp.Interfaces.ItemTaskCheckedChange;
import com.example.todosapp.Interfaces.ItemTaskClick;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.google.android.gms.tasks.OnCompleteListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> implements FilterByTagId, FilterByDeadlineDate {


    private final Context context;
    private ArrayList<Task> tasks;
    private ArrayList<Task> tempTasks;
    private String typeTask;

    private int positionUndoRemove;
    private Task taskUndoRemove;

    private ItemTaskClick itemTaskClick;
    private ItemTaskCheckedChange itemTaskCheckedChange;
    private Callback finishFilter;

    private String tagId;
    private String deadlineDate;
    private RecyclerView recyclerView;

    public TaskAdapter(Context context, ArrayList<Task> tasks) {
        this.context = context;
        initData(tasks, "");
    }

    public TaskAdapter(Context context, ArrayList<Task> tasks, String typeTask) {
        this.context = context;
        initData(tasks, typeTask);
    }

    private void initData(ArrayList<Task> tasks, String typeTask) {
        this.tasks = tasks;
        this.tempTasks = new ArrayList<>(tasks);
        this.typeTask = typeTask;
        this.positionUndoRemove = -1;
        this.taskUndoRemove = null;
        this.tagId = "";
        this.deadlineDate = "";
        itemTaskClick = (task, position) -> {
            navigateToDetailTask(position);
        };
        itemTaskCheckedChange = (task, position, checked) -> {
            TodoApplication application = (TodoApplication) context.getApplicationContext();
            Database database = application.getDatabase();
            Log.e("EEE", "CHECKED CHANGE: " + task.getTitle());
            database.TASKS.updateTaskCompleteState(task, checked, result -> {
                if (!result.isSuccessful()) {
                    HandleError.checkNetWorkError(context, null);
                }
            }, null);
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        if (tasks == null)
            return 0;
        return tasks.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setTask(tasks.get(position));
        resetEvents(holder);
        initItemView(holder, position);
        handleItemEvent(holder, position);
    }

    private void resetEvents(ViewHolder holder) {
        holder.itemTaskLayout.setOnClickListener(null);
        holder.cbComplete.setOnCheckedChangeListener(null);
    }

    private void initItemView(ViewHolder holder, int position) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        Task task = tasks.get(position);

        holder.cbComplete.setChecked(task.isComplete());
        holder.tvTitleTask.setText(task.getTitle());
        holder.tvDeadline.setText(simpleDateFormat.format(task.getDeadline()));
        holder.imgBranch.setVisibility(View.GONE);
        holder.imgNote.setVisibility(View.GONE);

        if (task.getTodos() != null) {
            if (task.getTodos().size() > 0) {
                holder.imgBranch.setVisibility(View.VISIBLE);
            }
        }
        if (!task.getNote().equals("")) {
            holder.imgNote.setVisibility(View.VISIBLE);
        }

        int flag;
        int textColor;
        int backgroundColor;
        if (task.isComplete()) {
            flag = Paint.STRIKE_THRU_TEXT_FLAG;
            textColor = context.getColor(R.color.itemTodoChecked);
            backgroundColor = context.getColor(R.color.todoBackgroundChecked);
        } else {
            flag = Paint.ANTI_ALIAS_FLAG;
            textColor = context.getColor(R.color.textColor);
            backgroundColor = context.getColor(R.color.todoBackground);
        }
        holder.tvTitleTask.setPaintFlags(flag);
        holder.tvTitleTask.setTextColor(textColor);
        holder.itemTaskLayout.setBackgroundColor(backgroundColor);
    }

    private void handleItemEvent(ViewHolder holder, int position) {
        holder.itemTaskLayout.setOnClickListener(v -> {
            if (itemTaskClick != null) {
                itemTaskClick.onItemTaskClick(holder.getTask(), position);
            }

        });

        holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Task task = tasks.get(position);
            boolean isNotChange = task.isComplete() == isChecked;
            if (isNotChange) {
                return;
            }
            Log.e("CB_ADAPTER", "task id: " + task.getTagId() + " title: " + task.getTitle() + " pos: " + position);
            if (itemTaskCheckedChange != null) {
                itemTaskCheckedChange.onItemCheckedChange(task, position, isChecked);
            }
        });
    }


    public void addTask(Task task, boolean checkAdded) {
        boolean isAdded = false;
        if (checkAdded) {
            for (Task iTask : tasks) {
                if (iTask.equals(task)) {
                    isAdded = true;
                    break;
                }
            }
        }
        if (!isAdded) {
            int pos;
            if (task.isComplete())
                pos = tasks.size();
            else
                pos = TaskDAO.findTheAppropriatePositionTaskToAddByDeadline(tasks, task, 0, tasks.size());
            addTask(pos, task);
        }
    }


    public void updateTask(Task task) {
        boolean validType = !typeTask.equals("");
        boolean sameType = typeTask.equals(task.getTypeTask());

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).equals(task)) {
                if (validType && !sameType) {
                    removeTask(i);
                    return;
                }
                updateTask(i, task);
                return;
            }
        }

        if (validType) {
            if (sameType || (typeTask.equals(Task.TypeTask.COMPLETED) && task.isComplete()))
                addTask(task, false);
        }
    }

    public void removeTask(Task task) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).equals(task)) {
                removeTask(i);
                return;
            }
        }
    }


    public void addTask(int position, Task task) {
        if (validFilterCondition(task)) {
            tasks.add(position, task);
            notifyItemInserted(position);
            notifyItemRangeChanged(position, tasks.size() - position + 1);
            addTempTasks(task, null);
        }
    }

    public void updateTask(int position, Task task) {
        tasks.set(position, task);
        notifyItemChanged(position);
        updateTempTasks(task, null);
    }

    public void removeTask(int position) {
        if (position < 0 || position >= tasks.size())
            return;
        positionUndoRemove = position;
        taskUndoRemove = tasks.get(position);
        tasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tasks.size() - position);
        removeItemTempTasks(taskUndoRemove, null);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void undoRemoveTask() {
        if (positionUndoRemove != -1 && taskUndoRemove != null) {
            addTask(positionUndoRemove, taskUndoRemove);
            clearUndoTask();
        }
    }

    public Task getTaskUndoRemove() {
        return taskUndoRemove;
    }

    public void clearUndoTask() {
        taskUndoRemove = null;
        positionUndoRemove = -1;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(ArrayList<Task> tasks) {
        this.tasks.clear();
        this.tasks = tasks;
        this.tempTasks = tasks;
        recyclerView.post(this::notifyDataSetChanged);
    }

    public void setItemTaskClick(ItemTaskClick itemTaskClick) {
        this.itemTaskClick = itemTaskClick;
    }

    public void setItemTaskCheckedChange(ItemTaskCheckedChange itemTaskCheckedChange) {
        this.itemTaskCheckedChange = itemTaskCheckedChange;
    }

    public void navigateToDetailTask(int position) {
        Task task = tasks.get(position);
        Intent intent = new Intent(context, UpdateTaskActivity.class);
        intent.putExtra("task", task);
        Log.e(HandleError.tag, task.toString());
        context.startActivity(intent);
    }

    public void addTempTasks(Task task, Callback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            for (Task iTask : tempTasks) {
                if (iTask.equals(task))
                    return;
            }
            int pos;
            if (task.isComplete())
                pos = tempTasks.size();
            else
                pos = TaskDAO.findTheAppropriatePositionTaskToAddByDeadline(tempTasks, task, 0, tempTasks.size());

            tempTasks.add(pos, task);
            if (callback != null) {
                callback.callback();
            }
        });
        executorService.shutdown();
    }

    public void updateTempTasks(Task task, Callback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            for (int i = 0; i < tempTasks.size(); i++) {
                if (tempTasks.get(i).equals(task)) {
                    tempTasks.set(i, task);
                    if (callback != null) {
                        callback.callback();
                    }
                    break;
                }
            }
        });
        executorService.shutdown();
    }

    public void removeItemTempTasks(Task task, Callback callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            for (int i = 0; i < tempTasks.size(); i++) {
                if (tempTasks.get(i).equals(task)) {
                    tempTasks.remove(i);
                    if (callback != null) {
                        callback.callback();
                    }
                    return;
                }
            }
        });
        executorService.shutdown();

    }

    public void setFinishFilter(Callback finishFilter) {
        this.finishFilter = finishFilter;
    }

    @Override
    public Filter getFilterByTagId() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                tagId = (String) constraint;
                if (tagId.equals(""))
                    tasks = tempTasks;
                else {
                    ArrayList<Task> resultTasks = new ArrayList<>();
                    for (Task task : tempTasks) {
                        if (task.getTagId().equals(tagId))
                            resultTasks.add(task);
                    }
                    tasks = resultTasks;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = tasks;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                recyclerView.post(() -> notifyDataSetChanged());
                if (finishFilter != null) {
                    finishFilter.callback();
                }
            }
        };
    }

    @Override
    public Filter getFilterByDeadlineDate() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                deadlineDate = (String) constraint;
                Log.e("AAA", deadlineDate);
                if (deadlineDate.equals(""))
                    tasks = tempTasks;
                else {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    ArrayList<Task> resultTasks = new ArrayList<>();
                    for (Task task : tempTasks) {
                        String strDeadline = sdf.format(task.getDeadline());
                        Log.e("AAA", strDeadline);
                        if (strDeadline.equals(deadlineDate))
                            resultTasks.add(task);
                    }
                    tasks = resultTasks;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = tasks;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                recyclerView.post(() -> notifyDataSetChanged());
                if (finishFilter != null) {
                    finishFilter.callback();
                }
            }
        };
    }

    private boolean haveFilterCondition() {
        return deadlineDate.equals("") && tagId.equals("");
    }

    private boolean validFilerConditionByDeadline(Task task) {
        if (deadlineDate.equals(""))
            return true;

        if (task == null)
            return false;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String strDeadline = sdf.format(task.getDeadline());
        return strDeadline.equals(deadlineDate);
    }

    private boolean validFilterConditionByTag(Task task) {
        if (tagId.equals(""))
            return true;
        if (task == null)
            return false;

        return task.getTagId().equals(tagId);
    }

    private boolean validFilterCondition(Task task) {
        return validFilerConditionByDeadline(task) && validFilterConditionByTag(task);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout itemTaskLayout;
        CheckBox cbComplete;
        TextView tvTitleTask, tvDeadline;
        ImageView imgBranch, imgNote;

        Task task;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTaskLayout = itemView.findViewById(R.id.itemTaskLayout);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            tvTitleTask = itemView.findViewById(R.id.tvTitleTask);
            tvDeadline = itemView.findViewById(R.id.tvDeadline);
            imgBranch = itemView.findViewById(R.id.imgBranch);
            imgNote = itemView.findViewById(R.id.imgNote);
        }

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }
    }
}
