package com.example.todosapp.Activities.Task;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.Models.Todo;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;

import java.util.Date;

/**
 * This activity extends from Task activity
 * and is used for add a task.
 *
 *
 * */
public class AddTaskActivity extends TaskActivity {

    @Override
    protected void setUpToolBar() {
        super.setUpToolBar();
        getSupportActionBar().setTitle(R.string.AddTask);
    }


    // override from task activity
    // get the variable from the others activity

    @Override
    protected void initData() {
        super.initData();
        task = new Task();
        Intent intent = getIntent();
        // if task fragment send a selectedTagId
        if (intent.hasExtra("selectedTagId")) {
            String selectedTagId = intent.getStringExtra("selectedTagId");
            for (Tag tag : tags) {
                if (tag.getId().equals(selectedTagId)) {
                    task.setTag(tag);
                    break;
                }
            }
        } else {
            // if not, choose the first tag.
            task.setTag(tags.get(0));
        }

        // if the calendar send selectedDate -> save it to deadline
        if (intent.hasExtra("selectedDate")) {
            Date date = (Date) intent.getSerializableExtra("selectedDate");
            if (date != null) {
                task.setDeadline(date);
            }
        }else{
            //if not, choose today.
            task.setDeadline(new Date());
        }
    }

    // this method will be call when user click add in toolbar
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

        // send data to firebase
        database.TASKS.add(task, task -> {
            //if firebase save it -> notify to user
            if (task.isSuccessful()) {
                Toast.makeText(this, R.string.add_successfully, Toast.LENGTH_SHORT).show();
            } else {
            // if not, notify error
                Toast.makeText(this, R.string.add_failure, Toast.LENGTH_SHORT).show();
                HandleError.checkNetWorkError(AddTaskActivity.this, task.getException());
            }
            // close activity and progress dialog
            AddTaskActivity.this.finish();
            ProgressDialog.hideDialog();
        });
    }
}
