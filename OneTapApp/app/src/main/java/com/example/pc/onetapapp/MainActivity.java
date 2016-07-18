package com.example.pc.onetapapp;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.example.pc.onetapapp.SharedPref.ApplicationSingleton;
import com.github.scribejava.apis.ImgurApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String AUTHORIZATION_URL = "https://api.imgur.com/oauth2/authorize";
    private static final String CLIENT_ID = "4c0b698b5b63cee";
    private OAuth2AccessToken accessToken;
    private String authorizationUrl = null;
    private String code = "";

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private List<String> imageItems;
    Button uploadImage;

    final OAuth20Service service = new ServiceBuilder()
            .apiKey(AUTHORIZATION_URL)
            .apiSecret(CLIENT_ID)
            .build(ImgurApi.instance());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String state = Environment.getExternalStorageState();
        if (state.contentEquals(Environment.MEDIA_MOUNTED) || state.contentEquals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            imageItems = new ArrayList<>();
            imageItems = getImageLocations();
        } else {
            Log.v("Error", "External Storage Unaccessible: " + state);
            //EXIT APP HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        }

        uploadImage = (Button) findViewById(R.id.uploadImageBtn);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

        gridAdapter = new GridViewAdapter(this, R.layout.grid_item, imageItems, false);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                gridAdapter.changeSelection(position, v);
                //checks if any images is selected and displays upload button
                if (gridAdapter.checkStatus()) {
                    uploadImage.setVisibility(View.VISIBLE);
                } else {
                    uploadImage.setVisibility(View.GONE);
                }
            }
        });
        gridView.setAdapter(gridAdapter);

    }

    public ArrayList<String> getImageLocations() {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }

        return listOfAllImages;
    }

    public void uploadedImages(View view){
        Intent intent = new Intent(MainActivity.this, UploadedImagesActivity.class);
        startActivity(intent);
    }

    //Uploads images to imgur
    public void uploadImages(View view) {

        authorizationUrl = service.getAuthorizationUrl();

        // Check if refresh_token is needed
        try {

        } catch (Exception exp) {
        }


        //If device is not authorize yet
        if (authorizationUrl.isEmpty()) {

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.activate_app_dialog);
            dialog.setTitle("Authorize Application");
            TextView authUrlView = (TextView) findViewById(R.id.activateLinkText);
            authUrlView.setText(authorizationUrl);

            Button okBtn = (Button) dialog.findViewById(R.id.activateAppBtn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText actiCode = (EditText) findViewById(R.id.activateEditText);
                    code = actiCode.getText().toString();

                    //Gets accessToken using code provided by user
                    try {
                        accessToken = service.getAccessToken(code);
                        Log.d("tag", "AccessToken received");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }


        ArrayList<String> paths = gridAdapter.getSelectedPaths();

        for (int i = 0; i < paths.size(); i++) {
            new UploadToImgurTask().execute(paths.get(i));
        }
    }

    // Here is the upload task
    class UploadToImgurTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            final String upload_to = "https://api.imgur.com/3/upload";

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(upload_to);

            try {
                HttpEntity entity = MultipartEntityBuilder.create()
                        .addPart("image", new FileBody(new File(params[0])))
                        .build();


                httpPost.setHeader("Authorization", "Bearer " + accessToken);
                httpPost.setEntity(entity);

                final HttpResponse response = httpClient.execute(httpPost,
                        localContext);

                final String response_string = EntityUtils.toString(response
                        .getEntity());

                final JSONObject json = new JSONObject(response_string);

                Log.d("tag", json.toString());

                //Store the url of the uploaded image
                JSONObject data = json.optJSONObject("data");
                String uploadedImageUrl = data.optString("link");
                ApplicationSingleton.getInstance().getPrefManager().addUrl(uploadedImageUrl);
                Log.d("tag", "uploaded image url : " + uploadedImageUrl);

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean.booleanValue()) {
                Intent intent = new Intent(MainActivity.this, UploadedImagesActivity.class);
                startActivity(intent);
            }
        }
    }

}



