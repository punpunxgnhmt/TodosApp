package com.example.todosapp.Models;

import java.io.Serializable;

public class Todo implements Serializable {
    String id;
    String title;
    boolean isComplete;

    public Todo() {
        this.id = "";
        this.title = "";
        this.isComplete = false;
    }

    public Todo(String id, String title, boolean isComplete) {
        this.id = id;
        this.title = title;
        this.isComplete = isComplete;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
