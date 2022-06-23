package com.example.passtwo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import okhttp3.OkHttpClient;


public class settings_page extends AppCompatActivity {

    OkHttpClient gh = new OkHttpClient();

    private class GetData extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... st) {
            String test = "";
            try {
                URL u = new URL(st[0]);
                HttpURLConnection uc = (HttpURLConnection) u.openConnection();
                InputStream in = new BufferedInputStream(uc.getInputStream());
                Scanner sc = new Scanner(in).useDelimiter("\\A");
                test = sc.hasNext() ? sc.next() : "";
                System.out.println(test);
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


        TextView dinfo = (TextView) findViewById(R.id.dinfo);

        GetData gd = new GetData();
        gd.execute("https://api.github.com/users/gbafana25");





    }
}