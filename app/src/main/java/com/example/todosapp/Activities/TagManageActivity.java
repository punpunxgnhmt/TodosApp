package com.example.todosapp.Activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Adapters.TagManageAdapter;
import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.Database;
import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.Dialogs.TextInputDialog;
import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Interfaces.OptionsTag;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;


/**
 * This activity is used for manage tags of user.
 */
public class TagManageActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView rvTagManage;
    LinearLayout btnAddTag;

    ArrayList<Tag> tags;
    TagManageAdapter adapter;
    Database database;
    ChildRefEventListener tagsChangeEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_manage);
        overridePendingTransition(R.anim.from_right, R.anim.to_left);
        bindingViews();
        initData();
        setToolBar();
        initListTagView();
        handleEvents();
    }

    private void bindingViews() {
        toolbar = findViewById(R.id.toolbar);
        rvTagManage = findViewById(R.id.rvTagManage);
        btnAddTag = findViewById(R.id.btnAddTag);
    }

    private void initData() {
        TodoApplication application = (TodoApplication) getApplication();
        database = application.getDatabase();
        tags = database.TAGS.get();

        // listen tag change
        tagsChangeEvent = new ChildRefEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot) {
                adapter.notifyItemChanged(tags.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, int position, Object oldObject) {
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot, int position) {
                adapter.notifyItemRemoved(position);
            }
        };

        database.TAGS.addValueRefChangeListener(tagsChangeEvent);
    }

    private void setToolBar() {
        toolbar.setTitle(R.string.manage_tag);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initListTagView() {
        adapter = new TagManageAdapter(this, tags, new OptionsTag() {
            @Override
            public void edit(Tag tag) {
                // this method will be called when user click the update button on tag
                showUpdateDialog(tag);
            }

            @Override
            public void delete(Tag tag) {
                // this method will be called when user click the delete button on tag
                showDeleteTagDialog(tag);
            }
        });
        rvTagManage.setAdapter(adapter);
        rvTagManage.setLayoutManager(new LinearLayoutManager(TagManageActivity.this, LinearLayoutManager.VERTICAL, false));
    }

    private void handleEvents() {
        btnAddTag.setOnClickListener(v -> showAddTagDialog(new Tag()));
    }

    private void showUpdateDialog(Tag tag) {

        // handle update tag
        // create update tag dialog
        TextInputDialog dialog = new TextInputDialog(this);

        // set title
        dialog.setTitle(getString(R.string.Edit));

        // set current tag title
        dialog.setInput(tag.getTitle());

        // handle when user click update
        dialog.setOnPositiveClickListener((v, title) -> {

            // check valid tag tile
            if (title.equals("")) {
                dialog.showErrorText(getString(R.string.tag_title_not_empty));
                return;
            }

            // check change
            if (title.equals(tag.getTitle())) {
                dialog.dismiss();
                return;
            }

            // check title duplicate
            for (Tag mTag : tags) {
                if (!mTag.getId().equals(tag.getId()) && mTag.getTitle().equals(title)) {
                    dialog.showErrorText(getString(R.string.tag_title_already_exist));
                    return;
                }
            }
            // update tag
            tag.setTitle(title);
            updateTag(tag);
            dialog.dismiss();
        });
        dialog.show(getSupportFragmentManager(), "TAG");
    }

    private void showDeleteTagDialog(Tag tag) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle(R.string.delete);
        dialogBuilder.setMessage(R.string.Are_you_sure_you_want_to_delete_this_entry);
        dialogBuilder.setNegativeButton(R.string.Cancel, (dialog, which) -> {
            dialog.dismiss();
        });

        dialogBuilder.setPositiveButton(R.string.ok, (dialog, which) -> {
            deleteTag(tag);
        });

        dialogBuilder.show();
    }

    private void showAddTagDialog(Tag tag) {
        // create add task dialog
        // this dialog will be the same at the update tag dialog
        TextInputDialog dialog = new TextInputDialog(this);
        dialog.setTitle(getString(R.string.Add));
        dialog.setOnPositiveClickListener((v, title) -> {
            if (title.equals("")) {
                dialog.showErrorText(getString(R.string.tag_title_not_empty));
                return;
            }
            for (Tag mTag : tags) {
                if (mTag.getTitle().equals(title)) {
                    dialog.showErrorText(getString(R.string.tag_title_already_exist));
                    return;
                }
            }
            tag.setTitle(title);
            addNewTag(tag);
            dialog.dismiss();
        });

        dialog.show(getSupportFragmentManager(), "TAG");
    }


    private void addNewTag(Tag tag) {
        int maxTags = 8;
        if (tags.size() >= maxTags) {
            Toast.makeText(this, R.string.the_maximum_number_of_categories_is_, Toast.LENGTH_SHORT).show();
            return;
        }
        ProgressDialog.showDialog(this);
        database.TAGS.add(tag, task -> {
            ProgressDialog.hideDialog();
            if (!task.isSuccessful())
                checkError(task.getException());
        }, () -> checkError(null));
    }

    private void updateTag(Tag tag) {
        // call update tag from database
        ProgressDialog.showDialog(this);
        database.TAGS.update(tag, task -> {
            ProgressDialog.hideDialog();
            if (!task.isSuccessful())
                checkError(task.getException());
        }, () -> checkError(null));
    }

    private void deleteTag(Tag tag) {
        //delete tag

        // if current total tag is less or equals to 3 -> can't delete tag
        int minTags = 3;
        if (tags.size() - 1 <= minTags) {
            Toast.makeText(this, R.string.the_minimum_number_of_categories_is_, Toast.LENGTH_SHORT).show();
            return;
        }

        // call delete tag from database
        ProgressDialog.showDialog(this);
        database.TAGS.delete(tag, task -> {
            ProgressDialog.hideDialog();
            if (!task.isSuccessful())
                checkError(task.getException());
        }, () -> checkError(null));
    }

    private void checkError(Exception e){
        HandleError.checkNetWorkError(TagManageActivity.this, e);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.TAGS.removeValueRefChangeListener(tagsChangeEvent);
        overridePendingTransition(R.anim.from_left, R.anim.to_right);
    }
}