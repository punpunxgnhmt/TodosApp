package com.example.todosapp.Interfaces;

import com.example.todosapp.Models.Task;

public interface ItemTaskEvent {
    void onItemTaskClick(Task task, int position);
    void onItemCheckedChange(Task task, int position, boolean checked);
}
