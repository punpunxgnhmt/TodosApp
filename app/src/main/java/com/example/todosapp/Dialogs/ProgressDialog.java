package com.example.todosapp.Dialogs;

import android.content.Context;
import android.util.Log;

import com.example.todosapp.R;


public class ProgressDialog extends android.app.ProgressDialog {
    private static ProgressDialog instance;

    private ProgressDialog(Context context) {
        super(context);
    }

    public static void showDialog(Context context, String message) {
        if (context != null) {
            instance = new ProgressDialog(context);
            instance.setMessage(message);
            instance.show();
        }
    }

    public static void showDialog(Context context) {
        if (context != null) {
            instance = new ProgressDialog(context);
            instance.setMessage(context.getString(R.string.please_wait));
            instance.show();
        }
    }

    public static void hideDialog() {
        if (instance != null) {
            instance.dismiss();
        }
    }

    public static boolean isShowingDialog() {
        if (instance == null) {
            return false;
        }

        return instance.isShowing();
    }

}
