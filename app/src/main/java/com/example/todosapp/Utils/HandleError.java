package com.example.todosapp.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.todosapp.Dialogs.ProgressDialog;
import com.example.todosapp.R;
import com.example.todosapp.Views.MessageLayout;

public class HandleError {

    public final static String tag = "EEEE";

    public static void checkNetWorkError(MessageLayout messageLayout, Exception e) {
        ProgressDialog.hideDialog();
        Log.e(tag, "checkNetWorkError: " + e.getMessage());

        if (Tools.isInternetAvailable(messageLayout.getContext())) {
            messageLayout.addErrorMessage(R.string.no_internet_connection);
            return;
        }

        messageLayout.addErrorMessage(R.string.unknown_error);
    }

    public static void checkNetWorkError(Context context, Exception e) {
        ProgressDialog.hideDialog();


        if (Tools.isInternetAvailable(context)) {
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show();

        if (e != null)
            Log.e(tag, "checkNetWorkError: " + e.getMessage());
    }

    public static void updateProfileFailed(Context context, Exception e) {
        ProgressDialog.hideDialog();
        Log.e(tag, "updateProfileFailed: " + e.getMessage());
        Toast.makeText(context, R.string.update_profile_failed, Toast.LENGTH_SHORT).show();
    }

    public static void emailAlreadyUsed(MessageLayout messageLayout) {

        messageLayout.addErrorMessage(R.string.email_already_used);
        ProgressDialog.hideDialog();
    }

    public static void loginFailed(MessageLayout messageLayout, Exception e) {
        ProgressDialog.hideDialog();
        Log.e(tag, "loginFailed: " + e.getMessage());

        if (Tools.isInternetAvailable(messageLayout.getContext())) {
            messageLayout.addErrorMessage(R.string.no_internet_connection);
            return;
        }
        if (e.getMessage().equals(FirebaseConstants.ERROR.PASSWORD_INVALID) ||
                e.getMessage().equals(FirebaseConstants.ERROR.USER_NOT_EXIST)) {
            messageLayout.addErrorMessage(R.string.email_or_password_not_valid);
            return;
        }
        messageLayout.addErrorMessage(R.string.unknown_error);
    }

    public static void loginGoogleFailed(Context context, Exception e) {
        ProgressDialog.hideDialog();
        Log.e(tag, "loginGoogleFailed: " + e.getMessage());
        Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
    }

    public static void sendEmailFailed(Context context, Exception e) {
        ProgressDialog.hideDialog();
        Log.e(tag, "sendEmailFailed: " + e.getMessage());
        Toast.makeText(context, R.string.cant_send_verify_email, Toast.LENGTH_SHORT).show();
    }
}
