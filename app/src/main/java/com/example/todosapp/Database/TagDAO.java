package com.example.todosapp.Database;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todosapp.Interfaces.ChildRefEventListener;
import com.example.todosapp.Interfaces.FindTag;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * this class is used for interaction with database about tags.
 * */
public class TagDAO {
    // save all tags of user
    private ArrayList<Tag> tags;

    private DatabaseReference reference;
    private ChildEventListener eventListener;

    // send notify when tags change to the objects which registered.
    private ArrayList<ChildRefEventListener> callbacks;

    public TagDAO(DatabaseReference tagReference, User user) {
        this.reference = tagReference;
        this.tags = user.getTags();
        this.callbacks = new ArrayList<>();
        handleTagChangeListener();
    }

    // register date change
    public void addValueRefChangeListener(ChildRefEventListener callback) {
        if (callback != null) {
            if (!callbacks.contains(callback)) {
                callbacks.add(callback);
            }
        }
    }

    // cancel notify data change
    public void removeValueRefChangeListener(ChildRefEventListener callback) {
        if (callback != null && callbacks != null) {
            callbacks.remove(callback);
        }
    }

    private void handleTagChangeListener() {
        // listen value change from database
        eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method will be call if the new tag is added
                Tag tag = snapshot.getValue(Tag.class);

                // check tag exist
                for (int i = 0; i < tags.size(); i++) {
                    Tag iTag = tags.get(i);
                    if (tag.getId().equals(iTag.getId())) {
                        return;
                    }
                }
                // if not exist, save it and send notifications.
                tags.add(tag);
                for (ChildRefEventListener callback : callbacks) {
                    callback.onChildAdded(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // this method will be call if the tag is updated

                Tag tag = snapshot.getValue(Tag.class);
                // check tag exist
                for (int i = 0; i < tags.size(); i++) {
                    Tag iTag = tags.get(i);
                    if (tag.getId().equals(iTag.getId())) {
                        //if exist save it and send notifications.
                        Tag oldTag = tags.get(i);
                        tags.remove(i);
                        tags.add(i, tag);
                        for (ChildRefEventListener callback : callbacks) {
                            callback.onChildChanged(snapshot, i, oldTag);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Tag tag = snapshot.getValue(Tag.class);
                for (int i = 0; i < tags.size(); i++) {
                    Tag iTag = tags.get(i);
                    if (tag.getId().equals(iTag.getId())) {
                        tags.remove(i);
                        for (ChildRefEventListener callback : callbacks) {
                            callback.onChildRemoved(snapshot, i);
                        }
                        return;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        reference.addChildEventListener(eventListener);
    }

    public ArrayList<Tag> get() {
        return tags;
    }

    public void add(Tag tag, OnCompleteListener<Void> callback) {
        //create a new tag in database
        String id = reference.push().getKey();
        tag.setId(id);
        reference.child(id).setValue(tag).addOnCompleteListener(callback);
    }

    public void update(Tag tag, OnCompleteListener<Void> callback) {
        // update the exist tag in database.
        reference.child(tag.getId()).setValue(tag).addOnCompleteListener(callback);
    }

    public void delete(Tag tag, OnCompleteListener<Void> callback) {
        //remove the tag exist in database.
        reference.child(tag.getId()).removeValue().addOnCompleteListener(callback);
    }


    public void clear() {
        reference.removeEventListener(eventListener);
        reference = null;
        tags.clear();
        tags = null;
        callbacks.clear();
        callbacks = null;
    }
}
