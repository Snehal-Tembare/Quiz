package com.example.synerzip.quiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ToolTipPopup;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

/**
 * Created by synerzip on 19/7/16.
 */
public class LogInActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private TextView mInfo;
    private LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private SharedPreferences logInPref;
    private Singleton singleton;

    //Client for accessing Google APIs
    private GoogleApiClient mGoogleApiClient;

    private GoogleSignInOptions gso;

    //Basic account information of the signed in Google user
    private GoogleSignInAccount account;

    //Requestcode for resolutions involving sign-in
    private static final int RC_SIGN_IN = 9001;

    //Requestcode for resolutions involving Log-in
    private static final int RC_LOG_IN = 64206;

    //Result of SignIn account potentially contain a GoogleSignInAccount.
    private GoogleSignInResult result;

    //SharedPreferences to pass th information to other activities
    private SharedPreferences.Editor editor;

    private SignInButton btnSignIn;

    private String name;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);

        webView = (WebView) findViewById(R.id.webview);


        //GoogleSignInOptions is options used to configure the GOOGLE_SIGN_IN_API.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestProfile().build();
        singleton = Singleton.getInstance();
        singleton.setCFlag(false);
        singleton.setCppFlag(false);
        singleton.setJavaFlag(false);
        singleton.setAndroidFlag(false);
        singleton.setJavaScriptFlag(false);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        logInPref = getApplicationContext().getSharedPreferences(getString(R.string.quiz), Context.MODE_PRIVATE);
        editor = logInPref.edit();
        btnSignIn = (SignInButton) findViewById(R.id.button_sign_in);
        btnSignIn.setScopes(gso.getScopeArray());
        mLoginButton = (LoginButton) findViewById(R.id.button_login);
        mLoginButton.setToolTipStyle(ToolTipPopup.Style.BLACK);


        btnSignIn.setSize(SignInButton.SIZE_WIDE);
        btnSignIn.setScopes(gso.getScopeArray());
        mLoginButton.setReadPermissions(getString(R.string.email));

        btnSignIn.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            Log.e(getString(R.string.network_available), "");
            return true;
        } else {
            Log.e(getString(R.string.network_not_available), "");
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (RC_LOG_IN == requestCode) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            if (RC_SIGN_IN == requestCode && 0 != resultCode) {

                //GoogleSignInApi-Api interface for Sign In with Google.
       /*     GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            account = result.getSignInAccount();
           name = account.getDisplayName();
            String email = account.getEmail();

            Intent intent = new Intent(LogInActivity.this, StartActivity.class);
            editor.putString(getString(R.string.name), name);
            editor.commit();
            startActivity(intent);
            finish();*/

            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.could_not_sign_in), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                ProgressDialog dialog=new ProgressDialog(LogInActivity.this);
                dialog.setMessage(getString(R.string.please_wait));
                dialog.show();
                if (isNetworkAvailable()){
                Intent in = new Intent(LogInActivity.this, WebViewActivity.class);
                startActivity(in);
                    dialog.dismiss();
                finish();
                }else {
                    Toast.makeText(getApplicationContext(),getString(R.string.network_not_available),Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.button_login:
                mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                if (null != response.getError()) {
                                    return;
                                } else {
                                    String firstName = object.optString(getString(R.string.name));
                                    logInPref = getApplicationContext().getSharedPreferences(getString(R.string.quiz), Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = logInPref.edit();
                                    editor.putString(getString(R.string.name), firstName);
                                    editor.putString(getString(R.string.key), String.valueOf(RC_LOG_IN));
                                    editor.commit();
                                    Intent intent = new Intent(LogInActivity.this, StartActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }).executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), getString(R.string.login_cancelled), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.login_faild), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    

    /*TODO
     If facebook application is in phone installed
                profile=Profile.getCurrentProfile();
                mInfo.setText("User ID:" + loginResult.getAccessToken().getUserId() + "\n" + "Token:" + loginResult.getAccessToken().getToken()+profile.getFirstName()+" "+profile.getLastName());
                  mInfo.setText(profile.getName());
                Intent intent=new Intent(LogInActivity.this,StartActivity.class);
                intent.putExtra("NAME",profile.getName());
                startActivity(intent);*/
}


