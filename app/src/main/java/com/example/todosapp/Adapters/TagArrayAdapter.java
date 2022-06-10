package com.example.todosapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todosapp.Models.Tag;
import com.example.todosapp.R;

import java.util.List;


/**
 * This adapter is used for rendering list tag of dropdown view
 * */
public class TagArrayAdapter extends ArrayAdapter<Tag> {
    public TagArrayAdapter(@NonNull Context context, int resource, @NonNull List<Tag> objects) {
        super(context, resource, objects);
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_array_selected, parent, false);
        TextView tvTagNameSelected = view.findViewById(R.id.tvTagNameSelected);

        Tag tag = this.getItem(position);

        if(tag != null){
            tvTagNameSelected.setText(tag.toString());
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View view, @NonNull ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag_array, parent, false);
        TextView tvTagName = (TextView) view;

        Tag tag = this.getItem(position);

        if(tag != null){
            tvTagName.setText(tag.toString());
        }

        return view;
    }
}
