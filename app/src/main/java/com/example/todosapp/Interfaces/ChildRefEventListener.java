package com.example.todosapp.Interfaces;

import com.google.firebase.database.DataSnapshot;

public interface ChildRefEventListener {
    void onChildAdded(DataSnapshot snapshot);
    void onChildChanged(DataSnapshot snapshot, int position, Object oldObject);
    void onChildRemoved(DataSnapshot snapshot, int position);
}
