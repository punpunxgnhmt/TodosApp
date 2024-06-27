package com.example.todosapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todosapp.Interfaces.HandleTagClick;
import com.example.todosapp.Models.Tag;
import com.example.todosapp.R;

import java.util.ArrayList;

/**
 *  this adapter will be rendering list tag in task fragment
 * @see #tagSelected: this variable to save the postion of selected tag.
 * */
public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ViewHolder> {

    Context context;
    ArrayList<Tag> tags;
    HandleTagClick handleTagClick;
    int tagSelected;

    public TagAdapter(Context context, ArrayList<Tag> tags, HandleTagClick handleTagClick) {
        this.context = context;
        this.tags = tags;
        this.handleTagClick = handleTagClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (position == 0) {
            holder.tvTagName.setText(R.string.All);
        } else {
            holder.tvTagName.setText(tags.get(position).getTitle());
        }



        // if position of the tags equals to tagSelected -> set background with primaryColor and set text color with white
        // else set background with primary color light and set text color with textColorLight
        if (tagSelected == position) {
            holder.tvTagName.setTextColor(context.getColor(R.color.black));
            holder.tagContainer.setBackgroundTintList(context.getColorStateList(R.color.primaryColor));
        } else {
            holder.tvTagName.setTextColor(context.getColor(R.color.textColorLight));
            holder.tagContainer.setBackgroundTintList(context.getColorStateList(R.color.primaryColorLight));
        }

        // avoid the tag view have the width over the text width
        ViewGroup.LayoutParams layoutParams = holder.tvTagName.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.tvTagName.setLayoutParams(layoutParams);

        // when user click a tag -> call a interface function and change selectedItem.
        holder.tagContainer.setOnClickListener(v -> {
            if (handleTagClick != null) {
                handleTagClick.tagClick(tags.get(position), position);
            }
            changeItemSelected(position);
        });
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

        int newSelectedTag = 0;
        if (handleTagClick != null) {
            handleTagClick.tagClick(tags.get(newSelectedTag), newSelectedTag);
        }
        changeItemSelected(newSelectedTag);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeItemSelected(int newIndex) {
        int oldSelectedIndex = tagSelected;
        tagSelected = newIndex;
        notifyItemChanged(oldSelectedIndex);
        notifyItemChanged(tagSelected);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView tagContainer;
        TextView tvTagName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagContainer = itemView.findViewById(R.id.tagContainer);
            tvTagName = itemView.findViewById(R.id.tvTagName);
        }
    }
}
