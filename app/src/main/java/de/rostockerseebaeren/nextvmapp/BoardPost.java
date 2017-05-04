package de.rostockerseebaeren.nextvmapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Maxa on 02.01.2017.
 */

public class BoardPost {
    public long mTopicLastUpdate;
    public String mTopic;
    public String mUser;
    public String mMessage;
    public String mTopicID;


    public BoardPost(String topic, String user, String message, String id, long upd) {
        this.mTopic = topic;
        this.mUser = user;
        this.mMessage = message;
        this.mTopicID = id;
        this.mTopicLastUpdate = upd;
    }

    public BoardPost() {

    }

    public BoardPost(JSONObject jsonObject) {
        try {
            this.mTopic = jsonObject.getString("subject");
            this.mUser = jsonObject.getString("last_post_guest_name");
            this.mMessage = jsonObject.getString("last_post_message");
            this.mTopicID = jsonObject.getString("id");
            this.mTopicLastUpdate = jsonObject.getLong("last_post_time");
        } catch (JSONException e) {e.printStackTrace();}
    }

    public void BoardPost(String strJson) {
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);
            this.mTopic = jsonRootObject.getString("subject");
            this.mUser = jsonRootObject.getString("last_post_guest_name");
            this.mMessage = jsonRootObject.getString("last_post_message");
            this.mTopicID = jsonRootObject.getString("id");
            this.mTopicLastUpdate = jsonRootObject.getLong("last_post_time");
        } catch (JSONException e) {e.printStackTrace();}
    }
}
