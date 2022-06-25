package com.example.passtwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {



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
        setContentView(R.layout.activity_main);



        ghapi udata = new ghapi();
        udata.execute("https://api.github.com/user/repos");

        try {
            System.out.println(udata.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void show_settings_page(View view) {
        Intent i = new Intent(this, settings_page.class);
        startActivity(i);

    }
}