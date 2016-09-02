package com.example.synerzip.quiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * Created by Snehal Tembare on 26/7/16.
 * Copyright © 2016 Synerzip. All rights reserved
 */

public class ResultActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView tvUserName;
    private TextView tvGrade;
    private TextView tvTotalMarks;
    private Button btnExit;
    private CallbackManager mCallbackManager;
    private SharedPreferences resultPref;
    //    private int totalMarks;
    private SharedPreferences.Editor editor;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_result);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();


        tvUserName = (TextView) findViewById(R.id.text_user_name);
        tvGrade = (TextView) findViewById(R.id.text_grade);
        tvTotalMarks = (TextView) findViewById(R.id.text_total_marks);
        btnExit = (Button) findViewById(R.id.button_exit);
        resultPref = getApplicationContext().getSharedPreferences(getString(R.string.quiz), Context.MODE_PRIVATE);
        editor = resultPref.edit();

        String name = resultPref.getString(getString(R.string.name), "");
        //  int score=resultPref.getInt(getString(R.string.total),0);
//        totalMarks=resultPref.getInt(getString(R.string.subcount),0);
//        totalMarks=totalMarks*10;


        tvUserName.setText(name);
        tvTotalMarks.setText(getString(R.string.completed));
        tvGrade.setText(getString(R.string.thanks));

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();

                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
                editor.clear();
                editor.apply();
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        LoginManager.getInstance().logOut();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                editor.clear();
                editor.apply();
                finish();
            }
        });


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
