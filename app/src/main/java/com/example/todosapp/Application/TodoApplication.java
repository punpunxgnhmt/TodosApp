package com.example.todosapp.Application;

import android.app.Application;
import android.util.Log;

import com.example.todosapp.Database.Database;
import com.example.todosapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * this class will be have one instance which created when user
 * open the application and destroyed when the app is closed.
 * */
public class TodoApplication extends Application {
    User user;
    Database database;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public void login(User user) {
        this.user = user;
        if (user.getTags() == null)
            user.setTags(new ArrayList<>());

        if (user.getTasks() == null) {
            user.setTasks(new ArrayList<>());
        }
        database = new Database();
        database.login(user);
    }

    public Database getDatabase() {
        return database;
    }

    public User getUser() {
        return user;
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        database.clear();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.e("EEE", "APP KILL");
    }
}
