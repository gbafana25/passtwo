/*



THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
DEALINGS IN THE SOFTWARE.



*/


package com.example.passtwo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;

import android.content.Intent;
import android.content.SharedPreferences;
//import android.hardware.biometrics.BiometricPrompt;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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

        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
        String email = new String();
        String uname = sp.getString("username", "username not found");
        String rpname = sp.getString("repo_name", "repository name not found");

        ghapi udata = new ghapi();

        udata.execute("https://api.github.com/user");
        try {
            JSONObject prf = new JSONObject(udata.get());
            email = prf.getString("email");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        TextView uname_home = (TextView) findViewById(R.id.username_home_page);
        TextView email_home = (TextView) findViewById(R.id.email_home_page);
        TextView repo_home = (TextView) findViewById(R.id.repo_home);
        uname_home.setText(uname);
        email_home.setText(email);
        repo_home.setText(rpname);




    }


    public void show_settings_page(View view) {
        Intent i = new Intent(this, settings_page.class);
        startActivity(i);

    }

    public void show_cred_page(View view) {
        Intent i = new Intent(this, credential_page.class);
        startActivity(i);
    }
}
