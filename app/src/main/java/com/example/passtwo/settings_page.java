package com.example.passtwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;



public class settings_page extends AppCompatActivity {

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

        //Button dtok = (Button) findViewById(R.id.req_device_token);
        //Button ver = (Button) findViewById(R.id.verify_button);

        TextView tstat = (TextView) findViewById(R.id.vcode);
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        String ato = sp.getString("access_token", "");
        if(!ato.isEmpty()) {
            tstat.setText("Device already registered");
            //System.out.println(ato);
        }




    }

    public void get_device_code(View view) {
        EditText cid = (EditText) findViewById(R.id.client_id_field);
        client_id = cid.getText().toString();
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor en = sp.edit();
        if(!client_id.isEmpty()) {
            en.putString("client_id", client_id);
        }

        //System.out.println(client_id);
        GetData gd = new GetData();
        gd.execute("https://github.com/login/device/code?client_id="+client_id+"&scope=repo,read:user");
        try {
            JSONObject respdata = new JSONObject(gd.get());
            String uco = respdata.getString("user_code");
            String dco = respdata.getString("device_code");
            en.putString("device_code", dco);
            TextView code_box = (TextView) findViewById(R.id.vcode);
            code_box.setText(uco);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/login/device"));
            startActivity(i);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        en.commit();

    }

    public void verify_device_code(View view) {
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor edi = sp.edit();
        String cid = sp.getString("client_id", "client id not found");
        String dcode = sp.getString("device_code", "device code not found");
        GetData vc = new GetData();
        vc.execute("https://github.com/login/oauth/access_token?client_id="+cid+"&device_code="+dcode+"&grant_type=urn:ietf:params:oauth:grant-type:device_code");
        try {
            JSONObject ver = new JSONObject(vc.get());
            String token = ver.getString("access_token");
            //System.out.println(token);
            edi.putString("access_token", token);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        edi.commit();
    }

    public void get_prefs() {
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        String tu = sp.getString("username", "username not found");
        String tr = sp.getString("repo_name", "repository name not found");
        System.out.println("Username: " + tu);
        System.out.println("Repository: " + tr);
    }

    public void save_prefs(View view) {
        EditText uname = (EditText) findViewById(R.id.github_username);
        EditText repo_url = (EditText) findViewById(R.id.repo_name);
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
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