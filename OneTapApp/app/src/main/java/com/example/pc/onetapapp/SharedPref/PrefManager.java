package com.example.pc.onetapapp.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class PrefManager {

    public static final String TAG = "In SharedPref";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "OneTap";
    private static final String KEY_Uploaded_Urls = null;
    private static final String KEY_token = null;
    private static final String KEY_Refresh_Token = null;

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

        ArrayList<String> array = new ArrayList<String>();
        String jArrayString = pref.getString(KEY_Uploaded_Urls, "NOPREFSAVED");
        if (jArrayString.matches("NOPREFSAVED")) return getDefaultArray();
        else {
            try {
                JSONArray jArray = new JSONArray(jArrayString);
                for (int i = 0; i < jArray.length(); i++) {
                    array.add(jArray.getString(i));
                }
                return array;
            } catch (JSONException e) {
                return getDefaultArray();
            }
        }
    }

    public void addUrl(String url) {

        ArrayList<String> urls = getUrls();
        urls.add(url);

        for(String temp: urls){
            Log.d("tag", temp);
        }
        JSONArray jArray = new JSONArray(urls);
        editor.remove(KEY_Uploaded_Urls);
        editor.putString(KEY_Uploaded_Urls, jArray.toString());
        editor.commit();
    }

    private ArrayList<String> getDefaultArray() {
        ArrayList<String> array = new ArrayList<String>();
        return array;
    }

    public void saveAccessToken(String temp) {
        editor.putString(KEY_token, temp);
        editor.commit();
    }

    public String getAccessToken() {
        String accessToken = pref.getString(KEY_token, null);
        return accessToken;
    }

    public void saveRefresh(String temp){
        editor.putString(KEY_Refresh_Token, temp);
        editor.commit();
    }

    public String getRefresh(){
        String refreshToken = pref.getString(KEY_Refresh_Token, null);
        return refreshToken;
    }


}
