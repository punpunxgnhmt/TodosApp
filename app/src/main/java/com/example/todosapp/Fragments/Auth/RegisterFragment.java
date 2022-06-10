package com.example.todosapp.Fragments.Auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.example.todosapp.Utils.Tools;
import com.example.todosapp.Views.MessageLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class RegisterFragment extends Fragment {

    View view;
    ScrollView registerLayout;
    MessageLayout messageLayout;
    TextInputEditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    MaterialButton btnSignUp, btnLogin;


    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register, container, false);
        bindingViews();
        initData();
        handleEvents();
        return view;
    }

    private void bindingViews() {
        registerLayout = view.findViewById(R.id.registerLayout);
        messageLayout = view.findViewById(R.id.messageLayout);
        edtUsername = view.findViewById(R.id.edtUsername);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnLogin = view.findViewById(R.id.btnLogin);
    }

    private void initData() {
        auth = FirebaseAuth.getInstance();
    }

    private void handleEvents() {
        registerLayout.setOnClickListener(v -> Tools.hideSoftKeyBoard(getActivity()));
        btnSignUp.setOnClickListener(v -> {
            handleSignUp();
        });

        btnLogin.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });
    }

    private void handleSignUp() {
        edtConfirmPassword.requestFocus();
        Tools.hideSoftKeyBoard(getActivity());
        String email = edtEmail.getText().toString();
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        String cPassword = edtConfirmPassword.getText().toString();

        if (checkInfoValidate(username, email, password, cPassword)) {
            checkUserExist(username, email, password);
        }
    }

    private boolean checkInfoValidate(String username, String email, String password, String cPassword) {
        registerLayout.smoothScrollTo(0, 0);
        if (username.equals("")) {
            messageLayout.addErrorMessage(R.string.please_enter_username);
            return false;
        }
        if (email.equals("")) {
            messageLayout.addErrorMessage(R.string.please_enter_email);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            messageLayout.addErrorMessage(R.string.email_not_valid);
            return false;
        }

        if (password.equals("")) {
            messageLayout.addErrorMessage(R.string.please_enter_password);
            return false;
        }

        if (password.length() < 6) {
            messageLayout.addErrorMessage(R.string.password_must_be_more_than_6);
            return false;
        }

        if (!password.equals(cPassword)) {
            messageLayout.addErrorMessage(R.string.password_not_matching);
            return false;
        }

        return true;
    }

    private void checkUserExist(String username, String email, String password) {
        ProgressDialog.showDialog(getContext());
        auth.fetchSignInMethodsForEmail(email)
                .addOnSuccessListener(task -> {
                    if (task.getSignInMethods().size() != 0) {
                        HandleError.emailAlreadyUsed(messageLayout);
                        return;
                    }
                    createNewUser(username, email, password);

                })
                .addOnFailureListener(e -> {
                    HandleError.checkNetWorkError(messageLayout, e);
                });
    }

    private void createNewUser(String username, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(requireActivity(), task -> {
                    user = auth.getCurrentUser();
                    if (user != null) {
                        updateUserProfile(username);
                    }
                })
                .addOnFailureListener(e -> {
                    HandleError.checkNetWorkError(messageLayout, e);
                });

    }

    private void updateUserProfile(String username) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username).build();

        user.updateProfile(profileUpdates)
                .addOnSuccessListener(unused -> {
                    user.sendEmailVerification();
                    navigateToVerifyAccountScreen();
                })
                .addOnFailureListener(e -> {
                    HandleError.updateProfileFailed(getContext(), e);
                    navigateToVerifyAccountScreen();
                });
    }

    private void navigateToVerifyAccountScreen() {
        ProgressDialog.hideDialog();
        auth.signOut();
        Toast.makeText(getContext(), R.string.sign_up_successfully, Toast.LENGTH_LONG).show();
        Navigation.findNavController(view).navigate(R.id.action_registerFragment_to_registerSuccessfullyFragment);
    }


}
