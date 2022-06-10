package com.example.todosapp.Helper;

import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Adapters.TodoAdapter;
import com.example.todosapp.Interfaces.ItemTouchListener;

public class TodoAdapterItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final ItemTouchListener itemTouchListener;

    public TodoAdapterItemTouchHelper(int dragDirs, int swipeDirs, ItemTouchListener itemTouchListener) {
        super(dragDirs, swipeDirs);
        this.itemTouchListener = itemTouchListener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        if (itemTouchListener != null) {
            itemTouchListener.onSwiped(viewHolder);
        }
    }


    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        if (viewHolder != null) {
            View view = ((TodoAdapter.ViewHolder) viewHolder).todoLayout;
            getDefaultUIUtil().onSelected(view);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View view = ((TodoAdapter.ViewHolder) viewHolder).todoLayout;
        getDefaultUIUtil().onDrawOver(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View view = ((TodoAdapter.ViewHolder) viewHolder).todoLayout;
        getDefaultUIUtil().onDraw(c, recyclerView, view, dX, dY, actionState, isCurrentlyActive);
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        View view = ((TodoAdapter.ViewHolder) viewHolder).todoLayout;
        getDefaultUIUtil().clearView(view);
        super.clearView(recyclerView, viewHolder);
    }

}
