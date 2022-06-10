package com.example.todosapp.Database;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.Models.User;
import com.example.todosapp.Utils.FirebaseConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 *
 * */
public class Database {

    private User user;
    public TaskDAO TASKS;
    public TagDAO TAGS;


    public void login(User user) {
        this.user = user;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference(FirebaseConstants.REFERENCES.USERS).child(user.getId());
        DatabaseReference tagReference = reference.child(FirebaseConstants.REFERENCES.TAGS);
        DatabaseReference taskReference = reference.child(FirebaseConstants.REFERENCES.TASKS);
        this.TASKS = new TaskDAO(taskReference, user);
        this.TAGS = new TagDAO(tagReference, user);
    }


    public User getUser() {
        return user;
    }

    public void clear(){
        user = null;
        TASKS.clear();
        TAGS.clear();
    }

}
