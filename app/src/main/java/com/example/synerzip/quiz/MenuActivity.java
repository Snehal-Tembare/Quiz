package com.example.synerzip.quiz;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuActivity extends FragmentActivity implements GoogleApiClient.OnConnectionFailedListener {
    private ListView mListView;

    private TextView tvUserName;
    private Button btnSubmit;
    private TextView txtAttemptAll;

    private String name;
    private SharedPreferences mainPref;
    private List<String> mSubjects;
    SharedPreferences.Editor editor = null;
    private Singleton singleton;
    private int totalMarks = 0;

    private CustomAdapter adapter;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions gso;
    private int totalScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        singleton = Singleton.getInstance();

        mListView = (ListView) findViewById(R.id.listView);

        btnSubmit = (Button) findViewById(R.id.button_submit_test);
        tvUserName = (TextView) findViewById(R.id.text_main_user_name);
        txtAttemptAll = (TextView) findViewById(R.id.text_menu_instruction);

        mainPref = getApplicationContext().getSharedPreferences(getString(R.string.quiz), Context.MODE_PRIVATE);
        editor = mainPref.edit();
        name = mainPref.getString(getString(R.string.name), "");
        tvUserName.setText(getString(R.string.logged_in) + " " + name);

        mSubjects = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.subject_names)));

        adapter = new CustomAdapter(getApplicationContext(), R.layout.list_item, mSubjects);
        mListView.setAdapter(adapter);

        editor.putInt(getString(R.string.subcount), mSubjects.size());
        editor.commit();

        totalMarks = mainPref.getInt(getString(R.string.total), 0);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(MenuActivity.this, QuestionActivity.class);
                ;

                switch (position) {
                    case 0:
                        if (!singleton.isCFlag()) {
                            intent.putExtra(getString(R.string.position), position);
                            startActivity(intent);
                            finish();
                        } else {
                            view.clearAnimation();
                            Toast.makeText(getApplicationContext(), getString(R.string.attempted_test), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        if (!singleton.isCppFlag()) {
                            intent.putExtra(getString(R.string.position), position);
                            startActivity(intent);
                            finish();
                        } else {
                            view.clearAnimation();
                            Toast.makeText(getApplicationContext(), getString(R.string.attempted_test), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 2:
                        if (!singleton.isJavaFlag()) {
                            intent.putExtra(getString(R.string.position), position);
                            startActivity(intent);
                            finish();
                        } else {
                            view.clearAnimation();
                            Toast.makeText(getApplicationContext(), getString(R.string.attempted_test), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        if (!singleton.isAndroidFlag()) {
                            intent.putExtra(getString(R.string.position), position);
                            startActivity(intent);
                            finish();
                        } else {
                            view.clearAnimation();
                            Toast.makeText(getApplicationContext(), getString(R.string.attempted_test), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 4:
                        if (!singleton.isJavaScriptFlag()) {
                            intent.putExtra(getString(R.string.position), position);
                            startActivity(intent);
                            finish();
                        } else {
                            view.clearAnimation();
                            Toast.makeText(getApplicationContext(), getString(R.string.attempted_test), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        return;
                }
            }
        });
        if (singleton.isCFlag() && singleton.isCppFlag() && singleton.isAndroidFlag() && singleton.isJavaFlag() && singleton.isJavaScriptFlag()) {
            btnSubmit.setVisibility(View.VISIBLE);
            txtAttemptAll.setVisibility(View.GONE);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    totalMarks = mainPref.getInt(getString(R.string.total), 0);
                    totalScore = totalMarks + totalScore;
                    editor.putInt("total", totalScore);
                    editor.commit();

                    FileWriter fw = new FileWriter(getResources().getString(R.string.file_path), true);

                    CSVWriter writer = new CSVWriter(fw, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
                    writer.writeNext(new String[]{name, String.valueOf(totalScore)});
                    writer.close();
                    new SendingEmail().execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent resultIntent = new Intent(MenuActivity.this, ResultActivity.class);
                startActivity(resultIntent);
                finish();
            }
        });

    }

    private class SendingEmail extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Mail m = new Mail(getResources().getString(R.string.app_mail), getResources().getString(R.string.app_password));
            String[] _toArray = {getResources().getString(R.string.to_mail)};
            m.set_to(_toArray);
            m.set_subject(getResources().getString(R.string.test_result));
            m.set_body(getResources().getString(R.string.body));

            try {
                m.addAttachment(getResources().getString(R.string.file_path), getResources().getString(R.string.file_name));
                m.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewTreeObserver viewTreeObserver = mListView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                for (String subject : mSubjects) {
                    if (subject.equalsIgnoreCase(mSubjects.get(0))) {
                        if (singleton.isCFlag())
                            /*We can set background color to selected item of List
                            mListView.getChildAt(mSubjects.indexOf(subject)).setBackgroundColor(ContextCompat.getColor(MenuActivity.this,R.color.red));*/
                            mListView.getChildAt(mSubjects.indexOf(subject)).findViewById(R.id.image_tick).setVisibility(View.VISIBLE);
                        mListView.getChildAt(mSubjects.indexOf(subject)).setClickable(false);

                    } else if (subject.equalsIgnoreCase(mSubjects.get(1))) {
                        if (singleton.isCppFlag())
                            mListView.getChildAt(mSubjects.indexOf(subject)).findViewById(R.id.image_tick).setVisibility(View.VISIBLE);
                    } else if (subject.equalsIgnoreCase(mSubjects.get(2))) {
                        if (singleton.isJavaFlag())
                            mListView.getChildAt(mSubjects.indexOf(subject)).findViewById(R.id.image_tick).setVisibility(View.VISIBLE);
                    } else if (subject.equalsIgnoreCase(mSubjects.get(3))) {
                        if (singleton.isAndroidFlag())
                            mListView.getChildAt(mSubjects.indexOf(subject)).findViewById(R.id.image_tick).setVisibility(View.VISIBLE);
                    } else {
                        if (singleton.isJavaScriptFlag())
                            mListView.getChildAt(mSubjects.indexOf(subject)).findViewById(R.id.image_tick).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MenuActivity.this);
        dialog.setMessage(getResources().getString(R.string.menu_exit_app_message));

        dialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Logout if logged with facebook
                LoginManager.getInstance().logOut();

                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
                editor.clear();
                editor.commit();
                finish();


            }
        });
        dialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}

