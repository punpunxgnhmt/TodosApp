package com.example.todosapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;

import com.example.todosapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        CheckLogin();
    }

    private void CheckLogin() {

        // get current user
        user = FirebaseAuth.getInstance().getCurrentUser();

        // if user not null -> user logged
        if(user != null){
            // navigate to loading screen and finish this.
            Intent intent = new Intent(this, LoadingDataActivity.class);
            startActivity(intent);
            finish();
        }
    }
}