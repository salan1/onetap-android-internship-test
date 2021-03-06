package com.example.pc.onetapapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.example.pc.onetapapp.Network_Utils.ConnectionDetec;
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

    private static final String Client_Secret = "35ad6b498e261428b7bc41f42a64841fbcfceffb";
    private static final String CLIENT_ID = "4c0b698b5b63cee";
    private String authorizationUrl = null;
    private String code = "";

    private GridView gridView;
    private GridViewAdapter gridAdapter;
    private List<String> imageItems;
    private Button uploadImage;
    private ProgressDialog progress;
    private OAuth20Service service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectionDetec internetDetec = new ConnectionDetec(this);
        if (!internetDetec.isConnectingToInternet()) {
            new AlertDialog.Builder(this)
                    .setTitle("Unable to access Internet connection")
                    .setMessage("Sorry unable to access the internet. Press ok to close App")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        String state = Environment.getExternalStorageState();
        if (state.contentEquals(Environment.MEDIA_MOUNTED) || state.contentEquals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            imageItems = new ArrayList<>();
            imageItems = getImageLocations();

            if (ApplicationSingleton.getInstance().getPrefManager().getAccessToken() == null) {
                startService();
            }

        } else {
            Log.v("Error", "External Storage Inaccessible: " + state);
            new AlertDialog.Builder(this)
                    .setTitle("Unable to access storage")
                    .setMessage("Sorry unable to access storage. Press ok to close App")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
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

    public void startService() {
        try {
            service = new ServiceBuilder()
                    .apiKey(CLIENT_ID)
                    .apiSecret(Client_Secret)
                    .build(ImgurApi.instance());
        } catch (Exception exp) {
            exp.printStackTrace();
            Log.d("tag", "Error start service!!!");
        }
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

    public void uploadedImages(View view) {
        Intent intent = new Intent(MainActivity.this, UploadedImagesActivity.class);
        startActivity(intent);
    }

    //Uploads images to imgur
    public void uploadImages(View view) {

        //If device is not authorize yet
        if (ApplicationSingleton.getInstance().getPrefManager().getAccessToken() == null) {

            authorizationUrl = "https://api.imgur.com/oauth2/authorize?response_type=pin&client_id=" + CLIENT_ID;
            Log.d("tag", authorizationUrl);

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.activate_app_dialog);
            dialog.setTitle("Authorize Application");
            TextView authUrlView = (TextView) dialog.findViewById(R.id.activateLinkText);
            authUrlView.setText(authorizationUrl);

            Button okBtn = (Button) dialog.findViewById(R.id.activateAppBtn);
            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    EditText actiCode = (EditText) dialog.findViewById(R.id.activateEditText);
                    code = actiCode.getText().toString();

                    //Gets accessToken using code provided by user
                    Log.d("tag", "Code: " + code);
                    new GetToken().execute(code.trim());
                    dialog.cancel();
                }

            });
            Button cancelButton = (Button) dialog.findViewById(R.id.cancelActivateApp);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });

            dialog.show();
        } else {
            runUpload();
        }

    }

    public void runUpload() {
        ArrayList<String> paths = gridAdapter.getSelectedPaths();

        progress = new ProgressDialog(this);
        progress.setMessage("Uploading Images");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(paths.size());
        progress.show();

        new UploadToImgurTask().execute(paths);
    }


    class UploadToImgurTask extends AsyncTask<ArrayList<String>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(ArrayList<String>... params) {

            //Get passed paths
            ArrayList<String> passed = params[0]; //get passed ArrayList

            for (String tempPath : passed) {
                int refreshTries = 5;
                while (!uploadData(tempPath)) {
                    //If current accessToken is not valid
                    String refresh = ApplicationSingleton.getInstance().getPrefManager().getRefresh();
                    Log.d("tag", "RefreshToken: " + refresh);
                    if (refreshTries == 0) {
                        try {
                            OAuth2AccessToken accessToken = service.refreshAccessToken(refresh);
                            Log.d("tag", "AccessToken response: " + accessToken + " raw response: " + accessToken.getRawResponse() + " expires in: " + accessToken.getExpiresIn());

                            ApplicationSingleton.getInstance().getPrefManager().saveAccessToken(accessToken.getAccessToken());
                            ApplicationSingleton.getInstance().getPrefManager().saveRefresh(accessToken.getRefreshToken());
                            refreshTries--;
                        } catch (IOException exp) {
                            exp.printStackTrace();
                            return false;
                        }
                    } else {
                        return false;
                    }

                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            //Failed to upload
            if (!aBoolean.booleanValue()) {
                progress.cancel();

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Unable to upload")
                        .setMessage("Sorry unable to upload image, please try again")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        }
    }


    public boolean uploadData(String path) {
        final String upload_to = "https://api.imgur.com/3/upload";

        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = new HttpPost(upload_to);

        String errorMessage = "";

        try {

            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("image", new FileBody(new File(path)))
                    .build();

            Log.d("tag", ApplicationSingleton.getInstance().getPrefManager().getAccessToken());
            httpPost.setHeader("Authorization", "Bearer " + ApplicationSingleton.getInstance().getPrefManager().getAccessToken());
            httpPost.setEntity(entity);

            final HttpResponse response = httpClient.execute(httpPost,
                    localContext);

            final String response_string = EntityUtils.toString(response
                    .getEntity());

            final JSONObject json = new JSONObject(response_string);

            Log.d("tag", json.toString());

            JSONObject data = json.optJSONObject("data");
            errorMessage = data.getString("error");

            //If accessToken has expired
            if (errorMessage.equals("The access token provided is invalid.")) {
                Log.d("tag", "AccessToken expired");
                return false;
            }

            //Store the url of the uploaded image
            String uploadedImageUrl = data.getString("link");

            Log.d("tag", "uploaded image url: " + uploadedImageUrl);
            ApplicationSingleton.getInstance().getPrefManager().addUrl(uploadedImageUrl);

            progress.incrementProgressBy(1);

            return true;

        } catch (Exception exp) {
            exp.printStackTrace();
            return false;
        }
    }


    class GetToken extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                final OAuth2AccessToken accessToken = service.getAccessToken(params[0]);

                Log.d("tag", "AccessToken response: " + accessToken + " raw response: " + accessToken.getRawResponse() + " expires in: " + accessToken.getExpiresIn());
                ApplicationSingleton.getInstance().getPrefManager().saveAccessToken(accessToken.getAccessToken().toString());
                ApplicationSingleton.getInstance().getPrefManager().saveRefresh(accessToken.getRefreshToken().toString());

                Log.d("tag", "AccessToken received");
                return true;
            } catch (Exception exp) {
                exp.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean.booleanValue()) {
                runUpload();
            }
        }
    }


}



