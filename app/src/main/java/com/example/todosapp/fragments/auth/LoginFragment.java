package com.example.todosapp.fragments.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todosapp.R;
import com.example.todosapp.activities.MainActivity;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;


public class LoginFragment extends Fragment {

    View view;
        MaterialButton btnSignUp,btnLogin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        bindingViews();
        handleEvents();
        return view;
    }

    private void bindingViews() {
        btnLogin = view.findViewById(R.id.btnLogin);
        btnSignUp = view.findViewById(R.id.btnSignUp);
    }

    private void handleEvents() {
        btnSignUp.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment2_to_registerFragment);
        });
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }


}