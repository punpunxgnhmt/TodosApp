package com.example.todosapp.Models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.todosapp.R;
import com.example.todosapp.Utils.Tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task implements Serializable {
    private String id;
    private String title;
    private String note;
    private List<Todo> todos;
    private Date deadline;
    private boolean isComplete;
    private Date completedDate;
    private Tag tag;

    public Task() {
        this.id = "";
        this.title = "";
        this.note = "";
        this.todos = new ArrayList<>();
        this.deadline = new Date();
        this.isComplete = false;
    }

    public Task(String id, String title, String note, List<Todo> todos, Date deadline, Tag tag) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.todos = todos;
        this.deadline = deadline;
        this.tag = tag;
        this.isComplete = false;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = todos;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public String getTagId() {
        if (tag != null)
            return tag.getId();

        return "";
    }

    public Date getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate) {
        this.completedDate = completedDate;
    }

    public String getTypeTask() {
        Date today = Tools.removeTime(new Date());
        if (this.isComplete) {
            Date completedDate = Tools.removeTime(this.completedDate);
            if (completedDate != null && completedDate.compareTo(today) == 0) {
                return TypeTask.TODAY_COMPLETED;
            }
            return TypeTask.COMPLETED;
        }
        Date deadlineDate = Tools.removeTime(this.deadline);

        int compareDeadline = deadlineDate.compareTo(today);
        if (compareDeadline == 0)
            return TypeTask.TODAY_UNCOMPLETED;
        else if (compareDeadline > 0)
            return TypeTask.FUTURE_UNCOMPLETED;

        return TypeTask.PAST_UNCOMPLETED;
    }

    public String isValid(Context context) {
        if (title.equals(""))
            return context.getString(R.string.title_empty);

        return "";
    }

    public boolean equals(Task task) {
        return this.getId().equals(task.getId());
    }



    @NonNull
    public Task clone() {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDeadline(new Date(deadline.getTime()));
        task.setTag(tag);
        task.setNote(note);
        task.setComplete(isComplete);
        task.setCompletedDate(completedDate);
        task.setTodos(new ArrayList<>(todos));

        return task;
    }

    @NonNull
    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", deadline=" + deadline +
                ", completedDate=" + completedDate +
                ", isComplete=" + isComplete +
                '}';
    }

    public static class TypeTask implements Serializable {
        public static String TODAY_COMPLETED = "TODAY_COMPLETED";
        public static String COMPLETED = "COMPLETED";
        public static String TODAY_UNCOMPLETED = "TODAY_UNCOMPLETED";
        public static String PAST_UNCOMPLETED = "BEFORE_UNCOMPLETED";
        public static String FUTURE_UNCOMPLETED = "FUTURE_UNCOMPLETED";
    }


}
