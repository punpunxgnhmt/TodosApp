package com.example.todosapp.Dialogs;

import android.content.Context;
import android.util.Log;

import com.example.todosapp.R;


public class ProgressDialog {
    private static android.app.ProgressDialog progressDialog;
    private static boolean isVisible;

    public static void showDialog(Context context, String message) {
        if (context != null) {
            progressDialog = new android.app.ProgressDialog(context);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    public static void showDialog(Context context) {
        if (context != null) {
            progressDialog = new android.app.ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.please_wait));
            progressDialog.show();
        }
    }

    public static void hideDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
