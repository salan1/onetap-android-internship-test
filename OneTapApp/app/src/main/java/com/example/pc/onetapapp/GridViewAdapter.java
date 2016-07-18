package com.example.pc.onetapapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends ArrayAdapter {

    private Context context;
    private List<String> data = new ArrayList<String>();
    private int resourceId;
    private ArrayList<Boolean> states = new ArrayList<>();
    private boolean online;
    LayoutInflater inflater;

    public GridViewAdapter(Context context, int resourceId, List<String> data, Boolean online) {
        super(context, resourceId, data);
        this.resourceId = resourceId;
        this.context = context;
        this.data = data;
        this.online = online;
        inflater = LayoutInflater.from(context);
        for (int i = 0; i < data.size(); i++) {
            states.add(false);
        }
    }

    static class ViewHolder {
        ImageView image;
        TextView imagePath;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            row = inflater.inflate(resourceId, parent, false);
            holder = new ViewHolder();
            holder.imagePath = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        if (online) {

            Glide.with(context)
                    .load(data.get(position))
                    .fitCenter()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.image);
            holder.imagePath.setText(data.get(position));

        } else {

            Glide.with(context)
                    .load(data.get(position))
                    .fitCenter()
                    .centerCrop()
                    .into(holder.image);
            holder.imagePath.setText(data.get(position));
        }

        //Sets the highlight colour
        if (states.get(position)) {
            row.setBackgroundColor(Color.GREEN);
        } else {
            row.setBackgroundColor(Color.TRANSPARENT);
        }

        return row;
    }

    //Displays a border when an image is selected
    public void changeSelection(int position, View convertView) {
        if (states.get(position)) {
            states.set(position, false);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            states.set(position, true);
            convertView.setBackgroundColor(Color.GREEN);
        }
    }

    //Checks if there are any images selected
    public boolean checkStatus() {
        for (int i = 0; i < states.size(); i++) {
            if (states.get(i)) {
                return true;
            }
        }
        return false;
    }

    //Returns array of paths of selected images
    public ArrayList<String> getSelectedPaths() {
        ArrayList<String> tempPaths = new ArrayList<>();

        for (int i = 0; i < states.size(); i++) {
            if (states.get(i)) {
                tempPaths.add(data.get(i));
            }
        }

        return tempPaths;
    }

}