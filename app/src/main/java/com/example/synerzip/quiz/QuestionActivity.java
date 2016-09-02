package com.example.synerzip.quiz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.transition.Fade;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Snehal Tembare on 6/7/16.
 * Copyright © 2016 Synerzip. All rights reserved
 */

public class QuestionActivity extends Activity implements View.OnClickListener {
    private Quiz quiz;
    private C c;
    private Java java;
    private Android android;
    private Cpp cpp;
    private JavaScript javaScript;
    private List<Questions> questions = new ArrayList<>();

    private TextView tvQuestion;
    private TextView tvOpA;
    private TextView tvOpB;
    private TextView tvOpC;
    private TextView tvOpD;
    private TextView tvTimeRemain;
    private TextView tvUserName;

    private ImageButton mImgexit;

    private Button btnSkipQuestion;

    private static int mMarks;
    private static int total;
    private static int mIndex;
    private static int mQNo;
    private String mAnswer;
    private int mPosition;
    private final int TIMEOUT = 300;
    private boolean mFlag = true;

    private String name;
    private SharedPreferences questionPre;
    private long mLastClickTime = 0;

    private CountDownTimer mCountTimer;
    private Singleton singleton;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        getWindow().setExitTransition(new Fade(Fade.OUT).setDuration(200));
        singleton = Singleton.getInstance();
        Intent intent = getIntent();

        mPosition = intent.getExtras().getInt(getString(R.string.position));

        tvQuestion = (TextView) findViewById(R.id.text_question);
        tvTimeRemain = (TextView) findViewById(R.id.text_remaining_time);
        mImgexit = (ImageButton) findViewById(R.id.image_question_exit);

        tvOpA = (TextView) findViewById(R.id.text_option_a);
        tvOpB = (TextView) findViewById(R.id.text_option_b);
        tvOpC = (TextView) findViewById(R.id.text_option_c);
        tvOpD = (TextView) findViewById(R.id.text_option_d);
        tvUserName = (TextView) findViewById(R.id.text_user_name);

        questionPre = getApplicationContext().getSharedPreferences(getString(R.string.quiz), Context.MODE_PRIVATE);
        editor = questionPre.edit();

        name = questionPre.getString(getString(R.string.name), "");
        tvUserName.setText(getString(R.string.logged_in) + " " + name);

        tvOpA.setOnClickListener(this);
        tvOpB.setOnClickListener(this);
        tvOpC.setOnClickListener(this);
        tvOpD.setOnClickListener(this);

        btnSkipQuestion = (Button) findViewById(R.id.button_skip_question);
        loadData();
        mImgexit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(QuestionActivity.this);
                dialog.setMessage(getResources().getString(R.string.exit_message));

                dialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCountTimer.cancel();
                        Intent mainIntent = new Intent(QuestionActivity.this, MenuActivity.class);
                        int totalScore = questionPre.getInt(getString(R.string.total), 0);
                        editor.putInt(getString(R.string.total), mMarks + totalScore);
                        editor.apply();
                        startActivity(mainIntent);
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
        });

        btnSkipQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadQuestion();
            }
        });

        mCountTimer = new CountDownTimer(45000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimeRemain.setText(getResources().getString(R.string.time_remaining) + " " + millisUntilFinished / 60000 + ":" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                tvTimeRemain.setText(getString(R.string.time_finished));
                loadQuestion();
                if (mFlag == false) {
                    mCountTimer.cancel();
                    Intent scoreIntent = new Intent(QuestionActivity.this, MenuActivity.class);
                    scoreIntent.putExtra(getString(R.string.score), mMarks);
                    scoreIntent.putExtra(getString(R.string.outof), questions.size());
                    startActivity(scoreIntent);
                    finish();
                } else {
                    return;
                }
            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }


    public void loadData() {
        mIndex = 0;
        mQNo = 1;
        mMarks = 0;
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            InputStream stream = new BufferedInputStream(getAssets().open("Question.json"));
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        switch (mPosition) {
            case 0:
                singleton.setCFlag(true);
                quiz = gson.fromJson(sb.toString(), Quiz.class);
                c = quiz.getC();
                questions = c.getQuestions();
                break;
            case 1:
                singleton.setCppFlag(true);
                quiz = gson.fromJson(sb.toString(), Quiz.class);
                cpp = quiz.getCpp();
                questions = cpp.getQuestions();
                break;
            case 2:
                singleton.setJavaFlag(true);
                quiz = gson.fromJson(sb.toString(), Quiz.class);
                java = quiz.getJava();
                questions = java.getQuestions();
                break;
            case 3:
                singleton.setAndroidFlag(true);
                quiz = gson.fromJson(sb.toString(), Quiz.class);
                android = quiz.getAndroid();
                questions = android.getQuestions();
                break;
            case 4:
                singleton.setJavaScriptFlag(true);
                quiz = gson.fromJson(sb.toString(), Quiz.class);
                javaScript = quiz.getJavaScript();
                questions = javaScript.getQuestions();
                break;
        }

        tvQuestion.setText(getString(R.string.question) + mQNo + ". " + questions.get(mIndex).getQuestion());
        tvOpA.setText(questions.get(mIndex).getA());
        tvOpB.setText(questions.get(mIndex).getB());
        tvOpC.setText(questions.get(mIndex).getC());
        tvOpD.setText(questions.get(mIndex).getD());
        mAnswer = questions.get(mIndex).getAnswer();

    }


    public void setOptinFalse() {
        tvOpA.setClickable(false);
        tvOpB.setClickable(false);
        tvOpC.setClickable(false);
        tvOpD.setClickable(false);
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();

        switch (v.getId()) {
            case R.id.text_option_a:
                if (tvOpA.getText().equals(mAnswer)) {
                    tvOpA.setBackgroundColor(Color.GREEN);
                    mMarks++;
                    setOptinFalse();
                    loadQuestion();
                } else {
                    tvOpA.setBackgroundColor(Color.RED);
                    setOptinFalse();
                    loadQuestion();
                }
                break;
            case R.id.text_option_b:

                if (tvOpB.getText().equals(mAnswer)) {
                    tvOpB.setBackgroundColor(Color.GREEN);
                    mMarks++;
                    setOptinFalse();
                    loadQuestion();
                } else {
                    tvOpB.setBackgroundColor(Color.RED);
                    setOptinFalse();
                    loadQuestion();
                }
                break;
            case R.id.text_option_c:
                if (tvOpC.getText().equals(mAnswer)) {
                    tvOpC.setBackgroundColor(Color.GREEN);
                    mMarks++;
                    setOptinFalse();
                    loadQuestion();
                } else {
                    tvOpC.setBackgroundColor(Color.RED);
                    setOptinFalse();
                    loadQuestion();
                }
                break;
            case R.id.text_option_d:
                if (tvOpD.getText().equals(mAnswer)) {
                    tvOpD.setBackgroundColor(Color.GREEN);
                    mMarks++;
                    setOptinFalse();
                    loadQuestion();
                } else {
                    tvOpD.setBackgroundColor(Color.RED);
                    setOptinFalse();
                    loadQuestion();
                }
                break;
        }
    }

    public void loadQuestion() {


        mCountTimer.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tvOpA.setBackground(getResources().getDrawable(R.drawable.border, null));
                tvOpB.setBackground(getResources().getDrawable(R.drawable.border, null));
                tvOpC.setBackground(getResources().getDrawable(R.drawable.border, null));
                tvOpD.setBackground(getResources().getDrawable(R.drawable.border, null));

                tvOpB.setClickable(true);
                tvOpC.setClickable(true);
                tvOpA.setClickable(true);
                tvOpD.setClickable(true);


                if (questions.size() - 1 == mIndex) {
                    setOptinFalse();

                    Intent scoreIntent = new Intent(QuestionActivity.this, MenuActivity.class);
                    total = questionPre.getInt(getString(R.string.total), 0);
                    total = total + mMarks;
                    editor.putInt(getString(R.string.total), total);
                    editor.apply();
                    mFlag = false;
                    mCountTimer.cancel();
                    startActivity(scoreIntent);
                    finish();

                } else {
                    tvOpA.setBackground(getResources().getDrawable(R.drawable.border, null));
                    tvOpB.setBackground(getResources().getDrawable(R.drawable.border, null));
                    tvOpC.setBackground(getResources().getDrawable(R.drawable.border, null));
                    tvOpD.setBackground(getResources().getDrawable(R.drawable.border, null));

                    ++mIndex;
                    ++mQNo;
                    tvQuestion.setText(getResources().getString(R.string.question) + mQNo + ". " + questions.get(mIndex).getQuestion());
                    tvOpA.setText(questions.get(mIndex).getA());
                    tvOpB.setText(questions.get(mIndex).getB());
                    tvOpC.setText(questions.get(mIndex).getC());
                    tvOpD.setText(questions.get(mIndex).getD());
                    mAnswer = questions.get(mIndex).getAnswer();

                }
            }
        }, TIMEOUT);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.you_cannot_exit), Toast.LENGTH_SHORT).show();
    }
}
