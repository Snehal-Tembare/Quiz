package com.example.synerzip.quiz;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by Snehal Tembare on 30/8/16.
 * Copyright © 2016 Synerzip. All rights reserved
 */

public class WebViewActivity extends Activity {

    private static String CLIENT_ID = "735903112029-on44mlvbf103pg763da6466t6vl0bm49.apps.googleusercontent.com";

    private static String REDIRECT_URI = "http://localhost";

    private static String GRANT_TYPE = "authorization_code";

    private static String TOKEN_URL = "https://accounts.google.com/o/oauth2/token";

    private static String OAUTH_URL = "https://accounts.google.com/o/oauth2/auth";

    private static String OAUTH_SCOPE = "https://www.googleapis.com/auth/userinfo.profile";

    private WebView webView;

    private SharedPreferences pref;

    private SharedPreferences.Editor editor;

    private String name;

    private String tok;

    private String authCode;

    private String Code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        pref = getApplicationContext().getSharedPreferences(getString(R.string.quiz), Context.MODE_PRIVATE);

        editor = pref.edit();

        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.loadUrl(OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID + "&scope=" + OAUTH_SCOPE);

        webView.clearCache(true);

        webView.setWebViewClient(new WebViewClient() {

            boolean authComplete = false;

            Intent resultIntent = new Intent();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    WebViewActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    editor.putString("Code", authCode);
                    editor.commit();

                    new TokenGet().execute();
                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith(REDIRECT_URI)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (webView.canGoBack()) {
//            webView.goBack();
//
//        } else {
//            Intent intent=new Intent(WebViewActivity.this,LogInActivity.class);
//            startActivity(intent);
//            finish();
//
//        }
//        return true;
//    }

       private class TokenGet extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(WebViewActivity.this);
            pDialog.setMessage(getString(R.string.connecting_google));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {
            GetAccessToken jParser = new GetAccessToken();
            JSONObject json = jParser.gettoken(TOKEN_URL, Code, CLIENT_ID, REDIRECT_URI, GRANT_TYPE);

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            pDialog.dismiss();
            if (json != null) {

                try {
                    tok = json.getString("access_token");
                    String expire = json.getString("expires_in");
                    String refresh = json.getString("refresh_token");
                    Log.d("Token Access", tok);
                    Log.d("Expire", expire);
                    Log.d("Refresh", refresh);
                    new LoadProfileData().execute();
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.network_error_text), Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
            }
        }

        private class LoadProfileData extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                StringBuilder sb = new StringBuilder();
                try {
                    String link = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token=" + URLEncoder.encode(tok, "UTF-8");
                    URL url = new URL(link);
                    Log.e("Link", link);
                    URLConnection conn = url.openConnection();
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);

                    InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    Log.e("***Data", sb.toString());

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return sb.toString();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Profile profile = gson.fromJson(s, Profile.class);
                name = profile.getName();
                editor.putString(getString(R.string.name), name);
                editor.commit();
                Intent intent = new Intent(WebViewActivity.this, StartActivity.class);
                intent.putExtra(getString(R.string.name), name);
                startActivity(intent);
                editor.clear();
                editor.commit();
                webView.setVisibility(View.INVISIBLE);
                webView.loadUrl("https://mail.google.com/mail/u/0/?logout&hl=en");
                finish();
            }
        }
    }
}
