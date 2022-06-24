package com.example.passtwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;


public class settings_page extends AppCompatActivity {

    OkHttpClient gh = new OkHttpClient();
    String code_url = "https://github.com/login/device/code";
    String client_id = "";

    private class GetData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... st) {
            String test = "";
            try {
                URL u = new URL(st[0]);
                HttpURLConnection uc = (HttpURLConnection) u.openConnection();
                uc.setRequestMethod("POST");
                uc.setRequestProperty("Content-Type", "application/json");
                uc.setRequestProperty("Accept", "application/json");
                uc.setDoOutput(true);
                uc.connect();
                //System.out.println(uc.getInputStream().toString());
                try(BufferedReader b = new BufferedReader(
                        new InputStreamReader(uc.getInputStream(), "utf-8"))) {
                    StringBuilder res = new StringBuilder();
                    String resline = null;
                    while((resline = b.readLine()) != null) {
                        res.append(resline.trim());
                    }
                    test = res.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return test;
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        Intent i = getIntent();

        /*
        GetData gd = new GetData();
        gd.execute("https://api.github.com/users/gbafana25");
        try {
            // use `get` method to return proper value from async function
            System.out.println(gd.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

         */


    }

    public void get_device_code(View view) {
        //Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://duckduckgo.com"));
        //startActivity(i);
        EditText cid = (EditText) findViewById(R.id.client_id_field);
        client_id = cid.getText().toString();
        //System.out.println(client_id);
        GetData gd = new GetData();
        gd.execute("https://github.com/login/device/code?client_id="+client_id);
        try {
            JSONObject respdata = new JSONObject(gd.get());
            String uco = respdata.getString("user_code");
            System.out.println(uco);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void get_prefs() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        String tu = sp.getString("username", "username not found");
        String tr = sp.getString("repo_name", "repository name not found");
        System.out.println("Username: " + tu);
        System.out.println("Repository: " + tr);
    }

    public void save_prefs(View view) {
        EditText uname = (EditText) findViewById(R.id.github_username);
        EditText repo_url = (EditText) findViewById(R.id.repo_name);
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();

        String ue = uname.getText().toString();
        String re = repo_url.getText().toString();
        if(!ue.isEmpty()) {
            e.putString("username", ue);
        }

        if(!re.isEmpty()) {
            e.putString("repo_name", re);
        }

        e.commit();
        //get_prefs();
    }
}