package com.example.todosapp.Fragments.Auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.example.todosapp.Utils.Tools;
import com.example.todosapp.Views.MessageLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


public class FindAccountFragment extends Fragment {


    View view;
    ConstraintLayout findAccountLayout;
    MessageLayout messageLayout;
    TextInputEditText edtEmail;
    MaterialButton btnFindAccount, btnLogin;

    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_find_account, container, false);
        bindingViews();
        initData();
        handleEvents();
        return view;
    }

    private void bindingViews() {
        findAccountLayout = view.findViewById(R.id.findAccountLayout);
        messageLayout = view.findViewById(R.id.messageLayout);
        edtEmail = view.findViewById(R.id.edtEmail);
        btnFindAccount = view.findViewById(R.id.btnFindAccount);
        btnLogin = view.findViewById(R.id.btnLogin);

    }

    private void initData() {
        auth = FirebaseAuth.getInstance();
    }

    private void handleEvents() {
        findAccountLayout.setOnClickListener(v -> {
            Tools.hideSoftKeyBoard(getActivity());
        });

        btnFindAccount.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();

            if (checkInfoValidate(email)) {
                findAccount(email);
            }
        });

        btnLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });
    }

    private boolean checkInfoValidate(String email) {

        if (email.equals("")) {
            messageLayout.addErrorMessage(R.string.please_enter_email);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            messageLayout.addErrorMessage(R.string.email_not_valid);
            return false;
        }

        return true;
    }

    private void findAccount(String email) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.getResult().getSignInMethods().size() != 0) {
                sendResetPasswordMail(email);
            } else {
                messageLayout.addErrorMessage(R.string.account_not_exist);
            }
        }).addOnFailureListener(e -> {
            HandleError.checkNetWorkError(messageLayout, e);
        });
    }

    private void sendResetPasswordMail(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(task -> {
                    sendMailSuccessfully();
                })
                .addOnFailureListener(e -> {
                    HandleError.checkNetWorkError(messageLayout, e);
                });

    }

    private void sendMailSuccessfully() {
        Toast.makeText(getContext(), R.string.sended_verify_mail, Toast.LENGTH_SHORT).show();
        Navigation.findNavController(view).navigate(R.id.action_findAccountFragment_to_findAccountSuccessfullyFragment);
    }
}