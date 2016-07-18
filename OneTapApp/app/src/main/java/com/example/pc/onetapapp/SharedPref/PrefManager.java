package com.example.pc.onetapapp.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PrefManager {

    public static final String TAG = "In SharedPref";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "OneTap";
    private static final String KEY_Uploaded_Urls = "";
    private static final String KEY_token = null;

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

    public ArrayList<String> getUrls() {
        Gson gson = new Gson();
        String json = pref.getString(KEY_Uploaded_Urls, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> arrayList = gson.fromJson(json, type);
        return arrayList;
    }

    public void addUrl(String url) {
        //Converting String to gson and then to ArrayList
        Gson gson = new Gson();
        String json = pref.getString(KEY_Uploaded_Urls, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        ArrayList<String> arrayList = gson.fromJson(json, type);
        arrayList.add(url);

        //Saving urls backing into sharedPrefs
        Gson gsonSave = new Gson();
        String jsonSave = gsonSave.toJson(arrayList);
        editor.putString(TAG, jsonSave);
        editor.commit();
    }

    public void saveAccessToken(String temp){
        editor.putString(KEY_token, temp);
        editor.commit();
    }

    public String getAccessToken(){
        String accessToken = pref.getString(KEY_token, null);
        return accessToken;
    }


}
