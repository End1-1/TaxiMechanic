package com.taximechanic;

import org.json.JSONException;
import org.json.JSONObject;

public class Question {

    public int mId;
    public String mQuestion;
    public String mFieldName;
    public String mComment;
    public String mCreatedAt;
    public String mUpdatedAt;
    public boolean mYes;
    public boolean mNo;

    public Question() {
        init();
    }

    public Question(JSONObject jo) {
        init();
        try {
            mId = jo.getInt("question_id");
            mQuestion = jo.getString("question");
            mFieldName = jo.getString("field_name");
            mComment = "";
            mCreatedAt = jo.getString("created_at");
            mUpdatedAt = jo.getString("updated_at");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        mYes = false;
        mNo = false;
    }
}