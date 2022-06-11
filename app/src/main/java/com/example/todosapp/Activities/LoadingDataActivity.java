package com.example.todosapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todosapp.Application.TodoApplication;
import com.example.todosapp.Database.TaskDAO;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.Models.Task;
import com.example.todosapp.Models.User;
import com.example.todosapp.R;
import com.example.todosapp.Utils.FirebaseConstants;
import com.example.todosapp.Utils.HandleError;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadingDataActivity extends AppCompatActivity {

    User user;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_data);
        instanceData();
        getUserData();
    }

    private void instanceData() {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference(FirebaseConstants.REFERENCES.USERS).child(firebaseUser.getUid());
    }

    private void getUserData() {
        // get data of user only one time from fire base.
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if fire base return result

                user = new User();

                // get id from result
                String userId = snapshot.child(FirebaseConstants.REFERENCES.ID).getValue(String.class);
                user.setId(userId);

                // get tags from result
                for (DataSnapshot tagSnapshot : snapshot.child(FirebaseConstants.REFERENCES.TAGS).getChildren()) {
                    Tag tag = tagSnapshot.getValue(Tag.class);
                    user.getTags().add(tag);
                }

                // get task from result
                ArrayList<Task> userTasks = user.getTasks();
                for (DataSnapshot taskSnapshot : snapshot.child(FirebaseConstants.REFERENCES.TASKS).getChildren()) {
                    Task task = taskSnapshot.getValue(Task.class);

                    int pos = TaskDAO.findTheAppropriatePositionTaskToAddByDeadline(userTasks, task, 0, userTasks.size());
                    userTasks.add(pos, task);
                }

                // if result does not have data -> new user.
                // create new user data and push it to firebase.
                if (userId == null) {
                    createNewUserData();
                    return;
                }
                navigateToMain();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(HandleError.tag, "GET DATA ERROR: " + error.getMessage());
                Toast.makeText(LoadingDataActivity.this, R.string.loading_data_failure, Toast.LENGTH_SHORT).show();
                HandleError.checkNetWorkError(LoadingDataActivity.this, null);
            }
        });
    }

    private void createNewUserData() {
        // create a new user with reference is user id
        user = User.createNewUser(firebaseUser.getUid(), this);
        userRef.setValue(user)
                .addOnSuccessListener(unused -> {
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(HandleError.tag, "checkNewUser: " + e.getMessage());
                    Toast.makeText(LoadingDataActivity.this, R.string.loading_data_failure, Toast.LENGTH_SHORT).show();
                    HandleError.checkNetWorkError(LoadingDataActivity.this, e);
                });
    }

    private void navigateToMain() {
        TodoApplication application = (TodoApplication) this.getApplication();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            // after get or create user data, save it to application
            application.login(user);

            // navigate to main activity
            runOnUiThread(() -> {
                Intent intent = new Intent(LoadingDataActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            });
        });
        executorService.shutdown();
    }

    private void showFinishDialog(){
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle(R.string.error);
        dialogBuilder.setMessage(R.string.loading_data_failure);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton(R.string.finish, (dialog, which) -> finishAffinity());
        dialogBuilder.setPositiveButton(R.string.try_again, (dialog, which) -> {
            getUserData();
            dialog.dismiss();
        });

        dialogBuilder.show();
    }
}