package com.example.todosapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Interfaces.OptionsTag;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.R;

import java.util.ArrayList;

/**
 * this adapter is used for rendering list tag in tag manage activity
 * */

public class TagManageAdapter extends RecyclerView.Adapter<TagManageAdapter.ViewHolder> {

    Context context;
    ArrayList<Tag> tags;
    OptionsTag optionsTag;

    public TagManageAdapter(Context context, ArrayList<Tag> tags, OptionsTag optionsTag) {
        this.context = context;
        this.tags = tags;
        this.optionsTag = optionsTag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
        Tag tag = tags.get(position);
        holder.tvTagName.setText(tag.getTitle());

        // when user click the option button
        holder.btnOptionsTag.setOnClickListener(v -> showPopUp(holder, tag));
    }

    public void showPopUp(ViewHolder holder, Tag tag) {
        // create a pop up menu with two option:
        //  - update tag
        //  - delete tag
        PopupMenu popupMenu = new PopupMenu(context, holder.btnOptionsTag);
        popupMenu.getMenuInflater().inflate(R.menu.menu_options_tag, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.editTag)
                optionsTag.edit(tag);
            else
                optionsTag.delete(tag);

            return true;
        });

        popupMenu.show();
    }

    @Override
    public int getItemCount() {
        if (tags == null)
            return 0;
        return tags.size();
    }

    public void itemAdded() {
        int positionAdded = tags.size() - 1;
        notifyItemInserted(positionAdded);
        notifyItemRangeChanged(positionAdded, 1);
    }

    public void itemChanged(int position) {
        notifyItemChanged(position);
    }

    public void itemRemoved(int position) {
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tags.size());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout itemTagManageContainer;
        TextView tvTagName;
        ImageButton btnOptionsTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTagManageContainer = itemView.findViewById(R.id.itemTagManageContainer);
            tvTagName = itemView.findViewById(R.id.tvTagName);
            btnOptionsTag = itemView.findViewById(R.id.btnOptionsTag);
        }
    }
}
