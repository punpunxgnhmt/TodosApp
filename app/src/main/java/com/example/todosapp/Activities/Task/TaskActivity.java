package com.example.todosapp.Activities.Task;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Adapters.TagArrayAdapter;
import com.example.todosapp.Adapters.TodoAdapter;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.Helper.TodoAdapterItemTouchHelper;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.Models.Todo;
import com.example.todosapp.R;
import com.example.todosapp.Utils.Tools;
import com.example.todosapp.Views.MessageLayout;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;


/**
 * This activity will be render information of the task before update or add a task
 *
 *
 * */

public class TaskActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected TextInputEditText edtTask, edtNote;
    protected RecyclerView rvTodo;
    protected Spinner spnTag;
    protected LinearLayout btnAddTodo, btnDate, btnTime, btnNote;
    protected TextView tvDeadlineDate, tvDeadlineTime;
    protected MessageLayout messageLayout;

    protected Database database;
    protected ArrayList<Tag> tags;
    protected Task task;
    protected ArrayList<Todo> todos;
    protected TodoAdapter todoAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        //Get database from application
        initData();

        // binding views from layout resource
        bidingViews();

        // set title and create menu
        setUpToolBar();

        // create list tags dropdown
        createTagDropDown();

        // render todo(subtask) of task
        setUpTodoRv();

        // set deadline or create deadline equals current time if not exist
        setCurrentDeadline();

        // handle events for views
        handleEvents();
    }


    protected void initData() {
        // Get application
        TodoApplication application = (TodoApplication) getApplication();
        // Get database
        database = application.getDatabase();
        // Get tag from database
        tags = database.TAGS.get();
    }

    protected void bidingViews() {
        toolbar = findViewById(R.id.toolbar);
        edtTask = findViewById(R.id.edtTask);
        rvTodo = findViewById(R.id.rvTodo);
        btnAddTodo = findViewById(R.id.btnAddTodo);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        btnNote = findViewById(R.id.btnNote);
        spnTag = findViewById(R.id.spnTag);
        tvDeadlineDate = findViewById(R.id.tvDeadlineDate);
        tvDeadlineTime = findViewById(R.id.tvDeadlineTime);
        edtNote = findViewById(R.id.edtNote);
        messageLayout = findViewById(R.id.messageLayout);
    }

    protected void setUpToolBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        // listen event when user click menu item on toolbar
        toolbar.setOnMenuItemClickListener(item -> {
            //R.id.cancelTask: user select cancel -> close activity
            if (item.getItemId() == R.id.cancelTask) {
                finish();
            } else {
                // user select update or add a new task -> call finish task method
                if (item.getItemId() == R.id.finishTask) {
                    finishTask();
                } else {
                    // user select delete task, it will only call in update task activity
                    deleteTask();
                }
            }
            return true;
        });
    }

    protected void createTagDropDown() {
        // create adapter for rendering
        TagArrayAdapter tagArrayAdapter = new TagArrayAdapter(this, R.layout.item_tag_array_selected, tags);
        spnTag.setAdapter(tagArrayAdapter);

        // get current selected tag from task
        Tag selectedTag = task.getTag();

        // if selected task not exist, the first tag in list will be selected
        if (selectedTag != null) {
            for (int i = 0; i < tags.size(); i++) {
                Tag tag = tags.get(i);
                if (tag.getId().equals(selectedTag.getId())) {
                    spnTag.setSelection(i);
                    return;
                }
            }
        }
        spnTag.setSelection(0);
        task.setTag(tags.get(0));
    }

    // rendering todo list
    protected void setUpTodoRv() {
        todos = (ArrayList<Todo>) task.getTodos();

        // if todos are null, create a new List
        if (todos == null) {
            todos = new ArrayList<>();
        }

        // create adapter and attach it to recycleView
        todoAdapter = new TodoAdapter(this, todos);
        rvTodo.setAdapter(todoAdapter);
        rvTodo.setLayoutManager(new LinearLayoutManager(TaskActivity.this, LinearLayoutManager.VERTICAL, false));


        // create event that listens for the user to drag and drop a todo
        ItemTouchHelper dragTouch = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                // if user drag a todo. this will be called
                // Moved item to the position that dropped
                int positionDragged = viewHolder.getAdapterPosition();
                int positionTarget = target.getAdapterPosition();

                Collections.swap(todos, positionDragged, positionTarget);
                todoAdapter.notifyItemMoved(positionDragged, positionTarget);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        // attach it to  recycleView
        dragTouch.attachToRecyclerView(rvTodo);


        // create event that listens for the user to swipe left or right
        ItemTouchHelper.SimpleCallback swipeLeftCallback = new TodoAdapterItemTouchHelper(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, viewHolder -> {
            // if user swipe to left or right a todo, it will be deleted.
            todoAdapter.removeTodo(viewHolder.getAdapterPosition());

            //Display a snack bar to unRemove Todo
            showUndoSnackBar();
        });
        ItemTouchHelper deleteSwipe = new ItemTouchHelper(swipeLeftCallback);
        // attach it to  recycleView
        deleteSwipe.attachToRecyclerView(rvTodo);
    }

    protected void setCurrentDeadline() {
        // create format date and time string.
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm");

        // get deadline from task
        Date deadline = task.getDeadline();
        // set this for TextView to display
        tvDeadlineDate.setText(dateFormat.format(deadline));
        tvDeadlineTime.setText(timeFormat.format(deadline));
    }

    protected void showUndoSnackBar() {
        // if user click button Undo, the todo which was removed will be added to list
        @SuppressLint("ShowToast")
        Snackbar snackbar = Snackbar.make(btnNote, R.string.you_just_deleted_a_todo, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.undo, v -> todoAdapter.undoRemoveTodo());
        snackbar.setActionTextColor(getColor(R.color.primaryColor));
        snackbar.show();
    }

    protected void handleEvents() {

        // save a new tag when user change selected tag
        spnTag.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Tag tag = tags.get(position);
                task.setTag(tag);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // user click add new todo
        btnAddTodo.setOnClickListener(v -> {
            todoAdapter.addNewTodo();
        });

        // user click to choose Deadline date
        btnDate.setOnClickListener(v -> {
            showDatePicker();
        });

        // user click to choose Deadline time
        btnTime.setOnClickListener(v -> showTimePicker());

        // user change note
        edtNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                task.setNote(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // user change title of the task
        edtTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                task.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    protected void showDatePicker() {
        //show date picker for user select deadline date
        TimeZone timeZone = TimeZone.getDefault();
        long timestamp = task.getDeadline().getTime();
        timestamp += timeZone.getOffset(timestamp);

        MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
        datePickerBuilder.setTitleText(R.string.Deadline);
        datePickerBuilder.setSelection(timestamp);
        MaterialDatePicker<Long> datePicker = datePickerBuilder.build();
        datePicker.addOnPositiveButtonClickListener(this::setDeadlineDate);
        datePicker.show(getSupportFragmentManager(), "date");
    }

    protected void setDeadlineDate(long time) {
        //show time picker for user select deadline time
        Date deadlineTime = Tools.removeDate(task.getDeadline());
        Date deadlineDate = new Date(time);
        Date deadline = new Date(deadlineDate.getTime() + deadlineTime.getTime());
        task.setDeadline(deadline);

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        tvDeadlineDate.setText(sdf.format(deadline));
    }

    protected void showTimePicker() {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder();
        builder.setTitleText(R.string.setTimeDeadline);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDeadline());
        builder.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        builder.setMinute(calendar.get(Calendar.MINUTE));

        MaterialTimePicker timePicker = builder.build();
        timePicker.addOnPositiveButtonClickListener(v -> {
            int hours = timePicker.getHour();
            int minute = timePicker.getMinute();
            setTimeDeadline(hours, minute);
        });

        timePicker.show(getSupportFragmentManager(), "time");
    }

    protected void setTimeDeadline(int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(task.getDeadline());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        task.setDeadline(calendar.getTime());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
        tvDeadlineTime.setText(sdf.format(task.getDeadline()));
    }

    // this method will be override in update task activity
    protected void deleteTask() {

    }

    // this method will be override in both add task activity and update task activity
    protected void finishTask() {

    }

    // create menu in toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_task, menu);
        return super.onCreateOptionsMenu(menu);
    }
}