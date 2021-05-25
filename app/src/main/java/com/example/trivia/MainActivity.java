package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.controller.AppController;
import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class    MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SCORE_ID = "score_points";
    private TextView questionTextview;
    private TextView questionCounterTextview;
    private Button trueButton;
    private ImageButton nextButton;
    private ImageButton prevButton;
    private Button falseButton;
    private int currentQuestionIndex = 0;
    private List<Question> questionList;
    private TextView scoreTextview;
    //private int scoreCount = 0;
    private int scoreCounter = 0;
    private Score score;
    private Prefs prefs;
    private TextView highestScoreTextView;

    private Button shareButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nextButton = findViewById(R.id.next_button);
        prevButton = findViewById(R.id.prev_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        questionCounterTextview = findViewById(R.id.counter_text);
        questionTextview = findViewById(R.id.question_textview);
        scoreTextview = findViewById(R.id.score_text);
        score = new Score();
        prefs = new Prefs(MainActivity.this);
        highestScoreTextView = findViewById(R.id.highest_score);

        shareButton = findViewById(R.id.share_button);



        nextButton.setOnClickListener(this);
        prevButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);

        scoreTextview.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        currentQuestionIndex = prefs.getState();
        highestScoreTextView.setText(MessageFormat.format("Highest Score: {0}", String.valueOf(prefs.getHighScore())));

 /*       SharedPreferences.Editor editor;

        SharedPreferences sharedPreferences = getSharedPreferences(SCORE_ID, MODE_PRIVATE);

        if (!sharedPreferences.contains("score_key")) {
            editor = sharedPreferences.edit();
            editor.putInt("score_key", scoreCount);
            editor.apply();
            scoreTextview.setText("Score: " + scoreCount);
            Log.d("Initial_score", "onCreate() score: " + scoreCount);
        }
        else {
            int sharedPreferencesScore = sharedPreferences.getInt("score_key", 0);
            scoreCount = sharedPreferencesScore;
            scoreTextview.setText("Score: "+ scoreCount);
        }
*/



        /*
        List<Question> questionList = new QuestionBank().getQuestions(); this is not good enough as it returns empty ArrayList. We need to use
        asynchronous task to get it going via use of interface as we are pulling resources from the Internet. Below is correct solution
         */
        questionList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {
                questionTextview.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                questionCounterTextview.setText(currentQuestionIndex + " /" + questionArrayList.size());// 0 / 234
                Log.d("Inside", "processFinished: " + questionArrayList);
            }
        });

        //Log.d("Main", "onCreate():" + questionList);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.prev_button:
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex = (currentQuestionIndex - 1) % questionList.size();
                    updateQuestion();
                }
                break;
            case R.id.next_button:
                goNext();

                break;
            case R.id.true_button:
                checkAnswer(true);
                updateQuestion();
                break;
            case R.id.false_button:
                checkAnswer(false);
                updateQuestion();
                break;

            case R.id.share_button:

                //Toast.makeText(this, "Share Clicked", Toast.LENGTH_LONG).show();
                shareScore();

                break;
        }
    }

    private void shareScore() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"My current score is: " + score.getScore() + ". And my highest is: "+ prefs.getHighScore() +".");
        intent.putExtra(Intent.EXTRA_SUBJECT, "I am playing Trivia.");
        startActivity(intent);
    }

    private void checkAnswer(boolean userChooseCorrect) {
        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;
        if (userChooseCorrect == answerIsTrue) {
            fadeView();
            addPoints();
            toastMessageId = R.string.correct_answer;
            //scoreCount++;

        }
        else {
            shakeAnimation();
            toastMessageId = R.string.wrong_answer;
            deductPoints();
          /*  if (scoreCount > 0) {
                scoreCount--;
            }*/
        }
        Toast.makeText(this, toastMessageId, Toast.LENGTH_SHORT).show();
    }

    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);
        scoreTextview.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        //Log.d("Score", "addPoints: " + score.getScore());
    }


    private void deductPoints() {
        scoreCounter -= 100;
        if (scoreCounter > 0) {
            score.setScore(scoreCounter);
            scoreTextview.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        } else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
            scoreTextview.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
            //Log.d("Score Bad", "deductPoints: " + score.getScore());
        }

    }

    private void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        questionTextview.setText(question);
        questionCounterTextview.setText(MessageFormat.format("{0} / {1}", currentQuestionIndex, questionList.size()));
        //scoreTextview.setText("Score: " + scoreCount);
    }

    private void fadeView() {
        CardView cardView = findViewById(R.id.cardView);
        AlphaAnimation alphaAnimation =  new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(350);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setBackgroundColor(getResources().getColor(R.color.green));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setBackgroundColor(getResources().getColor(R.color.white));
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setBackgroundColor(getResources().getColor(R.color.red));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setBackgroundColor(getResources().getColor(R.color.white));
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

/*    protected void onDestroy() {

        SharedPreferences sharedPreferences = getSharedPreferences(SCORE_ID, MODE_PRIVATE);
        int scoreFromSharedPreferences = sharedPreferences.getInt("score_key", 0);

            if (scoreFromSharedPreferences < scoreCount) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("score_key", scoreCount);
                editor.apply();
            }

        super.onDestroy();
    }*/

    private void goNext() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }

    @Override
    protected void onPause() {
        prefs.saveHighScore(score.getScore());
        prefs.setState(currentQuestionIndex);
        super.onPause();
    }
}