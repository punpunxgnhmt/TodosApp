package com.example.todosapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Models.Todo;
import com.example.todosapp.R;
import com.example.todosapp.Utils.HandleError;
import com.example.todosapp.Utils.Tools;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    RecyclerView recyclerView;
    Context context;
    ArrayList<Todo> todos;

    int positionUndo;
    Todo todoUndo;

    public TodoAdapter(Context context, ArrayList<Todo> todos) {
        this.context = context;
        this.todos = todos;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        if (todos == null)
            return 0;
        return todos.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Todo todo = todos.get(position);

        holder.edtTodo.setText(todo.getTitle());
        holder.cbComplete.setChecked(todo.isComplete());
        int flags = todo.isComplete() ? Paint.STRIKE_THRU_TEXT_FLAG : Paint.ANTI_ALIAS_FLAG;
        holder.edtTodo.setPaintFlags(flags);
        handleViewHolderChecked(holder, position);
        handleItemEvent(holder, position);
    }

    private void handleViewHolderChecked(ViewHolder holder, int position) {
        Todo todo = todos.get(position);
        int flag;
        int textColor;
        int backgroundColor;

        if (todo.isComplete()) {
            flag = Paint.STRIKE_THRU_TEXT_FLAG;
            textColor = context.getColor(R.color.itemTodoChecked);
            backgroundColor = context.getColor(R.color.todoBackgroundChecked);

        } else {
            flag = Paint.ANTI_ALIAS_FLAG;
            textColor = context.getColor(R.color.textColor);
            backgroundColor = context.getColor(R.color.todoBackground);
        }

        holder.edtTodo.setPaintFlags(flag);
        holder.edtTodo.setTextColor(textColor);
        holder.todoLayout.setBackgroundColor(backgroundColor);
    }

    private void handleItemEvent(ViewHolder holder, int position) {
        Todo todo = todos.get(position);

        holder.btnCloseFocus.setOnClickListener(v -> {
            Tools.hideSoftKeyBoard((FragmentActivity) context);
            holder.edtTodo.clearFocus();
        });

        holder.edtTodo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.btnCloseFocus.setVisibility(View.VISIBLE);
                holder.btnDrag.setVisibility(View.GONE);
            } else {
                holder.btnCloseFocus.setVisibility(View.GONE);
                holder.btnDrag.setVisibility(View.VISIBLE);
            }
        });

        holder.edtTodo.setOnEditorActionListener((v, actionId, event) -> {
            Tools.hideSoftKeyBoard((FragmentActivity) context);
            holder.edtTodo.clearFocus();
            return true;
        });

        holder.edtTodo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                todo.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        holder.cbComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            todo.setComplete(isChecked);
            recyclerView.post(this::notifyDataSetChanged);
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    public void addNewTodo() {
        int todosLength = todos.size();
        if (todosLength > 0) {
            Log.e(HandleError.tag, "MORE THAN 0");
            Todo todo = todos.get(todosLength - 1);
            if (todo.getTitle().equals(""))
                return;
        }
        todos.add(new Todo());
        notifyItemInserted(todosLength);
        notifyItemRangeChanged(todosLength, todosLength + 1);

        ViewHolder newTodoView = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(todosLength);
        if (newTodoView != null) {
            newTodoView.edtTodo.requestFocus();
        }
    }

    public void removeTodo(int position) {
        if (position < todos.size()) {
            todoUndo = todos.get(position);
            positionUndo = position;
            todos.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void undoRemoveTodo() {
        if (todoUndo != null) {
            todos.add(positionUndo, todoUndo);
            todoUndo = null;
            notifyItemInserted(positionUndo);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbComplete;
        EditText edtTodo;
        ImageButton btnCloseFocus, btnDrag;
        public LinearLayout todoLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbComplete = itemView.findViewById(R.id.cbComplete);
            edtTodo = itemView.findViewById(R.id.edtTodo);
            btnCloseFocus = itemView.findViewById(R.id.btnCloseFocus);
            btnDrag = itemView.findViewById(R.id.btnDrag);
            todoLayout = itemView.findViewById(R.id.todoLayout);
        }
    }
}
