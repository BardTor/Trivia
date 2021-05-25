package com.example.trivia.data;

//method to get all the questions... coming from API duh

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.trivia.controller.AppController;
import com.example.trivia.model.Question;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import static com.example.trivia.controller.AppController.TAG;

public class QuestionBank {

    ArrayList<Question> questionArrayList = new ArrayList<>();
    private String url = "https://raw.githubusercontent.com/curiousily/simple-quiz/master/script/statements-data.json";


    public List<Question> getQuestions(final AnswerListAsyncResponse callBack) {//could use ArrayList

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, (JSONArray) null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {//array of arrays => [["Horoscopes accurately predict future events 85% of the time.",false]... []...]
                //Log.d("JSON Stuff","onResponse: " + response);
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Question question = new Question();
                        question.setAnswer(response.getJSONArray(i).getString(0));
                        question.setAnswerTrue(response.getJSONArray(i).getBoolean(1));

                        //Add question objects to List
                        questionArrayList.add(question);
                        //Log.d("hello", "onResponse: " + question);

                        //Log.d("JSON", "onResponse(): " + response.getJSONArray(i).getString(0));//retrieve "i" array, and from that array "zero" indexed object, this case String
                        //Log.d("JSON", "onResponse(): " + response.getJSONArray(i).getBoolean(1));//and here from array "i" we retrieve "one" indexed object, e.g. boolean
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                if (callBack != null)
                    callBack.processFinished(questionArrayList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        AppController.getInstance().addToRequestQueue(jsonArrayRequest);

        return questionArrayList;

    }

}
