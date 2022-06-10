package com.example.todosapp.Adapters;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Activities.Task.CompletedTaskActivity;
import com.example.todosapp.Activities.Task.UpdateTaskActivity;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.Helper.TaskAdapterItemTouchHelper;
import com.example.todosapp.Interfaces.ItemTaskClick;
import com.example.todosapp.Models.Task;
import com.example.todosapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * this adapter will be rendering completed task in completed task activity
 */
public class CompletedTaskAdapter extends RecyclerView.Adapter<CompletedTaskAdapter.ViewHolder> {

    CompletedTaskActivity context;
    Map<Long, ArrayList<Task>> map;
    List<Long> dates;

    RecyclerView recyclerView;

    TaskAdapter selectedAdapter;
    int posTaskSelected;
    int posGroupSelected;


    public CompletedTaskAdapter(CompletedTaskActivity context, Map<Long, ArrayList<Task>> map) {
        this.context = context;
        this.map = map;
        dates = new ArrayList<>();
        // this map is groups of tasks which are divided base on completed date
        Map<Long, ArrayList<Task>> treeMap = new TreeMap<>(map);

        //get list keys with long type to convert to a date.
        for (Map.Entry<Long, ArrayList<Task>> entry : treeMap.entrySet()) {
            dates.add(entry.getKey());
        }

        updateTaskResult = context.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent == null)
                            return;

                        if (intent.hasExtra("update")) {
                            Task task = (Task) intent.getSerializableExtra("update");
                            if (selectedAdapter != null) {
                                selectedAdapter.updateTask(posTaskSelected, task);
                            }
                            return;
                        }

                        if (intent.hasExtra("delete")) {
                            if (selectedAdapter != null) {
                                selectedAdapter.removeTask(posTaskSelected);

                                // check task group empty
                                if (selectedAdapter.getItemCount() == 0) {
                                    removeItem(posGroupSelected);
                                }
                            }
                        }
                    }
                }
        );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_completed_tasks, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Long time = dates.get(position);
        ArrayList<Task> tasks = map.get(time);

        if (tasks.size() == 0) {
            removeItem(position);
            return;
        }
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date(time);


        if (position == 0) {
            holder.vFirstGroup.setVisibility(View.VISIBLE);
        }

        holder.tvDateGroup.setText(sdf.format(date));

        TaskAdapter adapter = new TaskAdapter(context, tasks);

        // listen item tag completed state change
        adapter.setItemTaskCheckedChange((task, pos, checked) -> {
            if (!checked) {
                Log.e("EEE", "completed adapter call update ..." + task);
                // show dialog
                ProgressDialog.showDialog(context);

                // get db
                TodoApplication application = (TodoApplication) context.getApplicationContext();
                Database database = application.getDatabase();

                //call update
                database.TASKS.updateTaskCompleteState(task, checked, res -> {

                    // if success
                    if (res.isSuccessful()) {
                        // Remove Task
                        adapter.removeTask(pos);

                        // check task group empty
                        if (tasks.size() == 0) {
                            removeItem(position);
                        }

                    }
                    // if failure
                    else {
                        Toast.makeText(context, R.string.update_failure, Toast.LENGTH_SHORT).show();
                    }

                    // hide dialog
                    ProgressDialog.hideDialog();
                });
            }
        });

        adapter.setItemTaskClick((task, pos) -> {
            selectedAdapter = adapter;
            posTaskSelected = pos;
            posGroupSelected = position;
            Intent intent = new Intent(context, UpdateTaskActivity.class);
            intent.putExtra("task", task);
            updateTaskResult.launch(intent);
        });

        holder.rvCompletedTasks.setAdapter(adapter);
        holder.rvCompletedTasks.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public void removeItem(int position) {
        if (position < 0 || position >= dates.size())
            return;
        dates.remove(position);
        recyclerView.post(() -> {
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, dates.size() - position);
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    private final ActivityResultLauncher<Intent> updateTaskResult;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View vFirstGroup;
        TextView tvDateGroup;
        RecyclerView rvCompletedTasks;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vFirstGroup = itemView.findViewById(R.id.vFirstGroup);
            tvDateGroup = itemView.findViewById(R.id.tvDateGroup);
            rvCompletedTasks = itemView.findViewById(R.id.rvCompletedTasks);
        }
    }
}
