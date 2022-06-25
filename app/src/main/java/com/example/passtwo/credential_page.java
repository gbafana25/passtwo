package com.example.passtwo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class credential_page extends AppCompatActivity {

    private class ghapi extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... s) {
            String ret = "";
            SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
            String tok = sp.getString("access_token", "access token not found");

            try {
                URL u = new URL(s[0]);
                HttpURLConnection con = (HttpURLConnection) u.openConnection();
                con.setRequestMethod("GET");
                //con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/vnd.github.v3+json");
                con.setRequestProperty("Authorization", "token "+tok);

                //con.setDoOutput(true);
                try(BufferedReader b = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    StringBuilder res = new StringBuilder();
                    String resline = null;
                    while ((resline = b.readLine()) != null) {
                        res.append(resline.trim());
                    }
                    ret = res.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return ret;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential_page);

        LinearLayout ll = (LinearLayout) findViewById(R.id.list);

        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        String uname = sp.getString("username", "username not found");
        String rep = sp.getString("repo_name", "repo not found");

        ghapi g = new ghapi();
        g.execute("https://api.github.com/repos/"+uname+"/"+rep+"/contents");



        try {
            JSONArray contents = new JSONArray(g.get());
            for(int i = 0; i < contents.length(); i++) {
                JSONObject item = (JSONObject) contents.get(i);
                String dir = item.getString("type");
                if(dir.equals("dir")) {
                    System.out.println(item.getString("name")+"\n");
                    Button te = new Button(this);
                    te.setPadding(100, 100, 100, 100);
                    te.setGravity(Button.TEXT_ALIGNMENT_CENTER);
                    te.setWidth(1200);
                    te.setText(item.getString("name"));
                    ll.addView(te);


                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}