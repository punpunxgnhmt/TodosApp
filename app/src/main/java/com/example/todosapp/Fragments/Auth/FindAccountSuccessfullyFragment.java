package com.example.todosapp.Fragments.Auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todosapp.R;
import com.google.android.material.button.MaterialButton;


public class FindAccountSuccessfullyFragment extends Fragment {


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
        tvTitle.setText(R.string.ForgotPassword);
        tvDescriptions.setText(R.string.check_your_email_to_reset_password);
    }

    private void HandleEvents() {
        btnAction.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });
    }
}