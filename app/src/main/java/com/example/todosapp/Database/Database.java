package com.example.todosapp.Database;


import com.example.todosapp.Models.User;
import com.example.todosapp.Utils.FirebaseConstants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
