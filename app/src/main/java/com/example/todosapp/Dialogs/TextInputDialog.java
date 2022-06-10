package com.example.todosapp.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.todosapp.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class TextInputDialog extends BaseDialog {

    View view;
    TextView tvTitle;
    TextInputEditText edtInput;
    MaterialButton btnNegative, btnPositive;

    OnPositiveClickListener handlePositiveClickListener;
    View.OnClickListener handleNegativeClickListener;

    Context context;
    String title;
    String output;


    public TextInputDialog(Context context) {
        this.context = context;
        this.title = "";
        this.output = "";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_text_input, container, false);
        bindingViews();
        bindingData();
        handleEvents();
        return view;
    }


    private void bindingViews() {
        tvTitle = view.findViewById(R.id.tvTitleDialog);
        edtInput = view.findViewById(R.id.edtInput);
        btnNegative = view.findViewById(R.id.btnNegative);
        btnPositive = view.findViewById(R.id.btnPositive);
    }

    private void bindingData() {
        edtInput.setText(output);
        tvTitle.setText(title);
    }

    private void handleEvents() {
        btnNegative.setOnClickListener(v -> {
            if (handleNegativeClickListener != null)
                handleNegativeClickListener.onClick(v);
            dismiss();
        });

        btnPositive.setOnClickListener(v -> {
            if (handlePositiveClickListener != null) {
                String output = edtInput.getText().toString();
                handlePositiveClickListener.onClick(v, output);
            }
        });

        edtInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                output = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }



    public void setTitle(String title) {
        this.title = title;
    }

    public void setInput(String input) {
        this.output = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOnPositiveClickListener(OnPositiveClickListener onPositiveClickListener) {
        this.handlePositiveClickListener = onPositiveClickListener;
    }

    public void setOnNegativeClickListener(View.OnClickListener onClickListener){
        this.handleNegativeClickListener = onClickListener;
    }

    public void showErrorText(String error){
        if(edtInput != null){
            edtInput.setError(error);
        }
    }

    public interface OnPositiveClickListener {
        void onClick(View v, String output);
    }
}
