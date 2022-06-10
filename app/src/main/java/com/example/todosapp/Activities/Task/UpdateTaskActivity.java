package com.example.todosapp.Activities.Task;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.Models.Todo;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * This activity extends from Task activity
 * and is used for update a task.
 *
 *
 * */
public class UpdateTaskActivity extends TaskActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initData() {
        super.initData();
        getTask();
    }

    // get the task which user want to update
    private void getTask() {
        Intent intent = getIntent();
        if (intent.hasExtra("task")) {
            this.task = (Task) intent.getSerializableExtra("task");
        }
    }

    @Override
    protected void bidingViews() {
        super.bidingViews();
        bindingDataToViews();
    }

    // set data of task to display
    private void bindingDataToViews() {
        edtTask.setText(task.getTitle());
        edtNote.setText(task.getNote());
    }

    @Override
    protected void setUpToolBar() {
        super.setUpToolBar();
        getSupportActionBar().setTitle(R.string.UdapteTask);
    }



    // this method will be called when user choose update in toolbar
    @Override
    protected void finishTask() {
        // check title valid
        String error = task.isValid(this);
        if (!error.equals("")) {
            messageLayout.addErrorMessage(error);
            return;
        }

        // if the task is valid
        // show the progressbar
        ProgressDialog.showDialog(this);

        // save todos
        for (int i = 0; i < task.getTodos().size(); i++) {
            Todo todo = task.getTodos().get(i);
            todo.setId(String.valueOf(i));
        }

        // call firebase to update
        database.TASKS.update(task, taskResult -> {
            if (taskResult.isSuccessful()) {
                Intent intent = new Intent();
                intent.putExtra("update", task);
                setResult(RESULT_OK, intent);
                Toast.makeText(this, R.string.update_successfully, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.update_failure, Toast.LENGTH_SHORT).show();
                HandleError.checkNetWorkError(UpdateTaskActivity.this, taskResult.getException());
                setResult(RESULT_CANCELED);
            }
            UpdateTaskActivity.this.finish();
            ProgressDialog.hideDialog();
        });
    }

    // this method will be called when user click delete in toolbar
    @Override
    protected void deleteTask() {
        //show dialog to confirm delete task
        confirmDeleteTask((dialog, which) -> {
            ProgressDialog.showDialog(this);
            database.TASKS.delete(task, res -> {
                ProgressDialog.hideDialog();
                if (res.isSuccessful()) {
                    Toast.makeText(this, R.string.update_successfully, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("delete", true);
                    setResult(RESULT_OK, intent);
                    finish();
                    return;
                }
                setResult(RESULT_CANCELED);
                Toast.makeText(this, R.string.update_failure, Toast.LENGTH_SHORT).show();
            });
        }) ;
    }

    private void confirmDeleteTask(DialogInterface.OnClickListener confirmDelete){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.Are_you_sure_you_want_to_delete_this_entry);
        builder.setNegativeButton(R.string.Cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.Done,confirmDelete);
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.deleteTask);
        item.setVisible(true);
        return true;
    }
}
