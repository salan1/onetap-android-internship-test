package com.example.pc.onetapapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.pc.onetapapp.SharedPref.ApplicationSingleton;

import java.util.ArrayList;

public class UploadedImagesActivity extends AppCompatActivity {

    private GridView gridView;
    private ArrayList<String> urls;
    private GridViewAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_images);

        gridView = (GridView) findViewById(R.id.uploadedGridView);

        urls = ApplicationSingleton.getInstance().getPrefManager().getUrls();

        gridView.setChoiceMode(GridView.CHOICE_MODE_NONE);

        gridAdapter = new GridViewAdapter(this, R.layout.grid_item, urls, true);
        gridView.setAdapter(gridAdapter);
    }

    public void back(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
