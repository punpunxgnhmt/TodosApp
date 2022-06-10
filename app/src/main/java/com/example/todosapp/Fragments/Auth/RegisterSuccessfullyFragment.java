package com.example.todosapp.Fragments.Auth;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.todosapp.R;
import com.google.android.material.button.MaterialButton;


public class RegisterSuccessfullyFragment extends Fragment {


    View view;
    TextView tvTitle, tvDescriptions;
    MaterialButton btnAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_auth_successfully, container, false);

        BindingViews();
        InitUi();
        HandleEvents();
        return view;
    }

    private void BindingViews() {
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescriptions = view.findViewById(R.id.tvDescriptions);
        btnAction = view.findViewById(R.id.btnAction);

    }

    private void InitUi() {
        tvTitle.setText(R.string.SignUpSuccessfully);
        tvDescriptions.setText(R.string.check_your_email_to_active_account);
    }

    private void HandleEvents() {
        btnAction.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });
    }
}