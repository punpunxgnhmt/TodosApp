package com.example.todosapp.Interfaces;

import com.example.todosapp.Models.Task;

public interface ItemTaskCheckedChange {
    void onItemCheckedChange(Task task, int position, boolean checked);
}
