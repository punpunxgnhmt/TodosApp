package com.example.todosapp.Models;

import android.content.Context;

import com.example.todosapp.R;
import com.example.todosapp.Utils.FirebaseConstants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class User {
    String id;
    ArrayList<Tag> tags;
    ArrayList<Task> tasks;

    public User() {
        tags = new ArrayList<>();
        tasks = new ArrayList<>();
    }

    public User(String id, ArrayList<Tag> tags) {
        this.id = id;
        this.tags = tags;
        tasks = new ArrayList<>();
    }


    public String getId() {
        return id;
    }

    public ArrayList<Tag> getTags() {
        return tags;
    }

    public void setTags(ArrayList<Tag> tags) {
        this.tags = tags;
    }

    public static User createNewUser(String userId, Context context) {

        ArrayList<Tag> tags = new ArrayList<>();
        int position = 0;
        String id;

        id = String.valueOf(position);
        Tag unclassified = new Tag(id, context.getString(R.string.unclassified));
        tags.add(unclassified);
        position++;

        id = String.valueOf(position);
        Tag workTag = new Tag(id, context.getString(R.string.work));
        tags.add(workTag);
        position++;

        id = String.valueOf(position);
        Tag personTag = new Tag(id, context.getString(R.string.personal));
        tags.add(personTag);
        position++;

        id = String.valueOf(position);
        Tag favoriteTag = new Tag(id, context.getString(R.string.favorite));
        tags.add(favoriteTag);
        position++;

        id = String.valueOf(position);
        Tag birthdayTag = new Tag(id, context.getString(R.string.birthday));
        tags.add(birthdayTag);

        return new User(userId, tags);
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }


}
