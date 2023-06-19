package com.example.todosapp.Fragments.Auth;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todosapp.Activities.LoadingDataActivity;
import com.example.todosapp.Activities.MainActivity;
import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.example.todosapp.Utils.Tools;
import com.example.todosapp.Views.MessageLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginFragment extends Fragment {

    View view;
    ScrollView loginLayout;
    MessageLayout messageLayout;
    TextInputEditText edtEmail, edtPassword;
    MaterialButton btnForgotPassword, btnLogin, btnSignUp;
    AppCompatImageButton btnLoginGoogle;

    FirebaseAuth auth;
    FirebaseUser user;
    GoogleSignInClient googleSignInClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        bindingViews();
        configGoogleSignIn();
        handleEvents();
        return view;
    }


    private void bindingViews() {
        loginLayout = view.findViewById(R.id.loginLayout);
        messageLayout = view.findViewById(R.id.messageLayout);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPassword = view.findViewById(R.id.edtPassword);
        btnForgotPassword = view.findViewById(R.id.btnForgotPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLoginGoogle = view.findViewById(R.id.btnLoginGoogle);
        btnSignUp = view.findViewById(R.id.btnSignUp);
    }

    private void configGoogleSignIn() {
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    private void handleEvents() {

        loginLayout.setOnClickListener(v -> {
            Tools.hideSoftKeyBoard(getActivity());
        });

        btnLogin.setOnClickListener(v -> {
            handleLogin();
        });

        btnForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_findAccountFragment);
        });

        btnLoginGoogle.setOnClickListener(v -> {
            handleLoginGoogle();
        });

        btnSignUp.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment);
        });

    }

    private void handleLogin() {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        if (checkInfoValidate(email, password)) {
            login(email, password);
        }
    }

    private boolean checkInfoValidate(String email, String password) {
        loginLayout.smoothScrollTo(0, 0);
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
        return true;
    }

    private void login(String email, String password) {
        ProgressDialog.showDialog(getContext());
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    checkVerifyAccount();
                })
                .addOnFailureListener(e -> {
                    HandleError.loginFailed(messageLayout, e);
                });

    }

    private void checkVerifyAccount() {
        user = auth.getCurrentUser();
        if (user.isEmailVerified()) {
            loginSuccessfully();
        } else {
            notifyVerifiedEmail();
        }
    }

    private void handleLoginGoogle() {
        ProgressDialog.showDialog(getContext());
        Intent signInIntent = googleSignInClient.getSignInIntent();
        signInCredential.launch(signInIntent);
    }

    private final ActivityResultLauncher<Intent> signInCredential = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.e(HandleError.tag, result.toString());
                if (result.getResultCode() == RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        loginWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        HandleError.loginGoogleFailed(getContext(), e);
                    }
                }
            }
    );

    private void loginWithGoogle(String token) {
        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    Log.e(HandleError.tag, task.toString());
                })
                .addOnSuccessListener(task -> {
                    loginSuccessfully();
                })
                .addOnFailureListener(e -> {
                    HandleError.loginGoogleFailed(getContext(), e);
                });
    }

    private void notifyVerifiedEmail() {
        ProgressDialog.hideDialog();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(getString(R.string.verify_email));
        builder.setMessage(getString(R.string.verify_email_to_login));
        builder.setNegativeButton(R.string.send_email, (dialog1, which) -> {
                    ProgressDialog.showDialog(getContext());
                    user.sendEmailVerification()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    ProgressDialog.hideDialog();
                                    Toast.makeText(getContext(), getString(R.string.verify_email_to_login), Toast.LENGTH_SHORT).show();
                                } else {
                                    HandleError.sendEmailFailed(getActivity(), task.getException());
                                }
                                dialog1.dismiss();
                            });
                }
        );

        builder.setPositiveButton(R.string.ok, (dialog12, which) -> dialog12.dismiss());
        builder.setOnDismissListener(dialog13 -> auth.signOut());
        builder.show();
    }

    private void loginSuccessfully() {
        ProgressDialog.hideDialog();
        Toast.makeText(getContext(), R.string.login_successfully, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoadingDataActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

}