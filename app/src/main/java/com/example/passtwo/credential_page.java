package com.example.passtwo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.util.io.Streams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    private class rdloader extends AsyncTask<String, String, byte[]> {

        @Override
        protected byte[] doInBackground(String... s) {
            byte[] ret = new byte[512];
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);
            String tok = sp.getString("access_token", "access token not found");

            try {
                URL u = new URL(s[0]);
                int i;
                HttpURLConnection con = (HttpURLConnection) u.openConnection();
                con.setRequestMethod("GET");
                //con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept", "application/vnd.github.v3+json");
                con.setRequestProperty("Authorization", "token "+tok);
                InputStream is = con.getInputStream();
                while((i = is.read(ret)) != -1) {
                    bs.write(ret, 0, i);
                }
                is.close();
                //System.out.println(Arrays.toString(r));


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return bs.toByteArray();
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
                    //System.out.println(item.getString("name")+"\n");
                    Button te = new Button(this);
                    te.setPadding(100, 100, 100, 100);
                    te.setGravity(Button.TEXT_ALIGNMENT_CENTER);
                    te.setWidth(1200);
                    te.setText(item.getString("name"));
                    ll.addView(te);
                    te.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                getfiles(item.getString("url"), item.getString("name"), uname, rep);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });



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

    View.OnClickListener getfiles(String url, String dirname, String uname, String rep) {
        ghapi d = new ghapi();
        d.execute(url);
        JSONArray c = null;
        File cdir = getDir(dirname, Context.MODE_PRIVATE);
        List<String> ta = new ArrayList<>();
        //System.out.println(Arrays.toString(cdir.listFiles()));
        try {
            c = new JSONArray(d.get());
            for(int j = 0; j < c.length(); j++) {
                rdloader raw = new rdloader();
                JSONObject o = (JSONObject) c.get(j);
                String du = o.getString("download_url");
                String enc_name = o.getString("name");
                ta.add(enc_name);
                File cf = new File(cdir, enc_name);
                if(!cf.exists()) {
                    raw.execute(du);
                    byte[] rf = raw.get();
                    try(FileOutputStream os = new FileOutputStream(cf)) {
                        os.write(rf);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
            String[] lt = new String[ta.size()];
            ta.toArray(lt);
            final int[] cit = {0};
            AlertDialog.Builder bd = new AlertDialog.Builder(new ContextThemeWrapper(credential_page.this, R.style.Theme_Passtwo));
            bd.setTitle(dirname)
                    .setSingleChoiceItems(lt, cit[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            cit[0] = i;
                        }
                    })
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //System.out.println(lt[cit[0]]);
                            decrypt_file(dirname, lt[cit[0]], uname, rep);

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
            AlertDialog dia = bd.create();
            dia.show();



        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return null;
    }

    public void decrypt_file(String dir, String fname, String uname, String rep) {
        //byte[] prk = get_private_key(uname, rep);
        download_private_key(uname, rep);
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        File cdir = getDir(dir, Context.MODE_PRIVATE);
        File kd = getDir("keys", Context.MODE_PRIVATE);
        File f = new File(cdir, fname);
        File privk = new File(kd, "priv.pgp");

        byte[] bu = new byte[(int) f.length()];
        byte[] pkey = new byte[(int) privk.length()];
        //System.out.println(f.getAbsolutePath());


        try {
            InputStream fis = new FileInputStream(f);
            InputStream pin = new FileInputStream(privk);
            fis = PGPUtil.getDecoderStream(fis);
            JcaPGPObjectFactory jfa = new JcaPGPObjectFactory(fis);
            PGPEncryptedDataList dlist;

            Object o = jfa.nextObject();

            if(o instanceof PGPEncryptedDataList) {
                dlist = (PGPEncryptedDataList) o;
            } else {
                dlist = (PGPEncryptedDataList) jfa.nextObject();
            }

            Iterator it = dlist.getEncryptedDataObjects();
            PGPPrivateKey pri = null;
            PGPPublicKeyEncryptedData pbe = null;
            PGPSecretKeyRingCollection ring = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(pin), new JcaKeyFingerprintCalculator());

            while(pri == null && it.hasNext()) {
                pbe = (PGPPublicKeyEncryptedData) it.next();
                // doesn't give error when correct password is entered
                // still not printing data
                pri = PGPfuncs.findSecretKey(ring, pbe.getKeyID(), "notpass".toCharArray());
            }

            InputStream cl = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(pri));
            JcaPGPObjectFactory pla = new JcaPGPObjectFactory(cl);

            Object po = pla.nextObject();
            if(po instanceof PGPCompressedData) {
                PGPCompressedData comp = (PGPCompressedData) po;
                JcaPGPObjectFactory fa = new JcaPGPObjectFactory(comp.getDataStream());

                fa.nextObject();
            }

            // fix: not printing password, no errors
            if(po instanceof PGPLiteralData) {
                PGPLiteralData ld = (PGPLiteralData) po;
                InputStream rdata = ld.getDataStream();
                InputStreamReader isr = new InputStreamReader(rdata, StandardCharsets.UTF_8);
                System.out.println(isr.read());
            }
            //fis.read(bu);
            //fis.close();

            //System.out.println(Arrays.toString(bu));
            //InputStream in = PGPUtil.getDecoderStream(fis);


            //System.out.println(Arrays.toString(prv));
            pin.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    public void download_private_key(String uname, String rep) {
        File kd = getDir("keys", Context.MODE_PRIVATE);
        File f = new File(kd, "priv.pgp");

        rdloader pr = new rdloader();
        pr.execute("https://raw.githubusercontent.com/"+uname+"/"+rep+"/main/priv.pgp");
        try {
                byte[] pf = pr.get();
                FileOutputStream os = new FileOutputStream(f);
                os.write(pf);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}